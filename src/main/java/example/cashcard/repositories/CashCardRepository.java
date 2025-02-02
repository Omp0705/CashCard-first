package example.cashcard.repositories;

import example.cashcard.models.CashCard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface CashCardRepository extends CrudRepository<CashCard,Long>,
        PagingAndSortingRepository<CashCard,Long> {

    // to get a specific cashcard from id and owner(username)
    CashCard findByIdAndOwner(Long id,String owner);

    //to get all the cards that a persone owe
    Page<CashCard> findByOwner(String owner,PageRequest pageRequest);


}
