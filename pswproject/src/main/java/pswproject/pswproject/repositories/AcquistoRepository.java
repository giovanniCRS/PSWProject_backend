package pswproject.pswproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pswproject.pswproject.entities.Acquisto;
import pswproject.pswproject.entities.Carrello;
import pswproject.pswproject.entities.Prodotto;
import java.util.List;
import java.util.Optional;

//vogliamo rappresentare il livello database dell'Acquisto.
@Repository
public interface AcquistoRepository extends JpaRepository<Acquisto, Integer>{ //JpaRepository una serie di metodi preconfezionati che rappresentano delle query.

    List<Acquisto> findByCarrelloAssociato(Carrello carrelloAssociato);

    List<Acquisto> findByProdottoVenduto(Prodotto prodottoVenduto);

    Optional<Acquisto> findByIdAcquisto(int idAcquisto);

    boolean existsByIdAcquisto(int id);

    void deleteByIdAcquisto(int idAcquisto);
}
