package com.ebay.salesstatsnicolasr.service;

import java.text.NumberFormat;
import java.util.Locale;

import com.ebay.salesstatsnicolasr.model.Statistics;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StatisticsResource {

	private String totalSalesAmount;
	private String averageAmountPerOrder;

	public StatisticsResource() {
	}

	public StatisticsResource(Statistics statistics) {
		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);

		double salesAmount = statistics.getTotalSalesAmountCents() / 100.0;
		this.totalSalesAmount = nf.format(salesAmount);
		this.averageAmountPerOrder = statistics.getOrderCount() == 0 ? "0.00"
				: nf.format(salesAmount / statistics.getOrderCount());
	}

	@JsonProperty("total_sales_amount")
	public String getTotalSalesAmount() {
		return totalSalesAmount;
	}

	@JsonProperty("average_amount_per_order")
	public String getAverageAmountPerOrder() {
		return averageAmountPerOrder;
	}
}
