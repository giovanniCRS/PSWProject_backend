package pswproject.pswproject.entities;

import java.io.Serializable;
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
@Table(name = "acquisto")
public class Acquisto implements Serializable{ //tutti gli oggetti inviati su rete necessitano di essere serializzabili.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //notazione che lascia al database la decisione sui valori da assegnare.
    @Column(name = "idAcquisto", nullable = false)
    private int idAcquisto;

    @ManyToOne
    @JoinColumn(name = "carrelloAssociato") //rappresenta l'identificativo univoco di ogni Acquisto presente sul database, a livello di be è il Carrello a creare gli acquisti (legati al Carrello che li crea).
    @JsonIgnore                             //non possiamo portarci dietro questo campo in quanto l'oggetto JSON sarebbe ciclico, rappresenterebbe la relazione ricorsivamente (in Carrello è presente lista di acquisti).
    private Carrello carrelloAssociato;

    @ManyToOne(cascade = CascadeType.MERGE) //relazione molti a 1 : a molti acquisti corrisponde 1 prodotto (MERGE prende le colonne e le carica assieme).
    @JoinColumn(name = "prodottoVenduto")
    private Prodotto prodottoVenduto;

    private int prezzovendita;

    @Basic
    @Column(name = "quantita", nullable = false)
    private int quantita; //va a sottrarre quella presente in magazzino (inventario), nel caso di acquisto annullato dall'Admin, essa si torna a sommare con quella in magazzino.
}
