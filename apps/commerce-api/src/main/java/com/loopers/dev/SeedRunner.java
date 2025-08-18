package com.loopers.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("seed")
public class SeedRunner implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;
    public SeedRunner(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    private static final int NUM_BRANDS = 1_200;
    private static final int NUM_PRODUCTS = 100_000;
    private static final int BATCH_SIZE = 1_000;

    private final Random random = new Random();


    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [SEED] start");
        seedBrands();
        seedProducts();
        System.out.println(">>> [SEED] done");
    }

    private void seedBrands(){
        String sql = "INSERT INTO brand (brand_id, brand_name) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE brand_name = VALUES(brand_name)";
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        for (int i = 1; i <= NUM_BRANDS; i++) {
            batch.add(new Object[]{brandId(i), "Brand " + i});
            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) jdbcTemplate.batchUpdate(sql, batch);
        System.out.println(">>> [SEED] brands inserted: " + NUM_BRANDS);
    }

    private void seedProducts() {
        String sql = """
            INSERT INTO product
              (product_id, product_name, product_description, brand_id, price, stock)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE product_name = VALUES(product_name)
            """;

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        for (int i = 1; i <= NUM_PRODUCTS; i++) {
            String pid  = productId(i);
            String name = "상품 " + i;
            String desc = "설명 " + i;
            String bid  = pickBrandWeighted();
            int price   = randomPrice(500, 80_000);
            int stock   = randomStock();

            batch.add(new Object[]{pid, name, desc, bid, price, stock});

            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
            }

            // 큰 사이즈일 때 진행 로그 (선택)
            if (i % 20_000 == 0) {
                System.out.println(">>> [SEED] products inserted: " + i);
            }
        }
        if (!batch.isEmpty()) jdbcTemplate.batchUpdate(sql, batch);
        System.out.println(">>> [SEED] products inserted: " + NUM_PRODUCTS);
    }

    private static String brandId(int i)   { return String.format("b%04d", i); }
    private static String productId(int i) { return String.format("p%06d", i); }

    /** 상위 20개 브랜드에 40%, 다음 180개에 30%, 나머지에 30% 몰리게 (카디널리티 차이를 만들기 위함) */
    private String pickBrandWeighted() {
        int roll = random.nextInt(100);
        if (roll < 40) {                 // 40%: b0001 ~ b0020
            int i = 1 + random.nextInt(Math.min(20, NUM_BRANDS));
            return brandId(i);
        } else if (roll < 70) {          // 30%: b0021 ~ b0200
            int start = Math.min(21, NUM_BRANDS);
            int end   = Math.min(200, NUM_BRANDS);
            int range = Math.max(1, end - start + 1);
            int i = start + random.nextInt(range);
            return brandId(i);
        } else {                         // 30%: b0201 ~ b{NUM_BRANDS}
            int start = Math.min(201, NUM_BRANDS);
            int range = Math.max(1, NUM_BRANDS - start + 1);
            int i = start + random.nextInt(range);
            return brandId(i);
        }
    }

    /** 로그분포로 500~80,000 사이 */
    private int randomPrice(int min, int max) {
        double lnMin = Math.log(min), lnMax = Math.log(max);
        double v = lnMin + (lnMax - lnMin) * random.nextDouble();
        return (int) Math.exp(v);
    }

    /** 재고: 15% 품절, 25% 1~5개, 60% 6~500개 */
    private int randomStock() {
        int r = random.nextInt(100);
        if (r < 15) return 0;                 // 품절 많게
        if (r < 40) return 1 + random.nextInt(5);
        return 6 + random.nextInt(495);
    }
}


