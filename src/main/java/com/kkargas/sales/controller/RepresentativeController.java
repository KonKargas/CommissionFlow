package com.kkargas.sales.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.model.Representative;
import com.kkargas.sales.service.CommissionService;
import com.kkargas.sales.service.ReceiptService;
import com.kkargas.sales.service.RepresentativeService;

@Controller
@RequestMapping("/representatives")
public class RepresentativeController {

    private final RepresentativeService representativeService;
    private final ReceiptService receiptService;
    private final CommissionService commissionService;


    public RepresentativeController(
            RepresentativeService representativeService,
            ReceiptService receiptService,
            CommissionService commissionService) {

        this.representativeService = representativeService;
        this.receiptService = receiptService;
        this.commissionService = commissionService;
    }


    // =========================
    // LIST
    // =========================

    @GetMapping
    public String getRepresentatives(Model model) {

        model.addAttribute(
                "representatives",
                representativeService.getAllRepresentatives()
        );

        model.addAttribute(
                "activePage",
                "representatives"
        );

        return "representatives/list";
    }


    // =========================
    // CREATE FORM
    // =========================

    @GetMapping("/new")
    public String createRepresentativeForm(
            @RequestParam(required = false) String from,
            Model model) {

        model.addAttribute(
                "representative",
                new Representative()
        );

        model.addAttribute(
                "returnUrl",
                "dashboard".equals(from)
                        ? "/dashboard"
                        : "/representatives"
        );

        model.addAttribute(
                "activePage",
                "representatives"
        );

        return "representatives/form";
    }


    // =========================
    // EDIT FORM
    // =========================
    
    @GetMapping("/edit/{id}")
    public String editRepresentative(
            @PathVariable Long id,
            Model model) {

        Representative representative =
                representativeService.getRepresentativeById(id);

        if (representative == null) {
            return "redirect:/representatives";
        }

        model.addAttribute(
                "representative",
                representative
        );

        model.addAttribute(
                "returnUrl",
                "/representatives"
        );

        model.addAttribute(
                "activePage",
                "representatives"
        );

        return "representatives/form";
    }

    
    // =========================
    // CREATE / UPDATE
    // =========================

    @PostMapping("/save")
    public String saveRepresentative(
            @ModelAttribute Representative representative) {

        representativeService.saveRepresentative(
                representative
        );

        return "redirect:/representatives";
    }


    // =========================
    // DELETE
    // =========================

    @PostMapping("/delete/{id}")
    public String deleteRepresentative(
            @PathVariable Long id) {

        representativeService.deleteRepresentative(id);

        return "redirect:/representatives";
    }
    
 // =========================
 // MANAGE SALES
 // =========================

 @GetMapping("/{id}/sales")
 public String manageSales(
         @PathVariable Long id,
         Model model) {

     Representative representative =
             representativeService.getRepresentativeById(id);

     if (representative == null) {
         return "redirect:/representatives";
     }


     model.addAttribute(
             "representative",
             representative
     );


     model.addAttribute(
             "receipts",
             receiptService.getReceiptsByRepresentative(id)
     );


     model.addAttribute(
             "totalSales",
             receiptService.getTotalSalesByRepresentative(id)
     );


     model.addAttribute(
             "totalItems",
             receiptService.getTotalItemsByRepresentative(id)
     );


     model.addAttribute(
             "commission",
             receiptService.calculateCommission(id)
     );


     model.addAttribute(
             "trousersSales",
             receiptService.getSalesByCategory(
                     id,
                     ReceiptType.TROUSERS
             )
     );


     model.addAttribute(
             "skirtsSales",
             receiptService.getSalesByCategory(
                     id,
                     ReceiptType.SKIRTS
             )
     );


     model.addAttribute(
             "blousesSales",
             receiptService.getSalesByCategory(
                     id,
                     ReceiptType.BLOUSES
             )
     );


     model.addAttribute(
             "coatsSales",
             receiptService.getSalesByCategory(
                     id,
                     ReceiptType.COATS
             )
     );


     model.addAttribute(
             "activePage",
             "representatives"
     );
     
     BigDecimal totalSales =
    	        receiptService.getTotalSalesByRepresentative(id);

    	model.addAttribute(
    	        "totalSales",
    	        totalSales
    	);

    	model.addAttribute(
    	        "commission",
    	        commissionService.calculateCommission(totalSales)
    	);


     return "representatives/manage-sales";
 }
}