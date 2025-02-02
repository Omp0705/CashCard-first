package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import example.cashcard.models.CashCard;

import org.apache.coyote.Response;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;


import java.io.IOException;
import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {
	@Autowired
	TestRestTemplate testRestTemplate;


	@Test
	void shouldReturnACashCardWhenDataIsSaved(){
		ResponseEntity<String> response =
				testRestTemplate
						.withBasicAuth("om1","abc12")
						.getForEntity("/cashcards/99",String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		Number id = documentContext.read("$.id");
		assertThat(id).isNotNull();

		Double amount  = documentContext.read("$.amount");
		assertThat(amount).isNotNull();

	assertThat(id).isEqualTo(99);
	assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnCashCardWithUnknownId(){
		ResponseEntity<String> response =
				testRestTemplate
						.withBasicAuth("om1","abc12")
						.getForEntity("/cashcards/999", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();

	}

	@DirtiesContext
	@Test
	void shouldCreateANewCashCard(){
		//POST method test
		// the response body will be void (nothing)
		CashCard newCashCard = new CashCard(null,456.99,null);
		ResponseEntity<Void> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.postForEntity("/cashcards/create",newCashCard, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = response.getHeaders().getLocation();

		ResponseEntity<String> newResponse = testRestTemplate
				.withBasicAuth("om1","abc12")
				.getForEntity(locationOfNewCashCard,String.class);
		assertThat(newResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(newResponse.getBody()).isNotEmpty();
		DocumentContext documentContext = JsonPath.parse(newResponse.getBody());

		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(456.99);
	}

	@Test
	void shouldReturnAListOfCashCardsWhenRequested(){
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		int pageLength = documentContext.read("$.length()");
		assertThat(pageLength).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99,100,101);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45,1.0,150.0);


	}
	@Test
	void shouldReturnPageOfCashCards() {
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.getForEntity("/cashcards?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		// [*] wildcard notation
     	JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);


	}
	@Test
	void shouldReturnASortedPageOfCashCards(){
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		// [*] wildcard notation
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.0);
	}

	@Test
	void shouldNotReturnACashCardOnWrongCredentials(){
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("badddy","abc12")
				.getForEntity("/cashcards/1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		ResponseEntity<String> response1 = testRestTemplate
				.withBasicAuth("om1","ab12")
				.getForEntity("/cashcards/1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test
	void shouldNotReturnCashCardToUnAuthorizedPerson(){
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("sara1","1234")
				.getForEntity("/cashcards/99",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCardToUserWhoNotOwnThem(){
		ResponseEntity<String> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.getForEntity("/cashcards/102",String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	@DirtiesContext
	@Test
	void shouldUpdateCashCardIfRequestIsAuthorizedAndAuthenticated(){
		CashCard cashCardUpdate = new CashCard(null,11223.00,null);
		HttpEntity<CashCard> cashCardHttpEntity = new HttpEntity<>(cashCardUpdate);

		ResponseEntity<Void> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.exchange("/cashcards/99", HttpMethod.PUT,cashCardHttpEntity,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> response1 = testRestTemplate
				.withBasicAuth("om1","abc12")
				.getForEntity("/cashcards/99",String.class);
		assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response1.getBody());
		double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(11223.00);

	}

	@DirtiesContext
	@Test
	void shouldDeleteAExistingCashCard(){
		ResponseEntity<Void> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.exchange("/cashcards/99",HttpMethod.DELETE,null,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		ResponseEntity<String> getResponse = testRestTemplate
				.withBasicAuth("om1","abc12")
				.getForEntity("/cashcards/99", String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@DirtiesContext
	@Test
	void shouldNotDeleteACashCardThatDoesNotExist(){
		ResponseEntity<Void> response = testRestTemplate
				.withBasicAuth("om1","abc12")
				.exchange("/cashcards/9999",HttpMethod.DELETE,null,Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//		ResponseEntity<String> getResponse = testRestTemplate
//				.withBasicAuth("om1","abc12")
//				.getForEntity("/cashcards/99", String.class);
//
//		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}




	@Test
	void contextLoads() {
	}

}
