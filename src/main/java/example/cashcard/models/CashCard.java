package example.cashcard.models;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


public record CashCard(@Id Long id, Double amount,String owner){

}