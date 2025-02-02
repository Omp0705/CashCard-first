package example.cashcard.controllers;

import example.cashcard.models.CashCard;
import example.cashcard.repositories.CashCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

// controller class
@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    //test endpoint
    @GetMapping("/test")
    private String sayHello(){
        return "Hello";
    }

//    Get Method
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> getCashCards(@PathVariable Long requestedId, Principal principal){
        Optional<CashCard> cashCardOptional = Optional.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
        if(cashCardOptional.isPresent()){
            return ResponseEntity.ok(cashCardOptional.get());
        }else{
            return ResponseEntity.notFound().build();
        }

    }
//    POST Method
    @PostMapping("/create")
    private ResponseEntity<Void> createNewCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb,Principal principal){
        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfCashCard = ucb.path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id()).toUri();
        return ResponseEntity.created(locationOfCashCard).build();
    }
    // get-all
    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable,Principal principal) {
        Page<CashCard> page = (Page<CashCard>) cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC,"amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> updateCashCard(@PathVariable Long requestedId,@RequestBody CashCard cashCardUpdate,Principal principal){
        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId,principal.getName());
        CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
        cashCardRepository.save(updatedCashCard);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId,Principal principal){
        CashCard deleteCashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
        if(deleteCashCard != null){
            cashCardRepository.delete(deleteCashCard);
            return ResponseEntity.noContent().build();
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }





}
