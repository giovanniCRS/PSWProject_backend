package pswproject.pswproject.entities;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

//lombok
@Getter
@Setter
@ToString
@EqualsAndHashCode

//java.persistance mappa l'entità su una tabella del database.
@Entity
@Table(name= "carrello")
public class Carrello implements Serializable{ //tutti gli oggetti inviati su rete necessitano di essere serializzabili.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //notazione che lascia al database la decisione sui valori da assegnare.
    @Column(name = "idCarrello", nullable = false)
    private int idCarrello;

    @ManyToOne
    @JoinColumn(name = "utenteAssociato")
    private Utente utenteAssociato; //rappresenta l'identificativo univoco di ogni Carrello presente sul database, che resta legato all'Utente (rientra nello Storico dei suoi acquisti).

    @OneToMany(targetEntity = Acquisto.class, mappedBy = "carrelloAssociato", cascade = CascadeType.MERGE) //relazione 1 a molti : a 1 Carrello corrispondono più acquisti (MERGE prende le colonne e le carica assieme).
    private List<Acquisto> acquisti;                                                                       //1 Carrello a livello logico vuole rappresentare una lista di acquisti, associata a un Utente.

    @Column(name = "indirizzo", nullable = true, length = 255)
    private String indirizzo;

    @Column(name = "numeroDiTelefono", nullable = true, length = 20)
    private String numeroDiTelefono;

    @Column(name = "metodoDiPagamento", nullable = true, length = 50)
    private String metodoDiPagamento;
}
