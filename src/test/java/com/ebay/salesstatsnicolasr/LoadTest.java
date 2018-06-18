package com.ebay.salesstatsnicolasr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.ebay.salesstatsnicolasr.service.StatisticsResource;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Category(SlowTests.class)
public class LoadTest {

	@LocalServerPort
	private int port;

	private WebClient client;

	@Category(SlowTests.class)
	@Test
	public void serverCanHandleExpectedLoadAndSupportsMultithreading() throws Exception {
		client = WebClient.create("http://localhost:" + port);

		int secondsToRun = 90;
		int salesThreads = 5;
		String amount = "9.00";

		// run many sales requests in parallel threads
		ExecutorService salesPool = Executors.newFixedThreadPool(salesThreads);
		for (int i = 0; i < salesThreads; i++) {
			salesPool.submit(() -> {
				for (;;) {
					sendSalesRequest(amount);
				}
			});
		}

		AtomicLong statisticsRequestCount = new AtomicLong(0);
		AtomicLong statisticsRequestTimeTotal = new AtomicLong(0);
		// run one statistics request per second
		ExecutorService statisticsPool = Executors.newSingleThreadExecutor();
		statisticsPool.submit(() -> {
			for (;;) {
				long before = System.currentTimeMillis();
				sendStatisticsRequest();
				statisticsRequestCount.incrementAndGet();
				statisticsRequestTimeTotal.addAndGet(System.currentTimeMillis() - before);
				sleepOneSecond();
			}
		});

		salesPool.shutdown();
		salesPool.awaitTermination(secondsToRun, TimeUnit.SECONDS);
		statisticsPool.shutdownNow();

		Mono<ClientResponse> statsMono = sendStatisticsRequest();
		StatisticsResource stats = statsMono.flatMap(res -> res.bodyToMono(StatisticsResource.class)).block();

		// thread safety test
		assertEquals(amount, stats.getAverageAmountPerOrder());

		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		long salesRequestCount = (long) (nf.parse(stats.getTotalSalesAmount()).doubleValue()
				/ nf.parse(amount).doubleValue());

		System.out.printf("Service handled %d sales requests in %d seconds.%n", salesRequestCount, secondsToRun);
		// expected load
		assertTrue(salesRequestCount / secondsToRun > 250000 / 60.0);

		double statsRequestAvg = statisticsRequestTimeTotal.longValue() / statisticsRequestCount.doubleValue();
		System.out.println("Statistic request time AVG (ms): " + statsRequestAvg);

		assertTrue(statsRequestAvg < 100); // in ms
	}

	private void sleepOneSecond() {
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void sendSalesRequest(String amount) {
		client.post().uri("/sales").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("sales_amount", amount)).exchange().block();
	}

	private Mono<ClientResponse> sendStatisticsRequest() {

		return client.get().uri("/statistics").exchange();
	}
}
