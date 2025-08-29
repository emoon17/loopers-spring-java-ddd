package com.loopers.application.order;

import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.CreatedOrderCommand;
import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.PaymentsModel;
import com.loopers.domain.payments.PaymentsService;
import com.loopers.domain.payments.event.PaymentFailedEvent;
import com.loopers.domain.payments.event.PaymentSucceededEvent;
import com.loopers.domain.payments.event.RequestPaymentsCommand;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Component
public class OrderFacade {
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;
    private final PaymentsService paymentsService;
    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderInfo createOrderFromCart(UserModel user, String userCouponId, Long useAmount, String cardType, String cardNo, String callbackUrl) { // userCouponModel ㅍ람추가예정
        // 1. 유저 장바구니 조회
        CartModel cart = cartService.getOrCreateCart(user);
        List<CartItemModel> cartItems = cartService.getAllCart(cart);

        // 2. CartItem → OrderItemModel 리스트로 변환 --> order model 책임으로 리팩
        // TODO 선택 주문일 경우, items 파라미터로 받아서 처리할건지?
        List<OrderItemModel> orderItems = cartItems.stream()
                .map(cartItem -> OrderItemModel.from(cartItem, null))
                .toList();

        // 3. OrderModel 생성
        OrderModel order = orderService.handle(new CreatedOrderCommand(user.getLoginId(), orderItems, "CART", userCouponId));


        // 5. 재고 차감
        productService.decreaseProductStock(orderItems);

        Long totalPrice = order.getTotalPrice();
        // 6. 쿠폰 차감
        if (order.getUserCouponId() != null) { // 쿠폰 쪽에서 없어도 넘어갈 수 있도록 해야됌.
            CouponModel coupon = couponService.getCouponbyCouponIdWithLock(userCouponId);
            // 쿠폰 차감
            userCouponService.useCoupon(user.getLoginId(), userCouponId);
            
            // 결제금액
            totalPrice = coupon.applyDiscount(totalPrice);
        }

        // 6. 포인트 차감
        PointModel pointModel = pointService.getPointModelByLoginId(user.getLoginId());
        pointService.usePoint(order.getOrderId(), user.getLoginId() , useAmount); //차감
        totalPrice = pointModel.applyToPayment(totalPrice, useAmount); // 결제금액


        // pg호출
        paymentsService.handle(
                new RequestPaymentsCommand(
                        order.getOrderId(),
                        user.getLoginId(),
                        totalPrice,
                        cardType,
                        cardNo,
                        callbackUrl
                )
        );

        // 7. 주문 정보 반환
        List<OrderItemInfo> orderItemInfos = orderItems.stream()
                .map(OrderItemInfo::from)
                .toList();

        return OrderInfo.from(order, orderItemInfos);

    }


    /**
     * 결제가 확정(SUCCESS)된 후 주문 확정 처리 (콜백/스케줄러에서 호출)
     */
    @Transactional
    public void finalizeOrderAfterPayment(PaymentsModel payment, PaymentStatus status ) {
        OrderModel order = orderService.findOrderById(payment.getOrderId());

        if (status == PaymentStatus.SUCCESS) {
            order.markPaid();
            orderService.saveOrder(order);
            eventPublisher.publishEvent(
                    new PaymentSucceededEvent(order.getOrderId(), payment.getPaymentId())
            );
            // 영수증/알림 등 추가 - todo
        } else if (status == PaymentStatus.FAILED || status == PaymentStatus.REJECTED) {
            order.markPaymentFailed();
            orderService.saveOrder(order);
            eventPublisher.publishEvent(
                    new PaymentFailedEvent(order.getOrderId(), order.getUserCouponId(), status.toString())
            );
        } else {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제가 아직 완료되지 않았습니다.");
        }
    }

}
