package pswproject.pswproject.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCarrelloNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneEmailGiaUtilizzata;
import pswproject.pswproject.Exceptions.EccezioneUtenteNonEsistente;
import pswproject.pswproject.entities.Utente;
import pswproject.pswproject.services.UtenteService;

@RestController                               //indica un endpoint del be al quale poter fare richiesta.
@RequestMapping("/users")                     //specifichiamo un percorso Path specifico per questo endpoint.
@CrossOrigin(origins="http://localhost:4200") //sono permesse richieste al be dal fe a patto che il Path-name abbia questa origine.
public class UtenteController {
    
    @Autowired //rappresenta che l'oggetto UtenteService univoco per il be venga iniettato in questo componente.
    private UtenteService utenteService;

    /*------------------------------------GET Methods------------------------------------*/

    @GetMapping("/all")                //specifichiamo un percorso Path specifico per richieste di tipo GET.
    @PreAuthorize("hasRole('ADMIN')")  //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
	public List<Utente> getAll()       //restituisce la lista di tutti gli Utenti presenti sul database.
    {
		return utenteService.getAll();
	}

    @GetMapping("/byId/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity getUtente(@PathVariable int id) //resituisce una riposta HTTP che può essere corretta (Utente esistente) oppure no.
    {
        try{
            Utente utenteRis = utenteService.getUtente(id);

            return new ResponseEntity(utenteRis, HttpStatus.OK);
        }catch(EccezioneUtenteNonEsistente e){
            return new ResponseEntity("L'utente non esiste!", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/byEmail/{email}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity getUtente(@PathVariable String email) //resituisce una riposta HTTP che può essere corretta (Utente esistente) oppure no.
    {
        try{
            Utente utenteRis = utenteService.getUtente(email);

            return new ResponseEntity(utenteRis, HttpStatus.OK);
        }catch(EccezioneUtenteNonEsistente e){
            return new ResponseEntity("L'utente non esiste!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------POST Methods------------------------------------*/

    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")                         //da questo endpoint permettiamo solo all'Admin di creare un Utente come da lui richiesto.
    public ResponseEntity saveUtente(@RequestBody Utente u) { //resituisce una riposta HTTP che può essere corretta (Utente creato) oppure no.
        try {
            //controlliamo che i campi dell'Utente siano corretti.
            if(
                u.getNome().equals("") || u.getNome()==null || !u.getNome().matches("^[a-zA-Z\\s]+$") || u.getNome().length()<2
                || u.getCognome().equals("") || u.getCognome()==null || !u.getCognome().matches("^[a-zA-Z\\s]+$") || u.getCognome().length()<2
                || u.getEmail().equals("") || u.getEmail()==null || !u.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || u.getPassword().equals("") || u.getPassword()==null || !u.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
            )
                return new ResponseEntity("Utente non valido!", HttpStatus.BAD_REQUEST);

            Utente utenteRis = utenteService.saveUtente(u);

            return new ResponseEntity(utenteRis, HttpStatus.OK);
        } catch (EccezioneEmailGiaUtilizzata e) {
            return new ResponseEntity("Email già in utilizzo!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------PUT Methods------------------------------------*/

    @PutMapping("/{email}") //specifichiamo un percorso Path specifico per richieste di tipo PUT.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity updateUtente(@PathVariable String email, @Valid @RequestBody Utente u) //resituisce una riposta HTTP che può essere corretta (Utente modificato) oppure no.
    {
        try{
            //controlliamo che i campi dell'Utente siano corretti.
            if(
                u.getNome().equals("") || u.getNome()==null || !u.getNome().matches("^[a-zA-Z\\s]+$") || u.getNome().length()<2
                || u.getCognome().equals("") || u.getCognome()==null || !u.getCognome().matches("^[a-zA-Z\\s]+$") || u.getCognome().length()<2
                || u.getEmail().equals("") || u.getEmail()==null || !u.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || u.getPassword().equals("") || u.getPassword()==null || !u.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")
            )
                return new ResponseEntity("Utente non valido!", HttpStatus.BAD_REQUEST);

            Utente utenteRis = utenteService.updateUtente(email, u.getPassword(), u.getNome(), u.getCognome(), u.getAuthorities());

            return new ResponseEntity(utenteRis, HttpStatus.OK);
        }catch (EccezioneUtenteNonEsistente e){
            return new ResponseEntity("L'utente non esiste.", HttpStatus.BAD_REQUEST);
        }
    }



    /*------------------------------------DELETE Methods------------------------------------*/

    @DeleteMapping("/{email}") //specifichiamo un percorso Path specifico per richieste di tipo DELETE.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity deleteUtente(@PathVariable String email){  //resituisce una riposta HTTP che può essere corretta (Utente eliminato) oppure no.
        try{
            Utente eliminato=utenteService.eliminaUtente(email);

            return new ResponseEntity(eliminato, HttpStatus.OK);
        }catch (EccezioneUtenteNonEsistente e){
            return new ResponseEntity("L'utente non esiste.", HttpStatus.BAD_REQUEST);
        } catch (EccezioneAcquistoNonEsistente e) {
            return new ResponseEntity("Uno degli acquisti non esiste.", HttpStatus.BAD_REQUEST);
        } catch (EccezioneCarrelloNonEsistente e) {
            return new ResponseEntity("Uno dei carrelli non esiste.", HttpStatus.BAD_REQUEST);
        }
    }
}
