package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.product.ProductListVo;
import com.loopers.domain.product.ProductModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductModel, String> {

    Optional<ProductModel> findProductByProductId(String productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductModel p WHERE p.productId = :productId")
    Optional<ProductModel> findProductByProductIdWithLock(@Param("productId") String productId);
    @Query("""
    select new com.loopers.domain.product.ProductListVo(
        p.productId,
        p.productName,
        b.brandId,
        b.brandName,
        p.price,
        p.stock,
        ls.totalLikeCount
    )
    from ProductModel p
    join p.brand b
    left join LikeSummaryModel ls on ls.productId = p.productId
    where (
      :brandName is null
      or b.brandId in (
          select br.brandId
          from BrandModel br
          where br.brandName like concat(:brandName, '%')  
      )
    )                                                                   
     order by ls.totalLikeCount desc, ls.productId asc
""")
    Page<ProductListVo> findAllProdcutListVoByLikeDesc(
            @Param("brandName") String brandName,
            Pageable pageable
    );
}
