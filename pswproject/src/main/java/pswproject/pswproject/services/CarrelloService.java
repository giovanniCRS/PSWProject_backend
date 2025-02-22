package pswproject.pswproject.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCarrelloNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneUtenteNonEsistente;
import pswproject.pswproject.dto.CarrelloDto;
import pswproject.pswproject.entities.Acquisto;
import pswproject.pswproject.entities.Carrello;
import pswproject.pswproject.repositories.AcquistoRepository;
import pswproject.pswproject.repositories.CarrelloRepository;
import pswproject.pswproject.repositories.UtenteRepository;

//componente univoco che si occupa dell'aspetto logico delle operazioni.
@Service
public class CarrelloService {

    @Autowired //rappresenta che l'oggetto in questione, univoco per il be, venga iniettato in questo componente.
    private CarrelloRepository carrelloRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private AcquistoService acquistoService;

    @Autowired
    private AcquistoRepository acquistoRepository;

    /*------------------------------------READ------------------------------------*/

    @Transactional(readOnly = true)
    public List<Carrello> getCarrelli() //interroga la repository per ottenere tutti i Carrelli.
    {
        return carrelloRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Carrello> getCarrelliById(int idCarrello) //interroga la repository per ottenere i Carrelli corrispondenti, dato l'id.
    {
        return carrelloRepository.findById(idCarrello);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings({"all"})
    public List<Carrello> getCarrelliByUtente(String email) throws EccezioneUtenteNonEsistente //interroga la repository per ottenere i Carrelli corrispondenti, data la mail dell'UtenteAssociato.
    {
        if(!utenteRepository.findByEmail(email).isPresent())
            throw new EccezioneUtenteNonEsistente("L'utente non esiste!");
        return carrelloRepository.findByUtenteAssociato(utenteRepository.findByEmail(email).get());
    }

    /*------------------------------------WRITE------------------------------------*/

    @Transactional(readOnly = false)
    public Carrello addCarrello(@Valid CarrelloDto carrello) throws EccezioneUtenteNonEsistente, EccezioneCarrelloNonEsistente //chiede alla repository di aggiungere un Carrello, altrimenti non valido.
    {
        //controllo se l'UtenteAssociato esiste:
        if(!utenteRepository.existsByEmail(carrello.getEmail()))
            throw new EccezioneUtenteNonEsistente("L'utente non esiste!");

        //creiamo nuovo oggetto Carrello:
        Carrello nuovCarrello = new Carrello();

        //salviamo il nuovo Carrello, così che gli Acquisti che creeremo possano trovare un CarrelloAssociato già disponibile:
        nuovCarrello = carrelloRepository.save(nuovCarrello);

        nuovCarrello.setIndirizzo(carrello.getIndirizzo()); nuovCarrello.setMetodoDiPagamento(carrello.getMetodoDiPagamento());
        nuovCarrello.setNumeroDiTelefono(carrello.getNumeroDiTelefono());

        nuovCarrello.setUtenteAssociato(utenteRepository.findByEmail(carrello.getEmail()).get()); //nonostante l'utente sia valido lo prendiamo dal nostro db.

        //creiamo gli Acquisti chiedendo di farlo ad acquistoService:
        List<Acquisto> acquisti = new ArrayList<>();
        for( Acquisto a : carrello.getAcquisti() )
            acquisti.add(acquistoService.createAcquisto(nuovCarrello.getIdCarrello(), a.getProdottoVenduto().getEAN(), a.getQuantita(), a.getPrezzovendita()));
        nuovCarrello.setAcquisti(acquisti);

        return nuovCarrello;
    }

    @Transactional(readOnly = false)
    public Carrello updateCarrello(int id, @Valid Carrello carrello) throws EccezioneCarrelloNonEsistente, EccezioneUtenteNonEsistente //chiede alla repository di aggiornare un Carrello, altrimenti non valido.
    {
        //controllo se il Carrello esiste:
        if(!carrelloRepository.existsByIdCarrello(id))
            throw new EccezioneCarrelloNonEsistente("Il carrello non esiste!");

        //controllo se l'UtenteAssociato esiste:
        if(!utenteRepository.existsByEmail(carrello.getUtenteAssociato().getEmail()))
            throw new EccezioneUtenteNonEsistente("L'utente non esiste!");
        
        //prendiamo il Carrello da aggiornare dalla repository e aggiorniamolo:
        Carrello daAggiornare = carrelloRepository.findByIdCarrello(id).get();

        daAggiornare.setIndirizzo(carrello.getIndirizzo()); daAggiornare.setNumeroDiTelefono(carrello.getNumeroDiTelefono());
        daAggiornare.setMetodoDiPagamento(carrello.getMetodoDiPagamento());

        daAggiornare.setUtenteAssociato(utenteRepository.findByEmail(carrello.getUtenteAssociato().getEmail()).get());

        //salviamo il Carrello aggiornato:
        return carrelloRepository.save(daAggiornare);
    }

    /*------------------------------------DELETE------------------------------------*/

    @Transactional(readOnly = false)
    public void deleteCarrello(int id) throws EccezioneCarrelloNonEsistente, EccezioneAcquistoNonEsistente //chiede alla repository di eliminare un Carrello, altrimenti non valido.
    {
        //controllo se il Carrello esiste:
        if(!carrelloRepository.existsByIdCarrello(id))
            throw new EccezioneCarrelloNonEsistente("Il carrello non esiste!");

        //prendiamo il Carrello da eliminare dalla repository:
        Carrello daEliminare = carrelloRepository.findByIdCarrello(id).get();

        //eliminiamo gli Acquisti annessi, chiedendolo ad acquistoService di farlo (vogliamo che la quantità dei prodotti venga ripristinata in magazzino):
        for(Acquisto a: acquistoRepository.findByCarrelloAssociato(daEliminare))
            acquistoService.deleteAcquisto(a.getIdAcquisto());

        //eliminiamo il Carrello:
        carrelloRepository.deleteByIdCarrello(id);
    }
}
