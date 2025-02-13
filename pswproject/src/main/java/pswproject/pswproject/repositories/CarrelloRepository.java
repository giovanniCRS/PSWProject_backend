package pswproject.pswproject.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pswproject.pswproject.entities.Carrello;
import pswproject.pswproject.entities.Utente;

//vogliamo rappresentare il livello database del Carrello.
@Repository
public interface CarrelloRepository extends JpaRepository<Carrello, Integer>{ //JpaRepository una serie di metodi preconfezionati che rappresentano delle query.

    List<Carrello> findByUtenteAssociato(Utente byId);

    Optional<Carrello> findByIdCarrello(int idCarrello);

    boolean existsByIdCarrello(int carrelloAssociato);

    void deleteByIdCarrello(int id);
}