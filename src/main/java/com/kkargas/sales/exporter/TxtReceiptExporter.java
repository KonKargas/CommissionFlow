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
public class TxtReceiptExporter implements ReceiptExporter {

    private final ReceiptService receiptService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy");

    public TxtReceiptExporter(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Override
    public String export(Representative representative) {

        validateRepresentative(representative);

        StringBuilder builder = new StringBuilder();

        builder.append("Name: ")
                .append(safeValue(representative.getFullName()))
                .append(System.lineSeparator());

        builder.append("AFM: ")
                .append(safeValue(representative.getVatNumber()))
                .append(System.lineSeparator());

        builder.append("Receipts:")
                .append(System.lineSeparator());

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

            builder.append("Receipt ID: ")
                    .append(receipt.getReceiptCode())
                    .append(System.lineSeparator());

            builder.append("Date: ")
                    .append(receipt.getSalesDate().format(DATE_FORMAT))
                    .append(System.lineSeparator());

            builder.append("Kind: ")
                    .append(formatReceiptType(receipt))
                    .append(System.lineSeparator());

            builder.append("Sales: ")
                    .append(receipt.getSalesPrice())
                    .append(System.lineSeparator());

            builder.append("Items: ")
                    .append(receipt.getSalesAmount())
                    .append(System.lineSeparator());

            builder.append("Company: ")
                    .append(
                            artisanship != null
                                    ? safeValue(artisanship.getArtisanshipName())
                                    : ""
                    )
                    .append(System.lineSeparator());

            builder.append("Country: ")
                    .append(
                            address != null
                                    ? safeValue(address.getCountry())
                                    : ""
                    )
                    .append(System.lineSeparator());

            builder.append("City: ")
                    .append(
                            address != null
                                    ? safeValue(address.getCity())
                                    : ""
                    )
                    .append(System.lineSeparator());

            builder.append("Street: ")
                    .append(
                            address != null
                                    ? safeValue(address.getStreet())
                                    : ""
                    )
                    .append(System.lineSeparator());

            builder.append("Number: ")
                    .append(
                            address != null && address.getNumber() != null
                                    ? address.getNumber()
                                    : ""
                    )
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    @Override
    public String getFileExtension() {
        return "txt";
    }

    @Override
    public String getContentType() {
        return "text/plain";
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

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
