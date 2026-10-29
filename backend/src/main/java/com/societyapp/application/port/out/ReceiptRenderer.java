package com.societyapp.application.port.out;

import com.societyapp.application.model.billing.BillingModels;

public interface ReceiptRenderer {
    byte[] render(BillingModels.Receipt receipt);
}
