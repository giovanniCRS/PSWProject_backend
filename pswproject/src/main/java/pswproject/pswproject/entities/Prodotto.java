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
@Table(name = "prodotto")
public class Prodotto implements Serializable{ //tutti gli oggetti inviati su rete necessitano di essere serializzabili.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //notazione che lascia al database la decisione sui valori da assegnare.
    @Column(name = "idProdotto", nullable = false)
    private int idProdotto;

    @Basic
    @Column(name = "EAN", nullable = false, length = 13)
    private String EAN; //il codice EAN rappresenta l'identificativo univoco di ogni prodotto presente sul database.

    @Basic
    @Column(name = "quantitaInMagazzino", nullable = false)
    private int quantitaInMagazzino; //può essere 0, poi sarà l'Admin eventualmente ad aggiungere altra scorta.

    @Basic
    @Column(name = "prezzo", nullable = false)
    private int prezzo;

    @Basic
    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Basic
    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @OneToMany(targetEntity = Acquisto.class, mappedBy = "prodottoVenduto", cascade = CascadeType.MERGE) //relazione 1 a molti : 1 prodotto si trova in più acquisti (MERGE prende le colonne e le carica assieme).
    @ToString.Exclude                                                                                    //non vogliamo che gli acquisti vengano stampati e resi noti per ogni prodotto (gli acquisti vengono considerati relativi agli utenti). 
    @JsonIgnore                                                                                          //non possiamo portarci dietro questo campo in quanto l'oggetto JSON sarebbe ciclico, rappresenterebbe la relazione ricorsivamente (in Carrello è presente UtenteAssociato).
    private List<Acquisto> acquisti;

    @ManyToOne
    @JoinColumn(name = "categoria") //rappresenta la relazione molti a 1 : a molti prodotti corrisponde 1 categoria (1 prodotto ha una sola Categoria).
    private Categoria categoria;

    @Version //notazione che rende noto al database se dal momento in cui ha prelevato la risorsa per modificarla un'altra transazione è avvenuta prima che potesse salvarla, in quel caso si annulla quella arrivata dopo.
    @Column(name = "versione", nullable = false)
    @JsonIgnore
    private long versione;
}
