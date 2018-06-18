package com.ebay.salesstatsnicolasr.service;

import static org.junit.Assert.assertEquals;

import com.ebay.salesstatsnicolasr.model.Statistics;

import org.junit.Test;

public class StatisticsResourceTest {

    @Test
    public void handlesZeroOrderCountCorrectly() {
        StatisticsResource resource = new StatisticsResource(new Statistics(0, 0));
        assertResourceEquals(resource, "0.00", "0.00");
    }

    @Test
    public void resourceFieldsAreCalculatedCorrectly() {
        StatisticsResource resource = new StatisticsResource(new Statistics(1250, 3));
        assertResourceEquals(resource, "12.50", "4.17");
    }

    private void assertResourceEquals(StatisticsResource resource, String expectedTotalSalesAmount,
            String expectedAvgAmountPerOrder) {
        
        assertEquals(expectedTotalSalesAmount, resource.getTotalSalesAmount());
        assertEquals(expectedAvgAmountPerOrder, resource.getAverageAmountPerOrder());
    }

}
