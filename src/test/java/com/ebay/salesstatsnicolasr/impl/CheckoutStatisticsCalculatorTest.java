package com.ebay.salesstatsnicolasr.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ebay.salesstatsnicolasr.model.Statistics;

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
		calculator.add(20, 1);
		calculator.add(10, 20);
		assertStatistics(2, 30, 60);
	}

	@Test
	public void addsExpire() {
		calculator.add(20, 0);
		calculator.add(10, 2);
		assertStatistics(1, 10, 61);
	}

	private void assertStatistics(int expectedOrderCount, int expectedSalesAmount, long timeMillisToUse) {
		Statistics stats = calculator.getStatistics(timeMillisToUse);
		assertEquals(expectedOrderCount, stats.getOrderCount());
		assertEquals(expectedSalesAmount, stats.getTotalSalesAmountCents());
	}
}
