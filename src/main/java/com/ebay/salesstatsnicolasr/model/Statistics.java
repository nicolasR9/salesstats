package com.ebay.salesstatsnicolasr.model;

public class Statistics {

    private long totalSalesAmountCents;
    private long orderCount;

    public Statistics(long totalSalesAmountCents, long orderCount) {
        this.totalSalesAmountCents = totalSalesAmountCents;
        this.orderCount = orderCount;
    }

    public long getTotalSalesAmountCents() {
        return totalSalesAmountCents;
    }

    public long getOrderCount() {
        return orderCount;
    }
}
