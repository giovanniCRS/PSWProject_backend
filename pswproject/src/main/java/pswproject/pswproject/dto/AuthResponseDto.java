package pswproject.pswproject.dto;

import lombok.Data;
import pswproject.pswproject.entities.Utente;

//lombok
@Data
public class AuthResponseDto{ //rappresenta la risposta di avvenuto Login dal be al fe.

    private Utente utente;   //inviamo i dati dell'Utente (utli al fe).
    private String jwt;      //inviamo un token che ci permette il riconoscimento delle richieste dell'Utente loggato.

    public AuthResponseDto(Utente u, String jwt) {
        this.utente = u;
        this.jwt = jwt;
    }
}
