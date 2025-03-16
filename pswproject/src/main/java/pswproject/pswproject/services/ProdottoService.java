package pswproject.pswproject.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import pswproject.pswproject.Exceptions.EccezioneCategoriaInesistente;
import pswproject.pswproject.Exceptions.EccezioneCodiceEANGiaEsistente;
import pswproject.pswproject.Exceptions.EccezionePrezzoNonValido;
import pswproject.pswproject.Exceptions.EccezioneProdottoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneQuantitaInsufficiente;
import pswproject.pswproject.entities.Acquisto;
import pswproject.pswproject.entities.Categoria;
import pswproject.pswproject.entities.Prodotto;
import pswproject.pswproject.repositories.AcquistoRepository;
import pswproject.pswproject.repositories.CategoriaRepository;
import pswproject.pswproject.repositories.ProdottoRepository;

//componente univoco che si occupa dell'aspetto logico delle operazioni.
@Service
public class ProdottoService {
    
    @Autowired //rappresenta che l'oggetto in questione, univoco per il be, venga iniettato in questo componente.
    private ProdottoRepository prodottoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AcquistoRepository acquistoRepository;

    /*------------------------------------READ------------------------------------*/

    @Transactional(readOnly = true)
    public List<Prodotto> getAll() //interroga la repository per ottenere tutti i Prodotti.
    {
        return prodottoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Prodotto> productsByName(String nome) //interroga la repository per ottenere i Prodotti corrispondenti, dato il nome.
    {
        return prodottoRepository.findByNome(nome);
    }

    @Transactional(readOnly = true)
    public List<Prodotto> productsByCategory(Categoria c) //interroga la repository per ottenere i Prodotti corrispondenti, data la Categoria.
    {
        return prodottoRepository.findByCategoria(c);
    }

    @Transactional(readOnly = true)
    public List<Prodotto> productsByCategory(Categoria c, int nPagine, int sPagina, String ordinamento) //interroga la repository per ottenere i Prodotti corrispondenti, data la Categoria e li racchiude in un oggetto Page (una porzione del totale).
    {
        Pageable p = PageRequest.of(nPagine, sPagina, Sort.by(ordinamento));
        Page<Prodotto> p2 = prodottoRepository.findByCategoria(c, (org.springframework.data.domain.Pageable) p);
        if(p2.hasContent())
            return p2.getContent();
        else
            return null;
    }

    @Transactional(readOnly = true)
    public List<Prodotto> getProdotti(int nPagine, int sPagina, String ordinamento) //interroga la repository per ottenere i Prodotti corrispondenti e li racchiude in un oggetto Page (una porzione del totale).
    {
        Pageable p = PageRequest.of(nPagine, sPagina, Sort.by(ordinamento));
        Page<Prodotto> p2 = prodottoRepository.findAll((org.springframework.data.domain.Pageable) p);
        if(p2.hasContent())
            return p2.getContent();
        else
            return null;
    }

    /*------------------------------------WRITE------------------------------------*/

    @Transactional(readOnly = false)
    public Prodotto addProdotto(Prodotto prodotto) throws EccezioneCodiceEANGiaEsistente, EccezioneCategoriaInesistente, EccezioneQuantitaInsufficiente, EccezionePrezzoNonValido //chiede alla repository di aggiungere un Prodotto, altrimenti non valido.
    {
        //controllo se Prodotto esiste per l'EAN ; Categoria non esiste ; Quantità negativa ; Prezzo negativo:
        if(prodottoRepository.existsByEAN(prodotto.getEAN()))
            throw new EccezioneCodiceEANGiaEsistente("Il prodotto esiste già!");
        if(!categoriaRepository.existsByNome(prodotto.getCategoria().getNome()))
            throw new EccezioneCategoriaInesistente("La categoria dichiarata per il prodotto non esiste!");
        if(prodotto.getQuantitaInMagazzino() <=0)
            throw new EccezioneQuantitaInsufficiente("Quantità non sufficiente per la vendita!");
        if(prodotto.getPrezzo() <=0 )
            throw new EccezionePrezzoNonValido("Prezzo non adatto alla vendita!");

        //creiamo nuovo oggetto Prodotto:
        Prodotto nuovoProdotto = new Prodotto();
        nuovoProdotto.setNome(prodotto.getNome()); nuovoProdotto.setPrezzo(prodotto.getPrezzo()); nuovoProdotto.setMarca(prodotto.getMarca());
        nuovoProdotto.setQuantitaInMagazzino(prodotto.getQuantitaInMagazzino()); nuovoProdotto.setEAN(prodotto.getEAN());
        nuovoProdotto.setCategoria(categoriaRepository.findByNome(prodotto.getCategoria().getNome()).get());

        //salviamo il nuovo Prodotto:
        return prodottoRepository.save(nuovoProdotto);
    }

    @Transactional(readOnly = false)
    public Prodotto updateProdotto(String ean, Prodotto prodotto) throws EccezioneProdottoNonEsistente, EccezioneCategoriaInesistente, EccezioneQuantitaInsufficiente, EccezionePrezzoNonValido { //chiede alla repository di aggiornare un Prodotto, altrimenti non valido.

        //controllo se Prodotto non esiste per l'EAN ; Categoria non esiste ; Quantità negativa ; Prezzo negativo:
        if(!prodottoRepository.existsByEAN(ean))
            throw new EccezioneProdottoNonEsistente("Il prodotto non esiste!");
        if(!categoriaRepository.existsByNome(prodotto.getCategoria().getNome()))
            throw new EccezioneCategoriaInesistente("La categoria non esiste!");
        if(prodotto.getQuantitaInMagazzino() <=0)
            throw new EccezioneQuantitaInsufficiente("Quantità non sufficiente per la vendita!");
        if(prodotto.getPrezzo() <=0 )
            throw new EccezionePrezzoNonValido("Prezzo non adatto alla vendita!");

        //prendiamo il Prodotto dalla repository:
        Prodotto daAggiornare = prodottoRepository.findByEAN(ean).get();
        daAggiornare.setNome(prodotto.getNome()); daAggiornare.setPrezzo(prodotto.getPrezzo()); daAggiornare.setMarca(prodotto.getMarca());
        daAggiornare.setQuantitaInMagazzino(prodotto.getQuantitaInMagazzino()); 
        daAggiornare.setCategoria(categoriaRepository.findByNome(prodotto.getCategoria().getNome()).get());

        //salviamo il nuovo Prodotto aggiornato:
        return prodottoRepository.save(daAggiornare);
    }

    /*------------------------------------DELETE------------------------------------*/

    @Transactional(readOnly = false)
    public Prodotto deleteProdotto(String ean) //chiede alla repository di eliminare un Prodotto, altrimenti non esiste.
    {
        //controllo se Prodotto esiste per l'EAN:
        if(!prodottoRepository.existsByEAN(ean))
            throw new EccezioneProdottoNonEsistente("Prodotto non esistente!");
        
        //prendiamo il prodotto da eliminare dalla repository:
        Prodotto daEliminare = prodottoRepository.findByEAN(ean).get();
        
        //eliminiamo gli acquisti relativi al Prodotto chiedendo all'acquistoRepository di farlo, non al Service in quanto non ci interessa che venga risommata la quantità.
        for(Acquisto a : acquistoRepository.findByProdottoVenduto(daEliminare))
            acquistoRepository.deleteByIdAcquisto(a.getIdAcquisto());
        
        //eliminiamo il prodotto:
        prodottoRepository.deleteByIdProdotto(daEliminare.getIdProdotto()); 
        return daEliminare;
    }
}
