package com.societyapp.application.port.out;

import com.societyapp.application.model.platformbilling.PlatformBillingModels;
import com.societyapp.domain.model.Society;
import com.societyapp.domain.model.SubscriptionOrder;
import java.util.List;

public interface PlatformInvoiceRenderer {
    byte[] render(
            SubscriptionOrder order,
            Society society,
            List<PlatformBillingModels.PricingLine> pricingBreakdown);
}
