package pswproject.pswproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;

import pswproject.pswproject.Exceptions.EccezioneEmailGiaUtilizzata;
import pswproject.pswproject.Exceptions.EccezioneOperazioneNonValida;
import pswproject.pswproject.Exceptions.EccezioneUtenteNonEsistente;
import pswproject.pswproject.dto.AuthResponseDto;
import pswproject.pswproject.dto.LoginDto;
import pswproject.pswproject.dto.RegisterDto;
import pswproject.pswproject.entities.Utente;
//import bak
import pswproject.pswproject.services.*;

@RestController                               //indica un endpoint del be al quale poter fare richiesta.
@RequestMapping("/auth")                      //specifichiamo un percorso Path specifico per questo endpoint (ACCESSIBILE A TUTTI).
@CrossOrigin(origins="http://localhost:4200") //sono permesse richieste al be dal fe a patto che il Path-name abbia questa origine.
public class AuthenticationController {

    @Autowired //rappresenta che l'oggetto AuthenticationService univoco per il be venga iniettato in questo componente.
    private AuthenticationService authenticationService;
    //modifiche bak
     @Autowired
    private AuthenticationManager authenticationManager; //si occupa dell'autenticazione dell'Utente.

    @Autowired
    private TokenService tokenService;   
    @Autowired
    private PasswordEncoder passwordEncoder ;

    //entrambi gli endpoint sono in POST perché passiamo i dati di login / registrazione.

    @PostMapping("/register")
    @SuppressWarnings({"rawtypes"})
    public ResponseEntity registraUtente(@RequestBody RegisterDto body) throws EccezioneEmailGiaUtilizzata, EccezioneOperazioneNonValida{ //restituisce una riposta HTTP che contiene i dati dell'Utente e un token (Utente accettato) errore altrimenti.
        System.out.println("Received Body: " + body); //log sulla console dove controllo che i dati vengono ricevuti correttamente

        //controlliamo che i campi della Registrazione dell'Utente siano corretti.
        if(
            body.getNome().equals("") || body.getNome()==null || !body.getNome().matches("^[a-zA-Z\\s]+$") || body.getNome().length()<2
            || body.getCognome().equals("") || body.getCognome()==null || !body.getCognome().matches("^[a-zA-Z\\s]+$") || body.getCognome().length()<2
            || body.getEmail().equals("") || body.getEmail()==null || !body.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
            || body.getPassword().equals("") || body.getPassword()==null || !body.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
        )
            return new ResponseEntity<>("l'utente non è valido", HttpStatus.BAD_REQUEST);
 
        try{
            Utente ris = authenticationService.registraUtente(body.getNome(), body.getCognome(), body.getEmail(), body.getPassword());
            boolean match = passwordEncoder.matches(body.getPassword(), ris.getPassword());//in chiaro, criptata
            String t= tokenService.generateJwt(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(ris.getEmail(), body.getPassword())));
            return new ResponseEntity<>(t, HttpStatus.OK);
        }catch(EccezioneEmailGiaUtilizzata e){
            return new ResponseEntity<>("L'utente è già registrato", HttpStatus.BAD_REQUEST);
        }catch(EccezioneOperazioneNonValida e){
            return new ResponseEntity<>("Operazione non valida", HttpStatus.BAD_REQUEST);
        }catch(Exception e){ 
            e.printStackTrace();
        }
        return new ResponseEntity<>("Errore", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    @SuppressWarnings({"rawtypes"})
    public ResponseEntity loginUtente(@RequestBody LoginDto body) throws EccezioneUtenteNonEsistente{ //restituisce una riposta HTTP che contiene i dati dell'Utente e un token (Utente registrato) errore altrimenti.

        //i dati devono essere presenti nel database (altrimenti l'Utente non è registrato).
        try{
            AuthResponseDto ris = authenticationService.loginUtente(body.getEmail(), body.getPassword());
            return new ResponseEntity<>(ris, HttpStatus.OK);
        }catch(EccezioneUtenteNonEsistente e){
            return new ResponseEntity<>("L'utente non è registrato!", HttpStatus.BAD_REQUEST);
        }
    }

}
