package com.ebay.salesstatsnicolasr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.reactive.function.BodyInserters;

import com.ebay.salesstatsnicolasr.service.StatisticsResource;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

	@Autowired
	private WebTestClient client;

	@Test
	public void salesWithoutAmountGetRejected() {
		client.post().uri("/sales").contentType(MediaType.APPLICATION_FORM_URLENCODED).exchange().expectStatus()
				.isBadRequest();
	}

	@Test
	public void salesWithWrongAmountFormatGetRejected() {
		sendSalesRequest("12,4").expectStatus().isBadRequest();
	}

	@Test
	public void salesAndStatisticsCallsWorkCorrectly() {
		sendSalesRequest("12.23").expectStatus().isAccepted();
		sendSalesRequest("0.88").expectStatus().isAccepted();

		StatisticsResource actualStatistics = sendStatisticsRequest().expectStatus().isOk()
				.expectBody(StatisticsResource.class).returnResult().getResponseBody();
		assertEquals("13.11", actualStatistics.getTotalSalesAmount());
		assertEquals("6.55", actualStatistics.getAverageAmountPerOrder());
	}

	private ResponseSpec sendSalesRequest(String amount) {
		return client.post().uri("/sales").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("sales_amount", amount)).exchange();
	}

	private ResponseSpec sendStatisticsRequest() {
		return client.get().uri("/statistics").accept(MediaType.APPLICATION_JSON).exchange();
	}
}
