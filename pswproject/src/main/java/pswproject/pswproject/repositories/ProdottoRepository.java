package pswproject.pswproject.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pswproject.pswproject.entities.Categoria;
import pswproject.pswproject.entities.Prodotto;
import java.util.List;
import java.util.Optional;

//vogliamo rappresentare il livello database del Prodotto.
@Repository
public interface ProdottoRepository extends JpaRepository<Prodotto, Integer>{ //JpaRepository una serie di metodi preconfezionati che rappresentano delle query.
    
    List<Prodotto> findByNome(String nome);

    List<Prodotto> findByCategoria(Categoria categoria);

    Optional<Prodotto> findByIdProdotto(int id);

    Optional<Prodotto> findByEAN(String EAN);

    Page<Prodotto> findByCategoria(Categoria c, Pageable p);

    boolean existsByEAN(String ean);

    boolean existsByIdProdotto(int prodottoAssociato);

    void deleteByIdProdotto(int id);
}
