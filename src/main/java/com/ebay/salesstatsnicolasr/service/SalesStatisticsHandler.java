package com.ebay.salesstatsnicolasr.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.ebay.salesstatsnicolasr.impl.CheckoutStatisticsCalculator;

import reactor.core.publisher.Mono;

@Component
public class SalesStatisticsHandler {

	@Autowired
	private CheckoutStatisticsCalculator statisticsCalculator;

	public Mono<ServerResponse> statistics(ServerRequest request) {
		long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

		StatisticsResource statisticsResource = new StatisticsResource(
				statisticsCalculator.getStatistics(currentTimeSeconds));
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(statisticsResource));
	}

	public Mono<ServerResponse> sales(ServerRequest request) {
		return request
				.formData()
				.map(MultiValueMap::toSingleValueMap)
				.flatMap(formData -> {
					String salesAmount = formData.get("sales_amount");
					String validationMessage = validate(salesAmount);
					if (validationMessage != null) {
						return ServerResponse.badRequest().body(BodyInserters.fromObject(validationMessage));
					}
					addSaleToCalculator(salesAmount);
					return ServerResponse.accepted().build();
				});
	}

	private void addSaleToCalculator(String salesAmount) {
		long amount = Long.parseLong(salesAmount.replace(".", ""));
		long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		statisticsCalculator.add(amount, currentTimeSeconds);
	}

	private String validate(String salesAmount) {
		if (salesAmount == null) {
			return "sales_amount parameter required";
		}

		if (!salesAmount.matches("\\d+\\.\\d{2}")) {
			return "Bad number format for sales_amount. Expected e.g. 12.00";
		}
		return null;
	}
}
