package com.ebay.salesstatsnicolasr.service;

import com.ebay.salesstatsnicolasr.impl.CheckoutStatisticsCalculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SalesStatisticsController {
    
    @Autowired
    private CheckoutStatisticsCalculator statisticsCalculator;
    
    @GetMapping("/statistics")
    public StatisticsResource getStatistics() {
        return new StatisticsResource(statisticsCalculator.getStatistics(System.currentTimeMillis()));
    }

    // curl -v -X POST --header "Content-type:application/x-www-form-urlencoded"  "http://localhost:8080/sales?sales_amount=12.00"
    @PostMapping(path = "/sales", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> add(@RequestParam("sales_amount") String salesAmount) {
        if (!salesAmount.matches("\\d+\\.\\d{2}")) {
            return ResponseEntity.badRequest().body("Bad number format for sales_amount. Expected e.g. 12.00");
        }
        long amount = Long.parseLong(salesAmount.replace(".", ""));
        statisticsCalculator.add(amount, System.currentTimeMillis());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
