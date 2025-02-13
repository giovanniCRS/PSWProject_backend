package pswproject.pswproject.dto;

import lombok.Data;
import pswproject.pswproject.entities.Acquisto;

//lombok
@Data
public class CarrelloDto{  //rappresenta una richiesta di creazione di un nuovo Carrello (acquisiti che stanno per avvenire ora) proveniente dal fe verso il be.

    //Indichiamo i campi significativi del Carrello (che necessitano di essere settati), al resto penserà il be.
    private String email;
    private Acquisto[] acquisti; //acquisti fittizi, sicuramente saranno settati in maniera corretta l'EAN corrispondente al Prodotto e la quantità, al resto penserà il be.
    private String indirizzo;
    private String numeroDiTelefono;
    private String metodoDiPagamento;

    public CarrelloDto(){
        super();
    }

    public CarrelloDto( String email, Acquisto[] acquisti, String indirizzo, String numeroDiTelefono, String metodoDiPagamento){
        super();
        this.email=email;
        this.acquisti=acquisti;
        this.indirizzo=indirizzo;
        this.numeroDiTelefono=numeroDiTelefono;
        this.metodoDiPagamento=metodoDiPagamento;
    }
}