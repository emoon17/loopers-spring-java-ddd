package com.loopers.application.order;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
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

    @Transactional
    public OrderInfo createOrderFromCart(UserModel user, UserCouponModel usercoupon) { // userCouponModel ㅍ람추가예정
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
        couponFacade.applyCoupon(user, orderItems, usercoupon);
        
        // 4. 포인트 차감
        pointService.usePoint(user.getLoginId(), order.getTotalPrice());


        // 5. product 재고 차감
        for (OrderItemModel item : orderItems) {
            ProductModel product = productService.getProductByProductId(item.getProductId())
                    .orElseThrow(()-> new CoreException(ErrorType.BAD_REQUEST, "상품이 존재하지 않습니다."));
            product.decreaseStock(item.getQuantity());
            productService.saveProduct(product);
        }

        // 6. 주문 저장
        orderService.saveOrder(order);
        orderService.saveOrderItems(orderItems);

        // 7. 카트 비우기
        cartService.clearCart(cart);

        // 8. 주문 정보 반환
        List<OrderItemInfo> orderItemInfos = orderItems.stream()
                .map(OrderItemInfo::from)
                .toList();

        return OrderInfo.from(order, orderItemInfos);

    }
    
    // 상품 -> 주문 바로하기
}
