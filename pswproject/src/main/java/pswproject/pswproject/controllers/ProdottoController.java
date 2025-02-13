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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import pswproject.pswproject.Exceptions.EccezioneCategoriaInesistente;
import pswproject.pswproject.Exceptions.EccezioneCodiceEANGiaEsistente;
import pswproject.pswproject.Exceptions.EccezionePrezzoNonValido;
import pswproject.pswproject.Exceptions.EccezioneProdottoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneQuantitaInsufficiente;
import pswproject.pswproject.entities.Categoria;
import pswproject.pswproject.entities.Prodotto;
import pswproject.pswproject.services.CategoriaService;
import pswproject.pswproject.services.ProdottoService;

@RestController                               //indica un endpoint del be al quale poter fare richiesta.
@RequestMapping("/products")                  //specifichiamo un percorso Path specifico per questo endpoint.
@CrossOrigin(origins="http://localhost:4200") //sono permesse richieste al be dal fe a patto che il Path-name abbia questa origine.
public class ProdottoController {
    
    @Autowired //rappresenta che l'oggetto ProdottoService univoco per il be venga iniettato in questo componente.
    private ProdottoService prodottoService;

    @Autowired
    private CategoriaService categoriaService;

    /*------------------------------------GET Methods------------------------------------*/

    @GetMapping("/all")            //specifichiamo un percorso Path specifico per richieste di tipo GET ; in questo caso il metodo è permesso a tutti (non specifichiamo i ruoli).
    public List<Prodotto> getAll() //restituisce la lista di tutti i Prodotti presenti sul database.
    {
        return prodottoService.getAll();
    }

    @GetMapping("/byName/{nome}")
    public List<Prodotto> productsByName(@PathVariable String nome) //restituisce la lista di tutti i Prodotti presenti sul database, in base al nome corrispondente.
    {
        return prodottoService.productsByName(nome);
    }

