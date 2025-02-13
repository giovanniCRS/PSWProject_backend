package pswproject.pswproject.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pswproject.pswproject.entities.Categoria;

//vogliamo rappresentare il livello database della Categoria.
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer>{ //JpaRepository una serie di metodi preconfezionati che rappresentano delle query.

    Optional<Categoria> findByIdCategoria(int id);

    Optional<Categoria> findByNome(String nome);

    boolean existsByNome(String nome);

    boolean existsByIdCategoria(int idCategoria);

    void deleteByIdCategoria(int id);

    void deleteByNome(String nome);
}
