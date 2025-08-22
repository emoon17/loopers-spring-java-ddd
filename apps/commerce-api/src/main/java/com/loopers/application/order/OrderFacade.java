package com.loopers.application.order;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.payment.PaymentsFacade;
import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final CouponFacade couponFacade;
    private final PaymentsFacade paymentsFacade;

    @Transactional
    public OrderInfo createOrderFromCart(UserModel user, String userCouponId, String cardType, String cardNo, String callbackUrl) { // userCouponModel ㅍ람추가예정
        // 1. 유저 장바구니 조회
        CartModel cart = cartService.getOrCreateCart(user);
        List<CartItemModel> cartItems = cartService.getAllCart(cart);

        // 2. CartItem → OrderItemModel 리스트로 변환
        // TODO 선택 주문일 경우, items 파라미터로 받아서 처리할건지?
        List<OrderItemModel> orderItems = cartItems.stream()
                .map(cartItem -> OrderItemModel.from(cartItem, null))
                .toList();

        // 3. OrderModel 생성
        OrderModel order = OrderModel.create(user.getLoginId(), orderItems, "CART");
        orderItems.forEach(item -> item.assignOrderId(order.getOrderId()));

        // 쿠폰 차감
        couponFacade.applyCoupon(user, orderItems, userCouponId);

        // 4. 포인트 차감
        pointService.usePoint(user.getLoginId(), order.getTotalPrice());

        // 5. product 재고 차감
        productService.decreaseProductStock(orderItems);

        // 6. 주문 저장
        orderService.saveOrder(order);
        orderService.saveOrderItems(orderItems);

        // 결제시도
        String paymentId = paymentsFacade.startAttempt(order.getOrderId(), order.getTotalPrice());

        // pg호출
        afterCommit(()-> paymentsFacade.requestToPg(
                user.getLoginId(), paymentId, cardType, cardNo, callbackUrl
        ));


        // 7. 주문 정보 반환
        List<OrderItemInfo> orderItemInfos = orderItems.stream()
                .map(OrderItemInfo::from)
                .toList();

        // 8. 카트 비우기
//        cartService.clearCart(cart);
        return OrderInfo.from(order, orderItemInfos);

    }

    // 트랜잭션 커밋 이후에 실행
    private void afterCommit(Runnable r) {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCommit() { r.run(); }
                }
        );
    }

    /**
     * 결제가 확정(SUCCESS)된 후 주문 확정 처리 (콜백/스케줄러에서 호출)
     */
    @Transactional
    public void finalizeOrderAfterPayment(String orderId) {
        var status = paymentsFacade.getCurrnentStatus(orderId);
        var order = orderService.findOrderById(orderId);

        if (status == PaymentStatus.SUCCESS) {

            order.markPaid();
            orderService.saveOrder(order);
            // 영수증/알림 등 추가
        } else if (status == PaymentStatus.FAILED || status == PaymentStatus.REJECTED) {
            order.markPaymentFailed();
            orderService.saveOrder(order);
            // 필요시 롤백/알림
        }
    }

    // 상품 -> 주문 바로하기
}
