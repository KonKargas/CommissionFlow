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
public class HtmlReceiptExporter implements ReceiptExporter {

    private final ReceiptService receiptService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy");

    public HtmlReceiptExporter(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Override
    public String export(Representative representative) {

        validateRepresentative(representative);

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="UTF-8">
                    <title>Receipts List</title>
                </head>

                <body>

                    <ul class="representative">
                """);

        html.append("""
                        <li data-field="name">
                            %s
                        </li>
                """.formatted(
                escapeHtml(
                        safeValue(
                                representative.getFullName()
                        )
                )
        ));

        html.append("""
                        <li data-field="afm">
                            %s
                        </li>
                    </ul>

                    <h2>Receipts</h2>

                    <section class="receipt">

                """.formatted(
                escapeHtml(
                        safeValue(
                                representative.getVatNumber()
                        )
                )
        ));

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

            html.append("""
                        <ul>
                """);

            appendItem(
                    html,
                    "Receipt ID",
                    receipt.getReceiptCode()
            );

            appendItem(
                    html,
                    "Date",
                    receipt.getSalesDate().format(DATE_FORMAT)
            );

            appendItem(
                    html,
                    "Kind",
                    formatReceiptType(receipt)
            );

            appendItem(
                    html,
                    "Sales",
                    receipt.getSalesPrice()
            );

            appendItem(
                    html,
                    "Items",
                    receipt.getSalesAmount()
            );

            appendItem(
                    html,
                    "Company",
                    artisanship != null
                            ? artisanship.getArtisanshipName()
                            : null
            );

            appendItem(
                    html,
                    "Country",
                    address != null
                            ? address.getCountry()
                            : null
            );

            appendItem(
                    html,
                    "City",
                    address != null
                            ? address.getCity()
                            : null
            );

            appendItem(
                    html,
                    "Street",
                    address != null
                            ? address.getStreet()
                            : null
            );

            appendItem(
                    html,
                    "Number",
                    address != null
                            ? address.getNumber()
                            : null
            );

            html.append("""
                        </ul>

                """);
        }

        html.append("""
                    </section>

                </body>

                </html>
                """);

        return html.toString();
    }

    private void appendItem(
            StringBuilder builder,
            String name,
            Object value) {

        builder.append("        <li>")
                .append(name)
                .append(": ")
                .append(
                        escapeHtml(
                                safeValue(value)
                        )
                )
                .append("</li>\n");
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
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
        return "html";
    }

    @Override
    public String getContentType() {
        return "text/html";
    }
}
