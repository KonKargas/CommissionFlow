package com.kkargas.sales.importer;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ReceiptImporter {

    int importFile(MultipartFile file) throws IOException;

}