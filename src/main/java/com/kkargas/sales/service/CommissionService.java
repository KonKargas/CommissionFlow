package com.kkargas.sales.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class CommissionService {

    private static final BigDecimal SIX_THOUSAND =
            new BigDecimal("6000");

    private static final BigDecimal TEN_THOUSAND =
            new BigDecimal("10000");

    private static final BigDecimal FORTY_THOUSAND =
            new BigDecimal("40000");

    private static final BigDecimal TEN_PERCENT =
            new BigDecimal("0.10");

    private static final BigDecimal FIFTEEN_PERCENT =
            new BigDecimal("0.15");

    private static final BigDecimal TWENTY_PERCENT =
            new BigDecimal("0.20");


    public BigDecimal calculateCommission(BigDecimal totalSales) {

        if (totalSales == null
                || totalSales.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO.setScale(2);
        }


        // Up to €6,000
        if (totalSales.compareTo(SIX_THOUSAND) <= 0) {

            return BigDecimal.ZERO.setScale(2);
        }


        // €6,001 - €10,000
        if (totalSales.compareTo(TEN_THOUSAND) <= 0) {

            return totalSales
                    .subtract(SIX_THOUSAND)
                    .multiply(TEN_PERCENT)
                    .setScale(2, RoundingMode.HALF_UP);
        }


        // €10,001 - €40,000
        if (totalSales.compareTo(FORTY_THOUSAND) <= 0) {

            BigDecimal firstPart =
                    TEN_THOUSAND.multiply(TEN_PERCENT);

            BigDecimal remaining =
                    totalSales.subtract(TEN_THOUSAND);

            BigDecimal secondPart =
                    remaining.multiply(FIFTEEN_PERCENT);

            return firstPart
                    .add(secondPart)
                    .setScale(2, RoundingMode.HALF_UP);
        }


        // Over €40,000

        BigDecimal firstPart =
                TEN_THOUSAND.multiply(TEN_PERCENT);

        BigDecimal secondPart =
                new BigDecimal("30000")
                        .multiply(FIFTEEN_PERCENT);

        BigDecimal remaining =
                totalSales.subtract(FORTY_THOUSAND);

        BigDecimal thirdPart =
                remaining.multiply(TWENTY_PERCENT);

        return firstPart
                .add(secondPart)
                .add(thirdPart)
                .setScale(2, RoundingMode.HALF_UP);
    }
}