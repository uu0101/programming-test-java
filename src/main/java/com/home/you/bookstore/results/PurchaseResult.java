package com.home.you.bookstore.results;

import java.math.BigDecimal;

public interface PurchaseResult {
    BigDecimal getTotalPrice();

    Statuses getStatuses();

    static Builder builder() {
        return PurchaseResultImpl.newBuilder();
    }

    interface Builder {
        Builder withTotalPrice(BigDecimal totalPrice);

        Builder withStatuses(Statuses statueses);

        PurchaseResult build();
    }
}
