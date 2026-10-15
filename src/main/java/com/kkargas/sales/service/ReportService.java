package com.kkargas.sales.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.model.Representative;


@Service
public class ReportService {

    private final RepresentativeService representativeService;
    private final ArtisanshipService artisanshipService;
    private final ReceiptService receiptService;
    private final CommissionService commissionService;


    public ReportService(
            RepresentativeService representativeService,
            ArtisanshipService artisanshipService,
            ReceiptService receiptService,
            CommissionService commissionService) {

        this.representativeService =
                representativeService;

        this.artisanshipService =
                artisanshipService;

        this.receiptService =
                receiptService;

        this.commissionService =
                commissionService;
    }


    public List<Representative> getRepresentatives() {

        return representativeService
                .getAllRepresentatives();
    }


    public List<Artisanship> getArtisanships() {

        return artisanshipService
                .getAllArtisanships();
    }


    public List<Receipt> getReceipts() {

        return receiptService
                .getAllReceipts();
    }


    public long getRepresentativesCount() {

        return representativeService
                .countRepresentatives();
    }


    public long getArtisanshipsCount() {

        return artisanshipService
                .countArtisanships();
    }


    public long getReceiptsCount() {

        return receiptService
                .countReceipts();
    }


    public BigDecimal getTotalSales() {

        return receiptService
                .getTotalSales();
    }


    public BigDecimal getTotalCommissions() {

        return representativeService
                .getAllRepresentatives()
                .stream()
                .map(representative -> {

                    BigDecimal sales =
                            receiptService
                                    .getTotalSalesByRepresentative(
                                            representative.getRepId()
                                    );

                    return commissionService
                            .calculateCommission(sales);
                })
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    public BigDecimal getCategorySales(
            ReceiptType type) {

        return receiptService
                .getTotalSalesByCategory(type);
    }


    public BigDecimal getRepresentativeSales(
            Long representativeId) {

        return receiptService
                .getTotalSalesByRepresentative(
                        representativeId
                );
    }


    public BigDecimal getRepresentativeCommission(
            Long representativeId) {

        BigDecimal sales =
                getRepresentativeSales(
                        representativeId
                );

        return commissionService
                .calculateCommission(sales);
    }


    public List<Receipt> getRepresentativeReceipts(
            Long representativeId) {

        return receiptService
                .getReceiptsByRepresentative(
                        representativeId
                );
    }


    public BigDecimal getRepresentativeCategorySales(
            Long representativeId,
            ReceiptType type) {

        return receiptService
                .getSalesByCategory(
                        representativeId,
                        type
                );
    }
}