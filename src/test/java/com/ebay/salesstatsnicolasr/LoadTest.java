package com.ebay.salesstatsnicolasr;

import static org.junit.Assert.assertEquals;

import com.ebay.salesstatsnicolasr.service.StatisticsResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class LoadTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testLoad() throws Exception {
        int secondsToRun = 10;
        int salesThreads = 5;
        String amount = "9.00";
        
        AtomicLong salesRequestCount = new AtomicLong(0);
        AtomicLong salesRequestTimeTotal = new AtomicLong(0);
        
        //run many sales requests in parallel threads
        ExecutorService salesPool = Executors.newFixedThreadPool(salesThreads);
        for (int i = 0; i < salesThreads; i++) {
            salesPool.submit(() -> {
                for (;;) {
                    long before = System.currentTimeMillis();
                    sendSalesRequest(amount);
                    salesRequestCount.incrementAndGet();
                    salesRequestTimeTotal.addAndGet(System.currentTimeMillis() - before);
                }
            });
        }
        
        AtomicLong statisticsRequestCount = new AtomicLong(0);
        AtomicLong statisticsRequestTimeTotal = new AtomicLong(0);
        //run one statistics request per second
        ExecutorService statisticsPool = Executors.newSingleThreadExecutor();
        statisticsPool.submit(() -> {
            for (;;) {
                long before = System.currentTimeMillis();
                sendStatisticsRequest();
                statisticsRequestCount.incrementAndGet();
                statisticsRequestTimeTotal.addAndGet(System.currentTimeMillis() - before);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        
        salesPool.shutdown();
        salesPool.awaitTermination(secondsToRun, TimeUnit.SECONDS);
        statisticsPool.shutdownNow();
        
        System.out.printf("Service handled %d sales requests in %d seconds.%n", salesRequestCount.longValue(), secondsToRun);
        System.out.println("Sales request time AVG (ms): " + (salesRequestTimeTotal.longValue() / salesRequestCount.doubleValue()));
        System.out.println("Statistic request time AVG (ms): " + (statisticsRequestTimeTotal.longValue() / statisticsRequestCount.doubleValue()));
        
        StatisticsResource statistics = sendStatisticsRequest();
        System.out.println("AVG amount 60s: " + statistics.getAverageAmountPerOrder());
        System.out.println("Total sales amount: " + statistics.getTotalSalesAmount());
        
        //thread safety test
        assertEquals(amount, statistics.getAverageAmountPerOrder());
    }

    private ResponseEntity<Object> sendSalesRequest(String amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded");

        ResponseEntity<Object> response = this.restTemplate.postForEntity(
                "http://localhost:" + port + "/sales?sales_amount="+ amount, new HttpEntity<>(headers),
                null);
        return response;
    }
    
    private StatisticsResource sendStatisticsRequest() {

        return this.restTemplate.getForEntity(
                "http://localhost:" + port + "/statistics", StatisticsResource.class).getBody();
    }
}
