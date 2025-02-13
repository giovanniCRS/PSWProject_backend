package pswproject.pswproject.dto;


import lombok.Data;

//lombok
@Data
public class RegisterDto{ //rappresenta una richiesta di registrazione Utente proveniente dal fe verso il be.

    //Indichiamo i campi significativi dell'Utente (che necessitano di essere settati), al resto penserà il be.
    private String nome;
    private String cognome;
    private String email;
    private String password;

    public RegisterDto(){
        super();
    }

    public RegisterDto( String nome, String cognome, String email, String password ){
        super();
        this.nome=nome;
        this.cognome=cognome;
        this.email=email;
        this.password=password;
    }
}
