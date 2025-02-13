package pswproject.pswproject.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCarrelloNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneUtenteNonEsistente;
import pswproject.pswproject.dto.CarrelloDto;
import pswproject.pswproject.entities.Acquisto;
import pswproject.pswproject.entities.Carrello;
import pswproject.pswproject.services.CarrelloService;

@RestController                               //indica un endpoint del be al quale poter fare richiesta.
@RequestMapping("/carts")                     //specifichiamo un percorso Path specifico per questo endpoint.
@CrossOrigin(origins="http://localhost:4200") //sono permesse richieste al be dal fe a patto che il Path-name abbia questa origine.
public class CarrelloController {
    
    @Autowired //rappresenta che l'oggetto CarrelloService univoco per il be venga iniettato in questo componente.
    private CarrelloService carrelloService;

    /*------------------------------------GET Methods------------------------------------*/

    @GetMapping("/all")                 //specifichiamo un percorso Path specifico per richieste di tipo GET.
    @PreAuthorize("hasRole('ADMIN')")   //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public List<Carrello> getCarrelli() //restituisce la lista di tutti i Carrelli presenti sul database.
    {
        return carrelloService.getCarrelli();
    }

    @GetMapping("/byId/{idCarrello}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity getCarrelliById(@PathVariable int idCarrello) //restituisce la lista di tutti i Carrelli presenti sul database, in base all'id.
    {
        Optional<Carrello> o = carrelloService.getCarrelliById(idCarrello);

        if(!o.isPresent())
            return new ResponseEntity("Carrello non esistente", HttpStatus.BAD_REQUEST);
        else
        {
            Carrello risCarrello = o.get();
            return new ResponseEntity(risCarrello, HttpStatus.OK);
        }
    }

    @GetMapping("/byUser/{email}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<CarrelloDto> getCarrelliByUtente(@PathVariable String email) //restituisce la lista di tutti i Carrelli presenti sul database, in base all'UtenteAssociato.
    {
        try{
            List<Carrello> l = carrelloService.getCarrelliByUtente(email);
            List<CarrelloDto> ris = new ArrayList<>();

            //convertiamo l'Arraylist in un Array in quanto CarrelloDto lato fe necessita di un Array[Acquisto].
            for(Carrello c : l)
            {
                Acquisto[] arr = new Acquisto[c.getAcquisti().size()];
                for(int i=0; i<c.getAcquisti().size(); i++)
                    arr[i] = c.getAcquisti().get(i);
                ris.add(new CarrelloDto(c.getUtenteAssociato().getEmail(), arr, c.getIndirizzo(), c.getNumeroDiTelefono(), c.getMetodoDiPagamento()));
            }
            return ris;
        }catch(EccezioneUtenteNonEsistente e){
            return null;
        }
    }

    /*------------------------------------POST Methods------------------------------------*/

    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasAnyRole('USER','ADMIN')")                                    //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity createCarrello(@Valid @RequestBody CarrelloDto carrello) //resituisce una riposta HTTP che può essere corretta (Carrello creato) oppure no.
    {
        try{
            //controlliamo che i campi del CarrelloDto siano corretti.
            if( carrello.getAcquisti().length==0 || carrello.getAcquisti()==null 
                || carrello.getEmail()==null || carrello.getEmail()=="" || !carrello.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || carrello.getIndirizzo()==null || carrello.getIndirizzo()==""
                || carrello.getMetodoDiPagamento()==null || carrello.getMetodoDiPagamento()==""
                || carrello.getNumeroDiTelefono()==null || carrello.getMetodoDiPagamento()=="" 
            )
                return new ResponseEntity<>("Carrello non valido!", HttpStatus.BAD_REQUEST);

            Carrello c = carrelloService.addCarrello(carrello);
            return new ResponseEntity(c, HttpStatus.OK);
        }catch(EccezioneUtenteNonEsistente e){
            return new ResponseEntity("L'utente {"+carrello.getEmail()+"} non esiste!", HttpStatus.BAD_REQUEST);
        }catch(EccezioneCarrelloNonEsistente e){
            return new ResponseEntity("Il carrello non esiste!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------PUT Methods------------------------------------*/

    @PutMapping("/{id}")                                                                               //specifichiamo un percorso Path specifico per richieste di tipo PUT.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")                                                                  //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity updateCarrello(@PathVariable int id, @Valid @RequestBody Carrello carrello){ //resituisce una riposta HTTP che può essere corretta (Carrello aggiornato) oppure no.
        try {
            if( carrello.getAcquisti().size()==0 || carrello.getAcquisti()==null 
                || carrello.getUtenteAssociato().getEmail()==null || carrello.getUtenteAssociato().getEmail()=="" || !carrello.getUtenteAssociato().getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || carrello.getIndirizzo()==null || carrello.getIndirizzo()==""
                || carrello.getMetodoDiPagamento()==null || carrello.getMetodoDiPagamento()==""
                || carrello.getNumeroDiTelefono()==null || carrello.getMetodoDiPagamento()=="" || !carrello.getMetodoDiPagamento().matches("^((00|\\+)39?[\\. ]??)??3\\d{2}[\\. ]??\\d{6,7}$"))
                
                return new ResponseEntity<>("Carrello non valido!", HttpStatus.BAD_REQUEST);
            carrelloService.updateCarrello(id, carrello);
            return new ResponseEntity("Carrello: "+id+" aggiornato!", HttpStatus.OK);
        } catch (EccezioneCarrelloNonEsistente e) {
            return new ResponseEntity("Il carrello non esiste!", HttpStatus.BAD_REQUEST);
        } catch (EccezioneUtenteNonEsistente e) {
            return new ResponseEntity("L'utente non esiste!", HttpStatus.BAD_REQUEST);
        }
    }

    //Delete Methods-------------------------
    @DeleteMapping("/{id}")                                    //specifichiamo un percorso Path specifico per richieste di tipo DELETE.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")                          //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity deleteCarrello(@PathVariable int id) //resituisce una riposta HTTP che può essere corretta (Carrello eliminato) oppure no.
    {
        try {
            carrelloService.deleteCarrello(id);
            return new ResponseEntity("Carrello con id:"+id+" eliminato!", HttpStatus.OK);
        } catch (EccezioneCarrelloNonEsistente e) {
            return new ResponseEntity("Il carrello non esiste!", HttpStatus.BAD_REQUEST);
        } catch (EccezioneAcquistoNonEsistente e) {
            return new ResponseEntity("Acquisto inesistente!", HttpStatus.BAD_REQUEST);
        }
    }
}
