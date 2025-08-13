package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductModel> getAllProducts(ProductSortCondition sortCondition) {
        return productRepository.findAllProducts(sortCondition);
    }

    public Optional<ProductModel> getProduct(String productId) {
        return productRepository.findProduct(productId);
    }

    public Optional<ProductModel> getProductByProductId(String productId) {
        return productRepository.findProductByProductId(productId);
    }

    public Optional<ProductModel> getProductByProductIdWithLock(String productId) {
        return productRepository.findProductByProductIdWithLock(productId);
    }

    @Transactional
    public void decreaseProductStock(List<OrderItemModel> orderItems) {
        for (OrderItemModel item : orderItems) {
            ProductModel product = getProductByProductIdWithLock(item.getProductId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));
            product.decreaseStock(item.getQuantity());
            saveProduct(product);
        }
    }

    public void saveProduct(ProductModel product) {
        productRepository.saveProduct(product);
    }

    public Page<ProductListVo> getProducts(String brandId, String sort, Pageable pageable){
        return productRepository.findAllProdcutListVo(brandId, sort, pageable);
    }

}
