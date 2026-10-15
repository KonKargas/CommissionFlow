package com.kkargas.sales.exporter;

import com.kkargas.sales.model.Representative;

public interface ReceiptExporter {

    String export(Representative representative);

    String getFileExtension();

    String getContentType();
}