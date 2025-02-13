package pswproject.pswproject.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCategoriaGiaEsistente;
import pswproject.pswproject.Exceptions.EccezioneCategoriaInesistente;
import pswproject.pswproject.entities.Acquisto;
import pswproject.pswproject.entities.Categoria;
import pswproject.pswproject.entities.Prodotto;
import pswproject.pswproject.repositories.AcquistoRepository;
import pswproject.pswproject.repositories.CategoriaRepository;
import pswproject.pswproject.repositories.ProdottoRepository;

//componente univoco che si occupa dell'aspetto logico delle operazioni.
@Service
public class CategoriaService {

    @Autowired //rappresenta che l'oggetto in questione, univoco per il be, venga iniettato in questo componente.
    private CategoriaRepository categoriaRepository;

    @Autowired
    private AcquistoService acquistoService;

    @Autowired
    private AcquistoRepository acquistoRepository;

    @Autowired
    private ProdottoRepository prodottoRepository;

    /*------------------------------------READ------------------------------------*/

    @Transactional(readOnly = true)
    public List<Categoria> getAll() //interroga la repository per ottenere tutte le Categorie.
    {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> getCategoria(String nome) //interroga la repository per ottenere la Categoria corrispondente, dato il nome.
    {
        return categoriaRepository.findByNome(nome);
    }

    /*------------------------------------WRITE------------------------------------*/

    @Transactional(readOnly = false)
    public Categoria addCategory(@Valid Categoria categoria) throws EccezioneCategoriaGiaEsistente //chiede alla repository di aggiungere una Categoria, altrimenti esiste già.
    {
        //controlliamo se Categoria esiste già:
        if(categoriaRepository.existsByNome(categoria.getNome()))
            throw new EccezioneCategoriaGiaEsistente("Categoria già esistente");

        //creiamo un nuovo oggetto Categoria
        Categoria nuovaCategoria = new Categoria();
        nuovaCategoria.setNome(categoria.getNome());

        //assegniamo i prodotti corrispondenti decisi dall'Admin:
        List<Prodotto> prodotti = new ArrayList<>();
        nuovaCategoria.setProdotti(prodotti);

        //salviamo la nuova Categoria:
        return categoriaRepository.save(nuovaCategoria);
    }
    
    @Transactional(readOnly = false)
    public Categoria updateCategoria(String vecchioNome, String nome) throws EccezioneCategoriaInesistente, EccezioneCategoriaGiaEsistente //chiede alla repository di aggiornare una Categoria, altrimenti non valida.
    {
        //controlliamo se il nuovo nome non corrisponde a una Categoria che esiste già:
        if(categoriaRepository.existsByNome(nome))
            throw new EccezioneCategoriaGiaEsistente("La categoria esiste già!");

        //controlliamo se Categoria esiste:
        if(!categoriaRepository.existsByNome(vecchioNome))
            throw new EccezioneCategoriaInesistente("La categoria non esiste!");

        //prendiamo l'oggetto Categoria dalla repository e lo aggiorniamo:
        Categoria daAggiornare = categoriaRepository.findByNome(vecchioNome).get();
        daAggiornare.setNome(nome);

         //salviamo la Categoria aggiornata:
        return categoriaRepository.save(daAggiornare);
    }

    /*------------------------------------DELETE------------------------------------*/

    @Transactional(readOnly = false)
    public Categoria deleteCategoria(String nome) throws EccezioneCategoriaInesistente, EccezioneAcquistoNonEsistente //chiede alla repository di eliminare una Categoria, altrimenti non esiste.
    {
        //controlliamo se Categoria esiste:
        if(!categoriaRepository.existsByNome(nome))
            throw new EccezioneCategoriaInesistente("La categoria non esiste!");

        //prendiamo l'oggetto Categoria da eliminare dalla repository:
        Categoria daEliminare = categoriaRepository.findByNome(nome).get();

        //eliminiamo i prodotti relativi alla Categoria, con annessi Acquisiti:
        for(Prodotto p: prodottoRepository.findByCategoria(daEliminare))
        {
            for(Acquisto a: acquistoRepository.findByProdottoVenduto(p))
                acquistoService.deleteAcquisto(a.getIdAcquisto());
            prodottoRepository.deleteByIdProdotto(p.getIdProdotto());
        }

        //eliminiamo la Categoria:
        categoriaRepository.deleteByNome(nome);
        return daEliminare;
    }
}
