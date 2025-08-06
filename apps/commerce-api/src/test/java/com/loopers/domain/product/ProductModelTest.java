package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductModelTest {
    @DisplayName("재고를 차감할 때")
    @Nested
    class Decrease{
        @DisplayName("상품은 재고를 가지고 있고, 주문 시 재고가 음수일 시 실패한다.")
        @Test
        void throwException_whenStockIsInsufficient(){
            // arrange
            ProductModel product = new ProductModel(
                "product1",
                "테스트 상품",
                "테스트 상품입니당",
                    "brand1",
                    1000L,
                    3L
            );

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                product.decreaseStock(4L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }

        @DisplayName("주문시 재고가 주문 수량보다 많을시 재고를 감소시키는데에 성공한다.")
        @Test
        void decreaseStock_whenOrderQuantityIsSufficient(){
            // arrange
            ProductModel product = new ProductModel(
                    "product1",
                    "테스트 상품",
                    "테스트 상품입니당",
                    "brand1",
                    1000L,
                    3L
            );

            // act
            product.decreaseStock(2L);

            // assert
            assertThat(product.getStock()).isEqualTo(1);
        }
    }
}
