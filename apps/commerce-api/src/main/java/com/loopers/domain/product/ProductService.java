package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.event.DecreaseStockCommand;
import com.loopers.support.cache.CacheKeys;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final OrderService orderService;

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
    public void handle(DecreaseStockCommand command) {
        for (OrderItemModel item : command.items()) {
            ProductModel product = getProductByProductIdWithLock(item.getProductId())
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));
            product.decreaseStock(item.getQuantity());
            saveProduct(product);
        }
    }

    @Transactional
    public void restoreStock(String productId, Long quantity) {
        ProductModel product = productRepository.findProductByProductId(productId).orElseThrow();
        if (product.getStock() < quantity) {
            product.increaseStock(quantity);
            productRepository.saveProduct(product);
        }
    }

    public void saveProduct(ProductModel product) {
        productRepository.saveProduct(product);
    }

    public Page<ProductListVo> getProducts(String brandName, ProductSortCondition sort, Pageable pageable) {
        if (sort == null) sort = ProductSortCondition.LATEST;

        if (sort == ProductSortCondition.LIKES_DESC) {
            long var = getOrInit(CacheKeys.likesGlobalVersionKey());
            String key = CacheKeys.likesListKey(brandName, pageable, var);

            try {
                @SuppressWarnings("unchecked")
                List<ProductListVo> cached = (List<ProductListVo>) redisTemplate.opsForValue().get(key);
                if (cached != null) {
                    log.info("List cache Hit ::: {}", key);
                    return new PageImpl<>(cached, pageable, cached.size());
                }

            } catch (Exception e) {
                log.warn("cache get err :: {}", e.getMessage());
            }

            Page<ProductListVo> page = productRepository.findAllProdcutListVoByLikeDesc(brandName, pageable);

            int base = 45, jitter = (int) (base * 0.1);
            int ttl = base + new java.util.Random().nextInt(jitter + 1);
            try {
                redisTemplate.opsForValue().set(key, page.getContent(), Duration.ofSeconds(ttl));
                log.info("[LIST] DB → cache SET: {} (size={}, ttl={}s)", key, page.getContent().size(), ttl);
            } catch (Exception e) {
                log.warn("cache set err: {}", e.getMessage());
            }

            return page;
        }


        return productRepository.findAllProdcutListVoByLikeDesc(brandName, pageable);
        // 추후 정렬 조건에 따라 분기 태울 예정
//        return switch (sort) {
//            case LIKE_DESC ->  productRepository.findAllProdcutListVoByLikeDesc(brandName, pageable);
//        };
    }

    private long getOrInit(String verKey) {
        try {
            Object v = redisTemplate.opsForValue().get(verKey);
            if (v == null) { redisTemplate.opsForValue().setIfAbsent(verKey, 1L); return 1L; }
            return Long.parseLong(v.toString());
        } catch (Exception e) { return 1L; }
    }
}
