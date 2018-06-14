package com.ebay.salesstatsnicolasr.service;

import com.ebay.salesstatsnicolasr.model.Statistics;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SalesStatisticsController {

    @GetMapping("/statistics")
    public Statistics getStatistics() {
        return new Statistics(1, "hallo");
    }

    // curl -v -X POST --header "Content-type:application/x-www-form-urlencoded"  "http://localhost:8080/sales?sales_amount=12.00"
    @PostMapping(path = "/sales", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Void> add(@RequestParam("sales_amount") String salesAmount) {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
