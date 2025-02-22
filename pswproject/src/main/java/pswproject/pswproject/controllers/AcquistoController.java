package pswproject.pswproject.controllers;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCarrelloNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneProdottoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneQuantitaInsufficiente;
import pswproject.pswproject.Exceptions.EccezioneQuantitaProdottoNonDisponibile;
import pswproject.pswproject.entities.Acquisto;
import pswproject.pswproject.services.AcquistoService;

@RestController                               //indica un endpoint del be al quale poter fare richiesta.
@RequestMapping("/purchases")                 //specifichiamo un percorso Path specifico per questo endpoint (ACCESSIBILE SOLO DALL'ADMIN).
@CrossOrigin(origins="http://localhost:4200") //sono permesse richieste al be dal fe a patto che il Path-name abbia questa origine.
public class AcquistoController {
    
    @Autowired //rappresenta che l'oggetto AcquistoService univoco per il be venga iniettato in questo componente.
    private AcquistoService acquistoService;

    /*------------------------------------GET Methods------------------------------------*/

    @GetMapping("/all")                 //specifichiamo un percorso Path specifico per richieste di tipo GET.
    @PreAuthorize("hasRole('ADMIN')")
    public List<Acquisto> getAcquisti() //restituisce la lista di tutti gli Acquisti presenti sul database.
    {
        return acquistoService.getAcquisti();
    }

    @GetMapping("/byCart/{idCarrello}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")                               //unica eccezione,ogni Utente può vedere i propri acquisti.
    public List<Acquisto> getAcquistoByCarrello(@PathVariable int idCarrello) //restituisce la lista di tutti i Prodotti presenti sul database, in base al CarrelloAssociato corrispondente.
    {
        return acquistoService.getAcquistiByCarrello(idCarrello);
    }

    @GetMapping("/byId/{idAcquisto}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity getAcquistoById(@PathVariable int idAcquisto) //restituisce la lista di tutti i Prodotti presenti sul database, in base all'id corrispondente.
    {
        Optional<Acquisto> risAcquisto = acquistoService.getAcquistoById(idAcquisto);
        if(risAcquisto.isPresent())
            return new ResponseEntity(risAcquisto.get(), HttpStatus.OK);
        else
            return new ResponseEntity("Acquisto non esistente", HttpStatus.BAD_REQUEST);
    }

    /*------------------------------------POST Methods------------------------------------*/

    @PostMapping("/{carrelloAssociato}/{EANProd}/{quantita}") //specifichiamo un percorso Path specifico per richieste di tipo POST.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity createAcquisto(@PathVariable int carrelloAssociato, @PathVariable String EANProd, @PathVariable int quantita, @PathVariable int prezzovendita) //resituisce una riposta HTTP che può essere corretta (Acquisto creato) oppure no.
    {
        try{
            Acquisto risAcquisto = acquistoService.createAcquisto(carrelloAssociato, EANProd, quantita, prezzovendita);
            return new ResponseEntity(risAcquisto, HttpStatus.OK);
        }catch(EccezioneCarrelloNonEsistente e){
            return new ResponseEntity("Carrello non esistente!", HttpStatus.BAD_REQUEST);
        }catch(EccezioneProdottoNonEsistente e){
            return new ResponseEntity("Prodotto non esistente!", HttpStatus.BAD_REQUEST);
        }catch(EccezioneQuantitaInsufficiente e){
            return new ResponseEntity("Quantità insufficiente!", HttpStatus.BAD_REQUEST);
        }catch(EccezioneQuantitaProdottoNonDisponibile e){
            return new ResponseEntity("Quantità insufficiente!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------PUT Methods------------------------------------*/

    @PutMapping("/{id}/{carrelloAssociato}/{prodottoAssociato}/{quantita}") //specifichiamo un percorso Path specifico per richieste di tipo PUT.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateAcquisto(@PathVariable int id, @PathVariable int carrelloAssociato, @PathVariable int prodottoAssociato, @PathVariable int quantita){ //resituisce una riposta HTTP che può essere corretta (Acquisto aggiornato) oppure no.
        try {
            Acquisto daRitornare = acquistoService.updateAcquisto(id, carrelloAssociato, prodottoAssociato, quantita);
            return new ResponseEntity(daRitornare, HttpStatus.OK);
        } catch (EccezioneAcquistoNonEsistente e) {
            return new ResponseEntity("Acquisto inesistente.", HttpStatus.BAD_REQUEST);
        } catch (EccezioneCarrelloNonEsistente e) {
            return new ResponseEntity("Carrello associato inesistente.", HttpStatus.BAD_REQUEST);
        } catch (EccezioneQuantitaInsufficiente e) {
            return new ResponseEntity("Quantità immessa insufficiente.", HttpStatus.BAD_REQUEST);
        } catch (EccezioneProdottoNonEsistente e) {
            return new ResponseEntity("Prodotto associato inesistente.", HttpStatus.BAD_REQUEST);
        } catch (EccezioneQuantitaProdottoNonDisponibile e) {
            return new ResponseEntity("Quantità richiesta non disponibile.", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------DELETE Methods------------------------------------*/

    @DeleteMapping("/{id}") //specifichiamo un percorso Path specifico per richieste di tipo DELETE.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteAcquisto(@PathVariable int id){ //resituisce una riposta HTTP che può essere corretta (Acquisto eliminato) oppure no.
        try {
            acquistoService.deleteAcquisto(id);
            return new ResponseEntity("Acquisto con id:"+id+" eliminato.", HttpStatus.OK);
        } catch (EccezioneAcquistoNonEsistente e) {
            return new ResponseEntity("Acquisto inesistente.", HttpStatus.BAD_REQUEST);
        }
    }

}
