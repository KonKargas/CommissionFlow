package com.kkargas.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.kkargas.sales.service.ImportService;

@Controller
@RequestMapping("/import")
public class ImportController {

    private final ImportService importService;


    public ImportController(ImportService importService) {
        this.importService = importService;
    }


    @GetMapping
    public String getImportPage(Model model) {

        model.addAttribute(
                "activePage",
                "import"
        );

        return "import/index";
    }


    @PostMapping
    public String importFile(
            @RequestParam("file") MultipartFile file,
            Model model) {

        try {

            int imported =
                    importService.importFile(file);

            model.addAttribute(
                    "successMessage",
                    imported + " receipts imported successfully."
            );

        }
        catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        model.addAttribute(
                "activePage",
                "import"
        );

        return "import/index";
    }
}