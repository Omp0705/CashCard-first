package example.cashcard;

import example.cashcard.models.CashCard;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;



@JsonTest
public class CashCardJsonTest {
    @Autowired
    private JacksonTester<CashCard> json;
    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;

    @BeforeEach
    void setup(){
        cashCards = Arrays.array(
                new CashCard(99L,123.45,"om1"),
                new CashCard(100L,1.0,"om1"),
                new CashCard(101L,150.0,"om1")
        );
    }

    @Test
    void cashCardSerializaionTest() throws IOException{
        CashCard cashCard = new CashCard(99L,123.45,"om1");

        assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
    }

    @Test
    void cashCardDeserializationTest() throws IOException{
        String expected = """
                {
                    "id":99,
                    "amount":123.45,
                    "owner": "om1"
                }
                """;
        assertThat(json.parse(expected))
                .isEqualTo(new CashCard(99L, 123.45,"om1"));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }
    @Test
    void allCashCardsSerializationTest() throws IOException{
            assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");

    }

    @Test
    void allCashCardsListDeserializationTest() throws IOException{
        String expected = """
                [
                    {"id": 99,"amount": 123.45,"owner": "om1"},
                    {"id": 100,"amount": 1.0,"owner": "om1"},
                    {"id": 101,"amount": 150.0,"owner": "om1"}
                ]
                """;
        assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }
}
