package pswproject.pswproject.entities;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

//lombok
@Getter
@Setter
@ToString
@EqualsAndHashCode

//java.persistance mappa l'entità su una tabella del database.
@Entity
@Table(name = "categoria")
public class Categoria implements Serializable{ //tutti gli oggetti inviati su rete necessitano di essere serializzabili.

    public Categoria(){}

    public Categoria(String nome){
        this.nome=nome;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //notazione che lascia al database la decisione sui valori da assegnare.
    @Column(name = "idCategoria", nullable = false)
    private int idCategoria;

    @Basic
    @Column(name = "nome", nullable = false, length = 50)
    private String nome; //rappresenta l'identificativo univoco di ogni Categoria presente sul database.

    @OneToMany(targetEntity = Prodotto.class, mappedBy = "categoria", cascade = CascadeType.MERGE) //relazione 1 a molti : a 1 Categoria corrispondono più prodotti (MERGE prende le colonne e le carica assieme).
    @JsonIgnore
    private List<Prodotto> prodotti;
}
