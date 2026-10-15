package com.kkargas.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.service.ReportService;


@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;


    public ReportController(
            ReportService reportService) {

        this.reportService =
                reportService;
    }


    @GetMapping
    public String reportsPage(
            Model model) {

        model.addAttribute(
                "activePage",
                "reports"
        );

        return "reports/index";
    }


    @GetMapping("/summary")
    public String summaryReport(
            Model model) {

        addGeneralStatistics(model);

        model.addAttribute(
                "activePage",
                "reports"
        );

        return "reports/summary";
    }


    @GetMapping("/full")
    public String fullReport(
            Model model) {

        addGeneralStatistics(model);


        model.addAttribute(
                "representatives",
                reportService.getRepresentatives()
        );


        model.addAttribute(
                "artisanships",
                reportService.getArtisanships()
        );


        model.addAttribute(
                "receipts",
                reportService.getReceipts()
        );


        model.addAttribute(
                "reportService",
                reportService
        );


        model.addAttribute(
                "activePage",
                "reports"
        );


        return "reports/full";
    }


    private void addGeneralStatistics(
            Model model) {

        model.addAttribute(
                "representativesCount",
                reportService.getRepresentativesCount()
        );


        model.addAttribute(
                "artisanshipsCount",
                reportService.getArtisanshipsCount()
        );


        model.addAttribute(
                "receiptsCount",
                reportService.getReceiptsCount()
        );


        model.addAttribute(
                "totalSales",
                reportService.getTotalSales()
        );


        model.addAttribute(
                "totalCommissions",
                reportService.getTotalCommissions()
        );


        model.addAttribute(
                "trousersSales",
                reportService.getCategorySales(
                        ReceiptType.TROUSERS
                )
        );


        model.addAttribute(
                "skirtsSales",
                reportService.getCategorySales(
                        ReceiptType.SKIRTS
                )
        );


        model.addAttribute(
                "blousesSales",
                reportService.getCategorySales(
                        ReceiptType.BLOUSES
                )
        );


        model.addAttribute(
                "coatsSales",
                reportService.getCategorySales(
                        ReceiptType.COATS
                )
        );
    }
}