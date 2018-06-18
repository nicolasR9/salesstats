package com.ebay.salesstatsnicolasr.service;

import com.ebay.salesstatsnicolasr.impl.CheckoutStatisticsCalculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
        Optional<String> salesAmount = request.queryParam("sales_amount");
        if (!salesAmount.isPresent()) {
            return ServerResponse.badRequest().body(BodyInserters
                    .fromObject("sales_amount parameter required"));
        }

        if (!salesAmount.get().matches("\\d+\\.\\d{2}")) {
            return ServerResponse.badRequest().body(BodyInserters
                    .fromObject("Bad number format for sales_amount. Expected e.g. 12.00"));
        }
        
        long amount = Long.parseLong(salesAmount.get().replace(".", ""));
        long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        statisticsCalculator.add(amount, currentTimeSeconds);

        return ServerResponse.accepted().build();
    }
}
