package com.ebay.salesstatsnicolasr.service;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SalesStatisticsRouter {

    @Bean
    public RouterFunction<ServerResponse> route(SalesStatisticsHandler salesStatisticsHandler) {

        return RouterFunctions.route(GET("/statistics"), salesStatisticsHandler::statistics)
                .andRoute(
                        POST("/sales").and(contentType(
                                MediaType.parseMediaType("application/x-www-form-urlencoded"))),
                        salesStatisticsHandler::sales);
    }

}
