package pswproject.pswproject.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pswproject.pswproject.entities.Utente;

//vogliamo rappresentare il livello database dell'Utente.
@Repository
public interface UtenteRepository extends JpaRepository<Utente, Integer> { //JpaRepository una serie di metodi preconfezionati che rappresentano delle query.

    Optional<Utente> findByIdUtente(int id);

    List<Utente> findByNome(String nome);

    List<Utente> findByCognome(String cognome);

    List<Utente> findByNomeAndCognome(String nome, String cognome);

    Optional<Utente> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdUtente(int idUtente);

    void deleteByEmail(String email);
}
