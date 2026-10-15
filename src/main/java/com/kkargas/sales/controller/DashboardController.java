package com.kkargas.sales.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.service.CommissionService;
import com.kkargas.sales.service.ReceiptService;
import com.kkargas.sales.service.RepresentativeService;


@Controller
public class DashboardController {

    private final RepresentativeService representativeService;
    private final ReceiptService receiptService;
    private final CommissionService commissionService;


    public DashboardController(
            RepresentativeService representativeService,
            ReceiptService receiptService,
            CommissionService commissionService) {

        this.representativeService =
                representativeService;

        this.receiptService =
                receiptService;

        this.commissionService =
                commissionService;
    }


    @GetMapping("/")
    public String home() {

        return "redirect:/dashboard";
    }


    @GetMapping("/dashboard")
    public String getDashboard(
            Model model) {


        // =============================================
        // COUNTS
        // =============================================

        model.addAttribute(
                "representativesCount",
                representativeService
                        .countRepresentatives()
        );


        model.addAttribute(
                "receiptsCount",
                receiptService
                        .countReceipts()
        );


        // =============================================
        // TOTAL SALES
        // =============================================

        BigDecimal totalSales =
                receiptService
                        .getTotalSales();


        model.addAttribute(
                "totalSales",
                totalSales
        );


        // =============================================
        // TOTAL COMMISSIONS
        // =============================================

        BigDecimal totalCommissions =
                representativeService
                        .getAllRepresentatives()
                        .stream()
                        .map(representative -> {

                            BigDecimal representativeSales =
                                    receiptService
                                            .getTotalSalesByRepresentative(
                                                    representative.getRepId()
                                            );

                            return commissionService
                                    .calculateCommission(
                                            representativeSales
                                    );
                        })
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        model.addAttribute(
                "totalCommissions",
                totalCommissions
        );


        // =============================================
        // SALES BY CATEGORY
        // =============================================

        model.addAttribute(
                "trousersSales",
                receiptService
                        .getTotalSalesByCategory(
                                ReceiptType.TROUSERS
                        )
        );


        model.addAttribute(
                "skirtsSales",
                receiptService
                        .getTotalSalesByCategory(
                                ReceiptType.SKIRTS
                        )
        );


        model.addAttribute(
                "blousesSales",
                receiptService
                        .getTotalSalesByCategory(
                                ReceiptType.BLOUSES
                        )
        );


        model.addAttribute(
                "coatsSales",
                receiptService
                        .getTotalSalesByCategory(
                                ReceiptType.COATS
                        )
        );


        model.addAttribute(
                "activePage",
                "dashboard"
        );


        return "dashboard/index";
    }
}