package com.kkargas.sales.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kkargas.sales.service.ExportService;
import com.kkargas.sales.service.ExportService.ExportResult;
import com.kkargas.sales.service.ExportService.ZipExportResult;
import com.kkargas.sales.service.RepresentativeService;


@Controller
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;
    private final RepresentativeService representativeService;


    public ExportController(
            ExportService exportService,
            RepresentativeService representativeService) {

        this.exportService =
                exportService;

        this.representativeService =
                representativeService;
    }


    @GetMapping
    public String exportPage(
            Model model) {

        model.addAttribute(
                "representatives",
                representativeService
                        .getAllRepresentatives()
        );


        model.addAttribute(
                "activePage",
                "export"
        );


        return "export/index";
    }


    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestParam Long representativeId,
            @RequestParam String format) {


        ExportResult result =
                exportService
                        .exportRepresentative(
                                representativeId,
                                format
                        );


        byte[] data =
                result.content()
                      .getBytes(
                              StandardCharsets.UTF_8
                      );


        HttpHeaders headers =
                new HttpHeaders();


        headers.setContentType(
                MediaType.parseMediaType(
                        result.contentType()
                                + ";charset=UTF-8"
                )
        );


        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                result.filename(),
                                StandardCharsets.UTF_8
                        )
                        .build()
        );


        headers.setContentLength(
                data.length
        );


        return ResponseEntity
                .ok()
                .headers(headers)
                .body(data);
    }
    
    @GetMapping("/download-all")
    public ResponseEntity<byte[]> downloadAll(
            @RequestParam String format) {


        ZipExportResult result =
                exportService
                        .exportAll(
                                format
                        );


        HttpHeaders headers =
                new HttpHeaders();


        headers.setContentType(
                MediaType.parseMediaType(
                        "application/zip"
                )
        );


        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                result.filename(),
                                StandardCharsets.UTF_8
                        )
                        .build()
        );


        headers.setContentLength(
                result.data().length
        );


        return ResponseEntity
                .ok()
                .headers(headers)
                .body(
                        result.data()
                );
    }
}