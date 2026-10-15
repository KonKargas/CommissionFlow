package com.kkargas.sales.exporter;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.kkargas.sales.model.Address;
import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.model.Representative;
import com.kkargas.sales.service.ReceiptService;

@Component
public class XmlReceiptExporter implements ReceiptExporter {

    private final ReceiptService receiptService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy");

    public XmlReceiptExporter(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Override
    public String export(Representative representative) {

        validateRepresentative(representative);

        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        xml.append("<Agent>\n\n");

        appendElement(
                xml,
                "Name",
                representative.getFullName(),
                4
        );

        appendElement(
                xml,
                "AFM",
                representative.getVatNumber(),
                4
        );

        xml.append("\n    <Receipts>\n");

        List<Receipt> receipts =
                receiptService.getReceiptsByRepresentative(
                        representative.getRepId()
                );

        for (Receipt receipt : receipts) {

            validateReceipt(receipt);

            Artisanship artisanship =
                    receipt.getArtisanship();

            Address address =
                    artisanship != null
                            ? artisanship.getArtisanshipAddress()
                            : null;

            xml.append("\n        <Receipt>\n\n");

            appendElement(
                    xml,
                    "ReceiptID",
                    receipt.getReceiptCode(),
                    12
            );

            appendElement(
                    xml,
                    "Date",
                    receipt.getSalesDate().format(DATE_FORMAT),
                    12
            );

            appendElement(
                    xml,
                    "Kind",
                    formatReceiptType(receipt),
                    12
            );

            appendElement(
                    xml,
                    "Sales",
                    receipt.getSalesPrice(),
                    12
            );

            appendElement(
                    xml,
                    "Items",
                    receipt.getSalesAmount(),
                    12
            );

            appendElement(
                    xml,
                    "Company",
                    artisanship != null
                            ? artisanship.getArtisanshipName()
                            : null,
                    12
            );

            appendElement(
                    xml,
                    "Country",
                    address != null
                            ? address.getCountry()
                            : null,
                    12
            );

            appendElement(
                    xml,
                    "City",
                    address != null
                            ? address.getCity()
                            : null,
                    12
            );

            appendElement(
                    xml,
                    "Street",
                    address != null
                            ? address.getStreet()
                            : null,
                    12
            );

            appendElement(
                    xml,
                    "Number",
                    address != null
                            ? address.getNumber()
                            : null,
                    12
            );

            xml.append("\n        </Receipt>\n");
        }

        xml.append("\n    </Receipts>\n\n");
        xml.append("</Agent>\n");

        return xml.toString();
    }

    private void appendElement(
            StringBuilder builder,
            String name,
            Object value,
            int indentation) {

        builder.append(" ".repeat(indentation));

        builder.append("<")
                .append(name)
                .append(">");

        builder.append(
                escapeXml(
                        safeValue(value)
                )
        );

        builder.append("</")
                .append(name)
                .append(">\n");
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String formatReceiptType(Receipt receipt) {
        return switch (receipt.getReceiptType()) {
            case TROUSERS -> "Trousers";
            case SKIRTS -> "Skirts";
            case BLOUSES -> "Blouses";
            case COATS -> "Coats";
        };
    }

    private void validateRepresentative(Representative representative) {
        if (representative == null) {
            throw new IllegalArgumentException(
                    "Representative does not exist."
            );
        }

        if (representative.getFullName() == null
                || representative.getVatNumber() == null) {
            throw new IllegalStateException(
                    "Representative contains incomplete data."
            );
        }
    }

    private void validateReceipt(Receipt receipt) {
        if (receipt == null
                || receipt.getReceiptCode() == null
                || receipt.getSalesDate() == null
                || receipt.getReceiptType() == null
                || receipt.getSalesPrice() == null
                || receipt.getSalesAmount() == null) {
            throw new IllegalStateException(
                    "Receipt contains incomplete sales data."
            );
        }
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }
}
