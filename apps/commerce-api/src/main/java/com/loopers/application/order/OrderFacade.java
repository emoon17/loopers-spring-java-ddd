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
import com.loopers.domain.point.event.UsePointCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.DecreaseStockCommand;
import com.loopers.domain.userCoupon.event.UseCouponCommand;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

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
        CartModel cart = cartService.getOrCreateCart(user);
        List<CartItemModel> cartItems = cartService.getAllCart(cart);

        List<OrderItemModel> orderItems = OrderItemModel.fromCartItems(cartItems, null);
        OrderModel order = orderService.handle(new CreatedOrderCommand(user.getLoginId(), orderItems, "CART", userCouponId));

        productService.handle(new DecreaseStockCommand(orderItems));

        Long totalPrice = order.getTotalPrice();
        if (order.getUserCouponId() != null) {
            CouponModel coupon = couponService.getCouponbyCouponIdWithLock(userCouponId);
            totalPrice = coupon.applyDiscount(totalPrice);
            userCouponService.handle(new UseCouponCommand(
                    user.getLoginId(), userCouponId
            ));
        }

        totalPrice = pointService.handle(new UsePointCommand(
                order.getOrderId(), user.getLoginId() , useAmount, totalPrice
        ));

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
