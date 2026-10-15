package com.kkargas.sales.importer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.model.Representative;
import com.kkargas.sales.service.ImportDataService;

@Component
public class XmlReceiptImporter
        implements ReceiptImporter {

    private final ImportDataService importDataService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy");


    public XmlReceiptImporter(
            ImportDataService importDataService) {

        this.importDataService =
                importDataService;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFile(
            MultipartFile file) {

        try {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory
                            .newInstance();

            /*
             * Basic protection against XXE.
             */
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
            );


            DocumentBuilder builder =
                    factory.newDocumentBuilder();


            Document document =
                    builder.parse(
                            file.getInputStream()
                    );


            document
                    .getDocumentElement()
                    .normalize();


            Element root =
                    document
                            .getDocumentElement();


            if (!root.getTagName()
                    .equalsIgnoreCase("Agent")) {

                throw new IllegalArgumentException(
                        "Invalid XML file. Root element must be <Agent>."
                );
            }


            String representativeName =
                    getRequiredText(
                            root,
                            "Name"
                    );


            String vatNumber =
                    getRequiredText(
                            root,
                            "AFM"
                    );


            Representative representative =
                    importDataService
                            .findOrCreateRepresentative(
                                    representativeName,
                                    vatNumber
                            );


            NodeList receiptNodes =
                    root.getElementsByTagName(
                            "Receipt"
                    );


            int importedCount = 0;

            long highestReceiptCode =
                    importDataService
                            .getMaxReceiptCode();


            List<Receipt> conflictingReceipts =
                    new ArrayList<>();


            for (int i = 0;
                 i < receiptNodes.getLength();
                 i++) {


                Node node =
                        receiptNodes.item(i);


                if (node.getNodeType()
                        != Node.ELEMENT_NODE) {

                    continue;
                }


                Element receiptElement =
                        (Element) node;


                Long receiptCode =
                        Long.valueOf(
                                getRequiredText(
                                        receiptElement,
                                        "ReceiptID"
                                )
                        );


                String date =
                        getRequiredText(
                                receiptElement,
                                "Date"
                        );


                String kind =
                        getRequiredText(
                                receiptElement,
                                "Kind"
                        );


                String sales =
                        getRequiredText(
                                receiptElement,
                                "Sales"
                        );


                String items =
                        getRequiredText(
                                receiptElement,
                                "Items"
                        );


                String company =
                        getRequiredText(
                                receiptElement,
                                "Company"
                        );


                String country =
                        getRequiredText(
                                receiptElement,
                                "Country"
                        );


                String city =
                        getRequiredText(
                                receiptElement,
                                "City"
                        );


                String street =
                        getRequiredText(
                                receiptElement,
                                "Street"
                        );


                String number =
                        getRequiredText(
                                receiptElement,
                                "Number"
                        );


                Artisanship artisanship =
                        importDataService
                                .findOrCreateArtisanship(
                                        company,
                                        country,
                                        city,
                                        street,
                                        number
                                );


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


                Receipt existing =
                        importDataService
                                .getExistingReceipt(
                                        receiptCode
                                );


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

                } else if (
                        !importDataService
                                .sameReceipt(
                                        existing,
                                        receipt
                                )) {

                    conflictingReceipts
                            .add(receipt);
                }
            }


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
                    "Could not parse XML file: "
                            + e.getMessage(),
                    e
            );
        }
    }


    private String getRequiredText(
            Element parent,
            String tagName) {

        NodeList nodes =
                parent.getElementsByTagName(
                        tagName
                );


        if (nodes.getLength() == 0) {

            throw new IllegalArgumentException(
                    "Missing XML element: <"
                            + tagName
                            + ">"
            );
        }


        String value =
                nodes.item(0)
                        .getTextContent()
                        .trim();


        if (value.isBlank()) {

            throw new IllegalArgumentException(
                    "Empty XML element: <"
                            + tagName
                            + ">"
            );
        }


        return value;
    }


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