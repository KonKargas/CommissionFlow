package com.kkargas.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.kkargas.sales.model.Address;
import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.service.ArtisanshipService;

@Controller
@RequestMapping("/artisanships")
public class ArtisanshipController {

    private final ArtisanshipService artisanshipService;


    public ArtisanshipController(
            ArtisanshipService artisanshipService) {

        this.artisanshipService = artisanshipService;
    }


    // =========================
    // LIST
    // =========================

    @GetMapping
    public String getArtisanships(Model model) {

        model.addAttribute(
                "artisanships",
                artisanshipService.getAllArtisanships()
        );

        model.addAttribute(
                "activePage",
                "artisanships"
        );

        return "artisanships/list";
    }


    // =========================
    // CREATE FORM
    // =========================

    @GetMapping("/new")
    public String createArtisanshipForm(
            @RequestParam(required = false) String from,
            Model model) {

        Artisanship artisanship =
                new Artisanship();

        artisanship.setArtisanshipAddress(
                new Address()
        );

        model.addAttribute(
                "artisanship",
                artisanship
        );

        model.addAttribute(
                "returnUrl",
                "dashboard".equals(from)
                        ? "/dashboard"
                        : "/artisanships"
        );

        model.addAttribute(
                "activePage",
                "artisanships"
        );

        return "artisanships/form";
    }


    // =========================
    // EDIT FORM
    // =========================

    @GetMapping("/edit/{id}")
    public String editArtisanship(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            Model model) {

        Artisanship artisanship =
                artisanshipService.getArtisanshipById(id);

        if (artisanship == null) {
            return "redirect:/artisanships";
        }

        if (artisanship.getArtisanshipAddress() == null) {
            artisanship.setArtisanshipAddress(
                    new Address()
            );
        }

        model.addAttribute(
                "artisanship",
                artisanship
        );

        model.addAttribute(
                "returnUrl",
                "dashboard".equals(from)
                        ? "/dashboard"
                        : "/artisanships"
        );

        model.addAttribute(
                "activePage",
                "artisanships"
        );

        return "artisanships/form";
    }


    // =========================
    // CREATE / UPDATE
    // =========================

    @PostMapping("/save")
    public String saveArtisanship(
            @ModelAttribute Artisanship artisanship) {

        artisanshipService.saveArtisanship(
                artisanship
        );

        return "redirect:/artisanships";
    }


    // =========================
    // DELETE
    // =========================

    @PostMapping("/delete/{id}")
    public String deleteArtisanship(
            @PathVariable Long id) {

        artisanshipService.deleteArtisanship(id);

        return "redirect:/artisanships";
    }
}