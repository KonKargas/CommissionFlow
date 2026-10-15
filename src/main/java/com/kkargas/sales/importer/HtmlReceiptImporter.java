package com.kkargas.sales.importer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.model.Representative;
import com.kkargas.sales.service.ImportDataService;


@Component
public class HtmlReceiptImporter
        implements ReceiptImporter {

    private final ImportDataService importDataService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy");


    public HtmlReceiptImporter(
            ImportDataService importDataService) {

        this.importDataService =
                importDataService;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFile(
            MultipartFile file) {

        try {

            Document document =
                    Jsoup.parse(
                            file.getInputStream(),
                            StandardCharsets.UTF_8.name(),
                            ""
                    );


            // =================================================
            // REPRESENTATIVE
            // =================================================

            Element representativeElement =
                    document.selectFirst(
                            ".representative"
                    );


            if (representativeElement == null) {

                throw new IllegalArgumentException(
                        "HTML file does not contain representative information."
                );
            }


            Element nameElement =
                    representativeElement.selectFirst(
                            "[data-field=name]"
                    );


            Element afmElement =
                    representativeElement.selectFirst(
                            "[data-field=afm]"
                    );


            if (nameElement == null
                    || afmElement == null) {

                throw new IllegalArgumentException(
                        "Representative name or AFM is missing."
                );
            }


            String representativeName =
                    nameElement.text().trim();


            String vatNumber =
                    afmElement.text().trim();


            Representative representative =
                    importDataService
                            .findOrCreateRepresentative(
                                    representativeName,
                                    vatNumber
                            );


            // =================================================
            // RECEIPTS SECTION
            // =================================================

            Element receiptsSection =
                    document.selectFirst(
                            "section.receipt"
                    );


            if (receiptsSection == null) {

                throw new IllegalArgumentException(
                        "HTML file does not contain receipts."
                );
            }


            Elements receiptLists =
                    new Elements();

            for (Element child
                    : receiptsSection.children()) {

                if (child.tagName()
                        .equalsIgnoreCase("ul")) {

                    receiptLists.add(child);
                }
            }


            if (receiptLists.isEmpty()) {

                throw new IllegalArgumentException(
                        "HTML file does not contain any receipt records."
                );
            }


            int importedCount = 0;


            long highestReceiptCode =
                    importDataService
                            .getMaxReceiptCode();


            List<Receipt> conflictingReceipts =
                    new ArrayList<>();


            // =================================================
            // READ EACH RECEIPT
            // =================================================

            for (Element receiptList
                    : receiptLists) {


                String receiptCodeValue =
                        getReceiptValue(
                                receiptList,
                                "Receipt ID",
                                "ReceiptID"
                        );


                String date =
                        getReceiptValue(
                                receiptList,
                                "Date"
                        );


                String kind =
                        getReceiptValue(
                                receiptList,
                                "Kind"
                        );


                String sales =
                        getReceiptValue(
                                receiptList,
                                "Sales"
                        );


                String items =
                        getReceiptValue(
                                receiptList,
                                "Items"
                        );


                String company =
                        getReceiptValue(
                                receiptList,
                                "Company"
                        );


                String country =
                        getReceiptValue(
                                receiptList,
                                "Country"
                        );


                String city =
                        getReceiptValue(
                                receiptList,
                                "City"
                        );


                String street =
                        getReceiptValue(
                                receiptList,
                                "Street"
                        );


                String number =
                        getReceiptValue(
                                receiptList,
                                "Number"
                        );


                Long receiptCode =
                        Long.valueOf(
                                receiptCodeValue
                        );


                // =================================================
                // ARTISANSHIP
                // =================================================

                Artisanship artisanship =
                        importDataService
                                .findOrCreateArtisanship(
                                        company,
                                        country,
                                        city,
                                        street,
                                        number
                                );


                // =================================================
                // BUILD RECEIPT
                // =================================================

                Receipt receipt =
                        new Receipt();


                receipt.setReceiptCode(
                        receiptCode
                );


                receipt.setSalesDate(
                        LocalDate.parse(
                                date,
                                DATE_FORMAT
                        )
                );


                receipt.setReceiptType(
                        parseReceiptType(
                                kind
                        )
                );


                receipt.setSalesPrice(
                        new BigDecimal(
                                sales
                        )
                );


                receipt.setSalesAmount(
                        Integer.valueOf(
                                items
                        )
                );


                receipt.setRepresentative(
                        representative
                );


                receipt.setArtisanship(
                        artisanship
                );


                // =================================================
                // DUPLICATE / COLLISION
                // =================================================

                Receipt existing =
                        importDataService
                                .getExistingReceipt(
                                        receiptCode
                                );


                /*
                 * Code does not exist.
                 */
                if (existing == null) {

                    importDataService
                            .saveReceipt(
                                    receipt
                            );


                    highestReceiptCode =
                            Math.max(
                                    highestReceiptCode,
                                    receiptCode
                            );


                    importedCount++;

                    continue;
                }


                /*
                 * Same code and same data.
                 * True duplicate -> skip.
                 */
                if (importDataService
                        .sameReceipt(
                                existing,
                                receipt
                        )) {

                    continue;
                }


                /*
                 * Same code but different data.
                 * Save later with new code.
                 */
                conflictingReceipts
                        .add(receipt);
            }


            // =================================================
            // SAVE CONFLICTING RECEIPTS
            // =================================================

            for (Receipt receipt
                    : conflictingReceipts) {


                highestReceiptCode++;


                receipt.setReceiptCode(
                        highestReceiptCode
                );


                importDataService
                        .saveReceipt(
                                receipt
                        );


                importedCount++;
            }


            return importedCount;


        } catch (IllegalArgumentException e) {

            throw e;


        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Could not parse HTML file: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // =========================================================
    // READ FIELD FROM RECEIPT <ul>
    // =========================================================

    private String getReceiptValue(
            Element receiptList,
            String... possibleNames) {


        Elements items =
                receiptList.select(
                        "li"
                );


        for (Element item : items) {

            String text =
                    item.text().trim();


            int separator =
                    text.indexOf(':');


            if (separator == -1) {
                continue;
            }


            String fieldName =
                    text.substring(
                            0,
                            separator
                    ).trim();


            String value =
                    text.substring(
                            separator + 1
                    ).trim();


            for (String possibleName
                    : possibleNames) {


                if (fieldName
                        .equalsIgnoreCase(
                                possibleName
                        )) {


                    if (value.isBlank()) {

                        throw new IllegalArgumentException(
                                "Empty HTML field: "
                                        + possibleName
                        );
                    }


                    return value;
                }
            }
        }


        throw new IllegalArgumentException(
                "Missing HTML field: "
                        + String.join(
                                " / ",
                                possibleNames
                        )
        );
    }


    // =========================================================
    // RECEIPT TYPE
    // =========================================================

    private ReceiptType parseReceiptType(
            String value) {


        return switch (
                value.trim()
                     .toLowerCase()
        ) {


            case "trousers" ->
                    ReceiptType.TROUSERS;


            case "skirts" ->
                    ReceiptType.SKIRTS;


            case "blouses",
                 "blouse",
                 "shirts",
                 "shirt" ->

                    ReceiptType.BLOUSES;


            case "coats",
                 "coat" ->

                    ReceiptType.COATS;


            default ->

                    throw new IllegalArgumentException(
                            "Unknown receipt type: "
                                    + value
                    );
        };
    }
}