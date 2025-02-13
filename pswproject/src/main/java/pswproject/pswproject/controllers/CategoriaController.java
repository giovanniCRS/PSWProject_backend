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
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCategoriaGiaEsistente;
import pswproject.pswproject.Exceptions.EccezioneCategoriaInesistente;
import pswproject.pswproject.entities.Categoria;
import pswproject.pswproject.services.CategoriaService;

@RestController                               //indica un endpoint del be al quale poter fare richiesta.
@RequestMapping("/categories")                //specifichiamo un percorso Path specifico per questo endpoint.
@CrossOrigin(origins="http://localhost:4200") //sono permesse richieste al be dal fe a patto che il Path-name abbia questa origine.
public class CategoriaController {
    
    @Autowired //rappresenta che l'oggetto UtenteService univoco per il be venga iniettato in questo componente.
    private CategoriaService categoriaService;

    /*------------------------------------GET Methods------------------------------------*/

    @GetMapping("/all")                   //specifichiamo un percorso Path specifico per richieste di tipo GET ; in questo caso il metodo è permesso a tutti (non specifichiamo i ruoli).
    public List<Categoria> getCategorie() //restituisce la lista di tutte le Categorie presenti sul database.
    {
        return categoriaService.getAll();
    }

    @GetMapping("/byName/{nome}")
    @SuppressWarnings({"all"})
    public ResponseEntity getCategoriaById(@PathVariable String nome) //restituisce la lista di tutte le Categorie presenti sul database, in base al nome corrispondente.
    {
        Optional<Categoria> risCategoria = categoriaService.getCategoria(nome);
        if(risCategoria.isPresent())
            return new ResponseEntity(risCategoria.get(), HttpStatus.OK);
        else
            return new ResponseEntity("Categoria non esistente", HttpStatus.BAD_REQUEST);
    }

    /*------------------------------------POST Methods------------------------------------*/

    @PostMapping ()
    @SuppressWarnings({"all"})
    @PreAuthorize("hasRole('ADMIN')")                                             //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity createCategory(@RequestBody @Valid Categoria categoria) //resituisce una riposta HTTP che può essere corretta (Categoria creata) oppure no.
    {
        try{
            if(categoria.getNome().equals("") || categoria.getNome()==null || !categoria.getNome().matches("[a-zA-Z][a-zA-Z\\s]+") || categoria.getNome().length() < 4)
                return new ResponseEntity("Categoria non valida!", HttpStatus.BAD_REQUEST);
            Categoria risCategoria = categoriaService.addCategory(categoria);
            return new ResponseEntity(risCategoria, HttpStatus.OK);
        }catch(EccezioneCategoriaGiaEsistente e){
            return new ResponseEntity("Categoria già esistente!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------PUT Methods------------------------------------*/

    @PutMapping("/{nome}")                                                                                    //specifichiamo un percorso Path specifico per richieste di tipo PUT.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")                                                                         //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity updateCategoria(@PathVariable String nome, @RequestBody @Valid Categoria categoria) //resituisce una riposta HTTP che può essere corretta (Categoria aggiornata) oppure no.
    {
        try {
            if(categoria.getNome().equals("") || categoria.getNome()==null || !categoria.getNome().matches("[a-zA-Z][a-zA-Z\\s]+") || categoria.getNome().length() < 4)
                return new ResponseEntity("Categoria non valida!", HttpStatus.BAD_REQUEST);
            Categoria update=categoriaService.updateCategoria(nome, categoria.getNome());
            return new ResponseEntity(update, HttpStatus.OK);
        } catch (EccezioneCategoriaInesistente e) {
            return new ResponseEntity("Categoria inesistente.!", HttpStatus.BAD_REQUEST);
        } catch (EccezioneCategoriaGiaEsistente e) {
            return new ResponseEntity("Categoria già esistente!", HttpStatus.BAD_REQUEST);
        }
    }

    /*------------------------------------DELETE Methods------------------------------------*/

    @DeleteMapping("/{nome}")                                         //specifichiamo un percorso Path specifico per richieste di tipo DELETE.
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PreAuthorize("hasRole('ADMIN')")                                 //la richiesta può essere effettuata solo da utenti loggati che possiedono il Ruolo indicato.
    public ResponseEntity deleteCategoria(@PathVariable String nome){ //resituisce una riposta HTTP che può essere corretta (Categoria eliminata) oppure no.
        try {
            Categoria eliminata = categoriaService.deleteCategoria(nome);
            return new ResponseEntity(eliminata, HttpStatus.OK);
        } catch (EccezioneCategoriaInesistente e) {
            return new ResponseEntity("Categoria inesistente!", HttpStatus.BAD_REQUEST);
        } catch (EccezioneAcquistoNonEsistente e) {
            return new ResponseEntity("Uno degli aquisti non esiste!", HttpStatus.BAD_REQUEST);
        }
    }

}
