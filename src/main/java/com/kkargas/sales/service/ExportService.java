package com.kkargas.sales.service;

import org.springframework.stereotype.Service;

import com.kkargas.sales.exporter.HtmlReceiptExporter;
import com.kkargas.sales.exporter.ReceiptExporter;
import com.kkargas.sales.exporter.TxtReceiptExporter;
import com.kkargas.sales.exporter.XmlReceiptExporter;
import com.kkargas.sales.model.Representative;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class ExportService {

    private final RepresentativeService representativeService;

    private final TxtReceiptExporter txtReceiptExporter;
    private final XmlReceiptExporter xmlReceiptExporter;
    private final HtmlReceiptExporter htmlReceiptExporter;


    public ExportService(
            RepresentativeService representativeService,
            TxtReceiptExporter txtReceiptExporter,
            XmlReceiptExporter xmlReceiptExporter,
            HtmlReceiptExporter htmlReceiptExporter) {

        this.representativeService =
                representativeService;

        this.txtReceiptExporter =
                txtReceiptExporter;

        this.xmlReceiptExporter =
                xmlReceiptExporter;

        this.htmlReceiptExporter =
                htmlReceiptExporter;
    }


    public ExportResult exportRepresentative(
            Long representativeId,
            String format) {

        Representative representative =
                representativeService
                        .getRepresentativeById(
                                representativeId
                        );


        if (representative == null) {

            throw new IllegalArgumentException(
                    "Representative not found."
            );
        }


        ReceiptExporter exporter =
                resolveExporter(format);


        String content =
                exporter.export(
                        representative
                );


        String safeVat =
                representative
                        .getVatNumber()
                        .replaceAll(
                                "[^a-zA-Z0-9_-]",
                                ""
                        );


        String filename =
                safeVat
                        + "_RECEIPTS."
                        + exporter.getFileExtension();


        return new ExportResult(
                filename,
                exporter.getContentType(),
                content
        );
    }
    
    public ZipExportResult exportAll(
            String format) {

        ReceiptExporter exporter =
                resolveExporter(format);


        List<Representative> representatives =
                representativeService
                        .getAllRepresentatives();


        if (representatives.isEmpty()) {

            throw new IllegalStateException(
                    "There are no representatives to export."
            );
        }


        try {

            ByteArrayOutputStream byteStream =
                    new ByteArrayOutputStream();


            try (ZipOutputStream zipOutputStream =
                         new ZipOutputStream(
                                 byteStream,
                                 StandardCharsets.UTF_8
                         )) {


                for (Representative representative
                        : representatives) {


                    String content =
                            exporter.export(
                                    representative
                            );


                    String baseName =
                            createRepresentativeFileName(
                                    representative
                            );


                    String filename =
                            baseName
                                    + "_RECEIPTS."
                                    + exporter.getFileExtension();


                    ZipEntry zipEntry =
                            new ZipEntry(
                                    filename
                            );


                    zipOutputStream.putNextEntry(
                            zipEntry
                    );


                    byte[] contentBytes =
                            content.getBytes(
                                    StandardCharsets.UTF_8
                            );


                    zipOutputStream.write(
                            contentBytes
                    );


                    zipOutputStream.closeEntry();
                }
            }


            String zipFilename =
                    "CommissionFlow_ALL_"
                            + format.trim()
                                    .toUpperCase()
                            + ".zip";


            return new ZipExportResult(
                    zipFilename,
                    byteStream.toByteArray()
            );


        } catch (IOException e) {

            throw new IllegalStateException(
                    "Could not create export ZIP file.",
                    e
            );
        }
    }
    
    private String createRepresentativeFileName(
            Representative representative) {

        String vatNumber =
                representative.getVatNumber();


        if (vatNumber != null
                && !vatNumber.isBlank()) {

            String safeVat =
                    vatNumber.replaceAll(
                            "[^a-zA-Z0-9_-]",
                            ""
                    );


            if (!safeVat.isBlank()) {
                return safeVat;
            }
        }


        return "REPRESENTATIVE_"
                + representative.getRepId();
    }


    private ReceiptExporter resolveExporter(
            String format) {

        if (format == null) {

            throw new IllegalArgumentException(
                    "Export format is required."
            );
        }


        return switch (
                format.trim()
                      .toLowerCase()
        ) {

            case "txt" ->
                    txtReceiptExporter;

            case "xml" ->
                    xmlReceiptExporter;

            case "html",
                 "htm" ->
                    htmlReceiptExporter;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported export format."
                    );
        };
    }


    public record ExportResult(
            String filename,
            String contentType,
            String content) {
    }
    
    public record ZipExportResult(
            String filename,
            byte[] data) {
    }
}