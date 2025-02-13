package pswproject.pswproject.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pswproject.pswproject.entities.UtenteSecurity.Role;

//vogliamo rappresentare il livello database del Ruolo.
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> { //JpaRepository una serie di metodi preconfezionati che rappresentano delle query.

    Optional<Role>findByAuthority(String authority);
}