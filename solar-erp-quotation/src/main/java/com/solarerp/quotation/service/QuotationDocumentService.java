package com.solarerp.quotation.service;

import java.util.UUID;

public interface QuotationDocumentService {
    byte[] generateDocx(UUID quotationId);
}