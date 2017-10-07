package com.home.you.bookstore.results;

import java.math.BigDecimal;

class PurchaseResultImpl implements PurchaseResult {
    private final BigDecimal totalPrice;
    private final Statuses statuses;

    private PurchaseResultImpl(BuilderImpl builder) {
        this.totalPrice = builder.totalPrice;
        this.statuses = builder.statuses;
    }

    @Override
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    @Override
    public Statuses getStatuses() {
        return statuses;
    }

    static PurchaseResult.Builder newBuilder() {
        return new BuilderImpl();
    }

    private static class BuilderImpl implements PurchaseResult.Builder {
        private BigDecimal totalPrice;
        private Statuses statuses;

        private BuilderImpl() {}

        @Override
        public PurchaseResult.Builder withTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        @Override
        public PurchaseResult.Builder withStatuses(Statuses statuses) {
            this.statuses = statuses;
            return this;
        }

        @Override
        public PurchaseResult build() {
            return new PurchaseResultImpl(this);
        }
    }
}
