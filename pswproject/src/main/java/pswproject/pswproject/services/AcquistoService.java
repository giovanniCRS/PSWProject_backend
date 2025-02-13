package pswproject.pswproject.services;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCarrelloNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneProdottoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneQuantitaInsufficiente;
import pswproject.pswproject.Exceptions.EccezioneQuantitaProdottoNonDisponibile;
import pswproject.pswproject.entities.Acquisto;
import pswproject.pswproject.entities.Prodotto;
import pswproject.pswproject.repositories.AcquistoRepository;
import pswproject.pswproject.repositories.CarrelloRepository;
import pswproject.pswproject.repositories.ProdottoRepository;

//componente univoco che si occupa dell'aspetto logico delle operazioni.
@Service
public class AcquistoService {

    @Autowired //rappresenta che l'oggetto in questione, univoco per il be, venga iniettato in questo componente.
    private AcquistoRepository acquistoRepository;

    @Autowired
    private CarrelloRepository carrelloRepository;

    @Autowired
    private ProdottoRepository prodottoRepository;

    /*------------------------------------READ------------------------------------*/

    @Transactional(readOnly = true)
    public List<Acquisto> getAcquisti() //interroga la repository per ottenere tutti gli Acquisti.
    {
        return acquistoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings({"all"})
    public List<Acquisto> getAcquistiByCarrello(int carrello) //interroga la repository per ottenere gli Acquisti corrispondenti, dato il CarrelloAssociato.
    {
        return acquistoRepository.findByCarrelloAssociato(carrelloRepository.getById(carrello));
    }

    @Transactional(readOnly = true)
    public Optional<Acquisto> getAcquistoById(int idAcquisto) //interroga la repository per ottenere gli Acquisti corrispondenti, dato l'id.
    {
        return acquistoRepository.findById(idAcquisto);
    }

    /*------------------------------------WRITE------------------------------------*/

    @Transactional(readOnly = false)
    @SuppressWarnings({"all"})
    public Acquisto createAcquisto(int carrelloAssociato, String EAN, int quantita) //chiede alla repository di aggiungere un Acquisto, altrimenti non valido.
    {
        //controlliamo se il ProdottoAssociato esiste e se la quantita richiesta di acquisto è positiva:
        if(!prodottoRepository.existsByEAN(EAN))
            throw new EccezioneProdottoNonEsistente("Il prodotto non esiste!");
        if(quantita <= 0)
            throw new EccezioneQuantitaInsufficiente("Quantità non valida!");

        //prendiamo l'oggetto Prodotto dalla sua repository, in quanto vogliamo aggiornarlo:
        Prodotto p = prodottoRepository.findByEAN(EAN).get();
        int nuovaQuantita = p.getQuantitaInMagazzino() - quantita; //variabile temporanea per valutare validità d'acquisto.

        //check se si vuole comprare di più rispetto alla quantità presente in magazzino:
        if(nuovaQuantita < 0)
            throw new EccezioneQuantitaProdottoNonDisponibile("Quantità non disponibile in magazzino");

        //creiamo nuovo oggetto Acquisto:
        Acquisto nuovoAcquisto = new Acquisto();
        
        //aggiorniamo il Prodotto con la nuova quantità:
        p.setQuantitaInMagazzino(nuovaQuantita);

        //settiamo i parametri dell'acquisto:
        nuovoAcquisto.setProdottoVenduto(prodottoRepository.findByEAN(EAN).get());
        nuovoAcquisto.setCarrelloAssociato(carrelloRepository.findByIdCarrello(carrelloAssociato).get());
        nuovoAcquisto.setQuantita(quantita);

        //salviamo il nuovo Acquisto nella repository:
        return acquistoRepository.save(nuovoAcquisto);
    }

    @Transactional(readOnly = false)
    public Acquisto updateAcquisto(int id, int carrelloAssociato, int prodottoAssociato, int quantita) throws EccezioneAcquistoNonEsistente, EccezioneCarrelloNonEsistente, EccezioneQuantitaInsufficiente, EccezioneProdottoNonEsistente, EccezioneQuantitaProdottoNonDisponibile //chiede alla repository di aggiornare un Acquisto, altrimenti non valido.
    {
        //controlliamo se l'Acquisto esiste già ; Carrello associato esiste ; il ProdottoAssociato esiste e se la quantita richiesta di acquisto è positiva:
        if(!acquistoRepository.existsByIdAcquisto(id))
            throw new EccezioneAcquistoNonEsistente("L'acquisto non esiste!");
        if(!carrelloRepository.existsByIdCarrello(carrelloAssociato))
            throw new EccezioneCarrelloNonEsistente("Il carrello non esiste!");
        if(!prodottoRepository.existsByIdProdotto(prodottoAssociato))
            throw new EccezioneProdottoNonEsistente("Il prodotto non esiste!");
        if(quantita <= 0)
            throw new EccezioneQuantitaInsufficiente("Quantità insufficiente.");

        //prendiamo dalla repository l'Acquisto da aggiornare:
        Acquisto daAggiornare = acquistoRepository.findByIdAcquisto(id).get();

        //prendiamo dalla repository il ProdottoAssociato da aggiornare:
        Prodotto p = prodottoRepository.findByIdProdotto(prodottoAssociato).get();

        int nuovaQuantita = 0;

        if(daAggiornare.getProdottoVenduto().getIdProdotto() == prodottoAssociato)
        {
            //l'Acquisto che si vuole aggiornare si basa sullo stesso Prodotto:

            //controlliamo la nuova quantità del Prodotto da aggiornare:
            nuovaQuantita = p.getQuantitaInMagazzino() - (quantita + daAggiornare.getQuantita());
            if (nuovaQuantita < 0)
            {
                throw new EccezioneQuantitaProdottoNonDisponibile("La quantità richiesta non è disponibile in magazzino!");
            }

            //settiamo la nuova quantità del Prodotto da aggiornare:
            p.setQuantitaInMagazzino(nuovaQuantita);
        }
        else
        {
            //l'Acquisto che si vuole aggiornare non si basa sullo stesso Prodotto:

            //controlliamo la nuova quantità del Prodotto da aggiornare:
            nuovaQuantita = p.getQuantitaInMagazzino() - quantita;
            if (nuovaQuantita < 0)
            {
                throw new EccezioneQuantitaProdottoNonDisponibile("La quantità richiesta non è disponibile in magazzino!");
            }
            //settiamo la nuova quantità del Prodotto da aggiornare:
            p.setQuantitaInMagazzino(nuovaQuantita);

            //ripristino dei pezzi del Prodotto precedente:
            daAggiornare.getProdottoVenduto().setQuantitaInMagazzino(daAggiornare.getProdottoVenduto().getQuantitaInMagazzino() + daAggiornare.getQuantita());
        }

        //settiamo i parametri dell'acquisto da aggiornare:
        daAggiornare.setCarrelloAssociato(carrelloRepository.findByIdCarrello(carrelloAssociato).get());
        daAggiornare.setProdottoVenduto(prodottoRepository.findByIdProdotto(prodottoAssociato).get());
        daAggiornare.setQuantita(quantita);

        //salviamo Acquisto aggiornato:
        return acquistoRepository.save(daAggiornare);
    }

    /*------------------------------------DELETE------------------------------------*/

    @Transactional(readOnly = false)
    public void deleteAcquisto(int id) throws EccezioneAcquistoNonEsistente //chiede alla repository di eliminare un Acquisto, altrimenti non valido.
    {
        //controlliamo se l'Acquisto esiste già:
        if(!acquistoRepository.existsByIdAcquisto(id))
            throw new EccezioneAcquistoNonEsistente("L'acquisto non esiste!");
        
        //prendiamo dalla repository l'Acquisto da eliminare:
        Acquisto daEliminare = acquistoRepository.findByIdAcquisto(id).get();

        //prendiamo il ProdottoAssociato da aggiornare:
        Prodotto p = daEliminare.getProdottoVenduto();
        
        //aggiorniamo la quantità del Prodotto:
        int nuovaQuantita = (p.getQuantitaInMagazzino() + daEliminare.getQuantita()) ;
        
        //salviamo il Prodotto aggiornato nella repository:
        p.setQuantitaInMagazzino(nuovaQuantita);
        prodottoRepository.save(p);
        
        //eliminamo l'Acquisto:
        acquistoRepository.deleteByIdAcquisto(id);
    }
}
