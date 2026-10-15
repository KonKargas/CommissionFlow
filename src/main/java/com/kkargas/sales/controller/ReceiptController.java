package com.kkargas.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.model.Representative;
import com.kkargas.sales.service.ArtisanshipService;
import com.kkargas.sales.service.ReceiptService;
import com.kkargas.sales.service.RepresentativeService;

@Controller
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final RepresentativeService representativeService;
    private final ArtisanshipService artisanshipService;


    public ReceiptController(
            ReceiptService receiptService,
            RepresentativeService representativeService,
            ArtisanshipService artisanshipService) {

        this.receiptService = receiptService;
        this.representativeService = representativeService;
        this.artisanshipService = artisanshipService;
    }
    
    @GetMapping
    public String getReceipts(Model model) {

        model.addAttribute(
                "receipts",
                receiptService.getAllReceipts()
        );

        model.addAttribute(
                "activePage",
                "receipts"
        );

        return "receipts/list";
    }

    @GetMapping("/new")
    public String createReceiptForm(
            @RequestParam(required = false) Long representativeId,
            @RequestParam(required = false) String from,
            Model model) {

        Receipt receipt = new Receipt();

        if (representativeId != null) {

            Representative representative =
                    representativeService
                            .getRepresentativeById(representativeId);

            if (representative != null) {
                receipt.setRepresentative(representative);
            }
        }


        model.addAttribute(
                "receipt",
                receipt
        );

        model.addAttribute(
                "representatives",
                representativeService.getAllRepresentatives()
        );

        model.addAttribute(
                "artisanships",
                artisanshipService.getAllArtisanships()
        );

        model.addAttribute(
                "returnUrl",
                resolveReceiptReturnUrl(from)
        );

        model.addAttribute(
                "activePage",
                "receipts"
        );

        return "receipts/form";
    }
    
    private String resolveReceiptReturnUrl(String from) {

        if ("dashboard".equals(from)) {
            return "/dashboard";
        }

        if ("representatives".equals(from)) {
            return "/representatives";
        }

        return "/receipts";
    }

    @GetMapping("/edit/{id}")
    public String editReceipt(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            Model model) {

        Receipt receipt =
                receiptService.getReceiptById(id);

        if (receipt == null) {
            return "redirect:/receipts";
        }

        model.addAttribute(
                "receipt",
                receipt
        );

        model.addAttribute(
                "representatives",
                representativeService.getAllRepresentatives()
        );

        model.addAttribute(
                "artisanships",
                artisanshipService.getAllArtisanships()
        );

        model.addAttribute(
                "returnUrl",
                resolveReceiptReturnUrl(from)
        );

        model.addAttribute(
                "activePage",
                "receipts"
        );

        return "receipts/form";
    }


    @PostMapping("/save")
    public String saveReceipt(
            @ModelAttribute Receipt receipt,
            @RequestParam(required = false) String returnUrl,
            Model model) {

        Receipt existingReceipt =
                receiptService.getReceiptByCode(
                        receipt.getReceiptCode()
                );


        /*
         * Duplicate receipt code.
         *
         * CREATE:
         * Any existing receipt with the same code is invalid.
         *
         * EDIT:
         * The current receipt is allowed to keep its own code.
         */
        if (existingReceipt != null
                && (receipt.getReceiptId() == null
                || !existingReceipt.getReceiptId()
                        .equals(receipt.getReceiptId()))) {

            model.addAttribute(
                    "receiptCodeWarning",
                    "A receipt with code "
                            + receipt.getReceiptCode()
                            + " already exists."
            );

            model.addAttribute(
                    "receipt",
                    receipt
            );

            model.addAttribute(
                    "representatives",
                    representativeService
                            .getAllRepresentatives()
            );

            model.addAttribute(
                    "artisanships",
                    artisanshipService
                            .getAllArtisanships()
            );

            model.addAttribute(
                    "returnUrl",
                    returnUrl != null
                            && !returnUrl.isBlank()
                            ? returnUrl
                            : "/receipts"
            );

            model.addAttribute(
                    "activePage",
                    "receipts"
            );

            return "receipts/form";
        }


        receiptService.saveReceipt(
                receipt
        );

        return "redirect:/receipts";
    }


    @PostMapping("/delete/{id}")
    public String deleteReceipt(
            @PathVariable Long id) {

        receiptService.deleteReceipt(id);

        return "redirect:/receipts";
    }
}