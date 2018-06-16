package com.ebay.salesstatsnicolasr.impl;

import static org.junit.Assert.assertEquals;

import com.ebay.salesstatsnicolasr.model.Statistics;

import org.junit.Test;

public class CheckoutStatisticsCalculatorTest {
    
    private CheckoutStatisticsCalculator calculator = new CheckoutStatisticsCalculator();
    
    @Test
    public void initialStatisticsAreZero() {
        assertStatistics(0, 0, 0);
    }

    @Test
    public void statisticsAfterOneAdd() {
        calculator.add(20, 1);
        assertStatistics(1, 20, 1);
    }
    
    @Test
    public void statisticsAfterTwoAdds() {
        calculator.add(20, 1L);
        calculator.add(10, 20000L);
        assertStatistics(2, 30, 60000L);
    }
    
    @Test
    public void addsExpire() {
        calculator.add(20, 1L);
        calculator.add(10, 1001L);
        assertStatistics(1, 10, 61000L);
    }
    
    private void assertStatistics(int expectedOrderCount, int expectedSalesAmount, long timeMillisToUse) {
        Statistics stats = calculator.getStatistics(timeMillisToUse);
        assertEquals(expectedOrderCount, stats.getOrderCount());
        assertEquals(expectedSalesAmount, stats.getTotalSalesAmountCents());
    }
}