    @GetMapping("/byCategory/{nome}") 
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity productsByCategory(@PathVariable String nome) //restituisce la lista di tutti i Prodotti presenti sul database, in base alla Categoria corrispondente.
    {
        try{
            Optional<Categoria> o = categoriaService.getCategoria(nome);
            if(!o.isPresent())
                return new ResponseEntity("Categoria non esistente!", HttpStatus.BAD_REQUEST);

            List<Prodotto> daRitornare = prodottoService.productsByCategory(o.get());
            return new ResponseEntity(daRitornare, HttpStatus.OK);

        }catch(EccezioneCategoriaInesistente e){
            return new ResponseEntity("Categoria non esistente!", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/page/byCategory/{nome}/{nPagine}/{sPagina}/{ordinamento}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity productsByCategory(@PathVariable String nome, @PathVariable int nPagine, @PathVariable int sPagina, @PathVariable String ordinamento) //restituisce la lista di tutti i Prodotti presenti sul database, in base alla Categoria racchiudendoli in un oggetto Page (che contiene un determinato numero di Prodotti).
    {
        try{
            List<Prodotto> daRitornare = prodottoService.productsByCategory(categoriaService.getCategoria(nome).get(), nPagine, sPagina, ordinamento);
            return new ResponseEntity(daRitornare, HttpStatus.OK);
        }catch(EccezioneCategoriaInesistente e){
            return new ResponseEntity("Categoria non esistente!", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/page/{nPagine}/{sPagina}/{ordinamento}")
    public List<Prodotto> getProdotti(@PathVariable int nPagine, @PathVariable int sPagina, @PathVariable String ordinamento){ //restituisce la lista di tutti i Prodotti presenti sul database, racchiudendoli in un oggetto Page (che contiene un determinato numero di Prodotti).
        return prodottoService.getProdotti(nPagine, sPagina, ordinamento);
    }

    /*------------------------------------POST Methods------------------------------------*/

    @PostMapping("/add")                                                     //specifichiamo un percorso Path specifico per richieste di tipo POST.
    @SuppressWarnings({"unchecked", "rawtypes"}) 
    @PreAuthorize("hasRole('ADMIN')")                                        //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity addProdotto(@RequestBody @Valid Prodotto prodotto) //resituisce una riposta HTTP che può essere corretta (Prodotto creato) oppure no.
    {
        try{
            //Correzione formattazione del prodotto
            prodotto.setNome(prodotto.getNome().trim().toLowerCase()); prodotto.setEAN(prodotto.getEAN().trim());
            if(prodotto.getEAN().equals("") || prodotto.getEAN()==null || prodotto.getEAN().length()!=13 || !prodotto.getEAN().matches("^[0-9]+")
                || prodotto.getQuantitaInMagazzino()<=0 || prodotto.getPrezzo()<=0 || prodotto.getNome().equals("") || prodotto.getNome()==null || prodotto.getNome().length()<4
                || prodotto.getMarca().equals("") || prodotto.getMarca()==null
            )
                return new ResponseEntity("Prodotto non valido!", HttpStatus.BAD_REQUEST);

            Prodotto aggiunto = prodottoService.addProdotto(prodotto);
            return new ResponseEntity(aggiunto, HttpStatus.OK);

        }catch(EccezioneCodiceEANGiaEsistente e){
            return new ResponseEntity("Codice EAN Già Esistente!", HttpStatus.BAD_REQUEST);
        }catch(EccezioneCategoriaInesistente e){
            return new ResponseEntity("Categoria del prodotto non esistente!", HttpStatus.BAD_REQUEST);
        }catch(EccezioneQuantitaInsufficiente e){
            return new ResponseEntity("Quantità in magazzino non valida!", HttpStatus.BAD_REQUEST);
        }catch(EccezionePrezzoNonValido e){
            return new ResponseEntity("Prezzo non valido!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------PUT Methods------------------------------------*/

    @PutMapping("/{ean}")             //specifichiamo un percorso Path specifico per richieste di tipo PUT.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')") //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity updateProdotto(@PathVariable String ean, @Valid @RequestBody Prodotto prodotto){ //resituisce una riposta HTTP che può essere corretta (Prodotto aggiornato) oppure no.
        try {
            //Correzione formattazione del prodotto
            prodotto.setNome(prodotto.getNome().trim().toLowerCase()); prodotto.setEAN(prodotto.getEAN().trim());
            if(prodotto.getEAN().equals("") || prodotto.getEAN()==null || prodotto.getEAN().length()!=13 || !prodotto.getEAN().matches("^[0-9]+")
                || prodotto.getQuantitaInMagazzino()<=0 || prodotto.getPrezzo()<=0 || prodotto.getNome().equals("") || prodotto.getNome()==null || prodotto.getNome().length()<=4
                || !prodotto.getNome().matches("^[a-zA-Z\\s]+$") || prodotto.getMarca().equals("") || prodotto.getMarca()==null
            )
                return new ResponseEntity("Prodotto non valido!", HttpStatus.BAD_REQUEST);

            Prodotto aggiornato = prodottoService.updateProdotto(ean, prodotto);
            return new ResponseEntity(aggiornato, HttpStatus.OK);
        } catch (EccezioneProdottoNonEsistente e) {
            return new ResponseEntity("Prodotto inesistente.", HttpStatus.BAD_REQUEST);
        } catch (EccezioneCategoriaInesistente e) {
            return new ResponseEntity("Categoria inesistente.", HttpStatus.BAD_REQUEST);
        }catch(EccezioneQuantitaInsufficiente e){
            return new ResponseEntity("Quantità in magazzino non valida!", HttpStatus.BAD_REQUEST);
        }catch(EccezionePrezzoNonValido e){
            return new ResponseEntity("Prezzo non valido!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------DELETE Methods------------------------------------*/

    @DeleteMapping("/{ean}")          //specifichiamo un percorso Path specifico per richieste di tipo DELETE.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')") //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity deleteProdotto(@PathVariable String ean){ //resituisce una riposta HTTP che può essere corretta (Prodotto eliminato) oppure no.
        try {
            Prodotto prodottoEliminato=prodottoService.deleteProdotto(ean);
            return new ResponseEntity(prodottoEliminato, HttpStatus.OK);
        } catch (EccezioneProdottoNonEsistente e) {
            return new ResponseEntity("Prodotto inesistente!", HttpStatus.BAD_REQUEST);
        }
    }
}
