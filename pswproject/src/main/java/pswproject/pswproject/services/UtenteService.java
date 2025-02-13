package pswproject.pswproject.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pswproject.pswproject.Exceptions.EccezioneAcquistoNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneCarrelloNonEsistente;
import pswproject.pswproject.Exceptions.EccezioneEmailGiaUtilizzata;
import pswproject.pswproject.Exceptions.EccezioneUtenteNonEsistente;
import pswproject.pswproject.entities.Carrello;
import pswproject.pswproject.entities.Utente;
import pswproject.pswproject.entities.UtenteSecurity.Role;
import pswproject.pswproject.repositories.CarrelloRepository;
import pswproject.pswproject.repositories.RoleRepository;
import pswproject.pswproject.repositories.UtenteRepository;

//componente univoco che si occupa dell'aspetto logico delle operazioni.
@Service
public class UtenteService implements UserDetailsService{
    
    @Autowired //rappresenta che l'oggetto in questione, univoco per il be, venga iniettato in questo componente.
    private UtenteRepository utenteRepository;

    @Autowired
    private CarrelloService carrelloService;

    @Autowired
    private CarrelloRepository carrelloRepository;

    //Security
    @Autowired
    private PasswordEncoder encoder; //utilizziamo un unico codificatore per la password dell'Utente (codifica uniforme).

    @Autowired
    private RoleRepository roleRepository;


    /*------------------------------------READ------------------------------------*/

    @Transactional(readOnly = true)
    public List<Utente> getAll() //interroga la repository per ottenere tutti gli Utenti.
    {
        return utenteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utente getUtente(int id) throws EccezioneUtenteNonEsistente //interroga la repository per ottenere l'Utente corrispondente, altrimenti non esiste.
    {
        Optional<Utente> o = utenteRepository.findByIdUtente(id);
        if(o.isPresent())
            return o.get();
        else
            throw new EccezioneUtenteNonEsistente("Utente non esistente!");
    }

    @Transactional(readOnly = true)
    public Utente getUtente(String email) throws EccezioneUtenteNonEsistente //interroga la repository per ottenere l'Utente corrispondente, altrimenti non esiste.
    {
        Optional<Utente> o = utenteRepository.findByEmail(email);
        if(o.isPresent())
            return o.get();
        else
            throw new EccezioneUtenteNonEsistente("Utente non esistente!");
    }

    /*------------------------------------WRITE------------------------------------*/

    @Transactional(readOnly = false)
    public Utente saveUtente(Utente u) throws EccezioneEmailGiaUtilizzata //chiede alla repository di salvare l'Utente, altrimenti esiste già.
    {
        //controlliamo se già esiste un utente registrato con questa email:
        if (utenteRepository.existsByEmail(u.getEmail())) {
            throw new EccezioneEmailGiaUtilizzata("Email già in utilizzo!");
        }

        //codifichiamo la password:
        String encodedPassword = encoder.encode(u.getPassword());

        //da questo endpoint diamo i ruoli decisi dall'admin:
        Set<Role> authorities = new HashSet<>();
        for(GrantedAuthority r : u.getAuthorities())
        {
            Role userRole = roleRepository.findByAuthority(r.getAuthority()).get();
            authorities.add(userRole);
        }
            

        //generiamo un oggetto utente da salvare nella repository:
        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome(u.getNome()); nuovoUtente.setCognome(u.getCognome());
        nuovoUtente.setEmail(u.getEmail()); nuovoUtente.setPassword("{bcrypt}"+encodedPassword);
        nuovoUtente.setAuthorities(authorities);

        List<Carrello> ordini = new ArrayList<>();
        nuovoUtente.setOrdini(ordini);

        //salviamo il nuovo utente:
        return utenteRepository.save(nuovoUtente);
    }

    @Transactional(readOnly = false)
    public Utente updateUtente(String email, String password, String nome, String cognome, Collection<? extends GrantedAuthority> collection) throws EccezioneUtenteNonEsistente { //chiede alla repository di aggiornare l'Utente, altrimenti non esiste.

        //controlliamo se esiste:
        if(!utenteRepository.existsByEmail(email))
            throw new EccezioneUtenteNonEsistente("L'utente non esiste.");

        //prendiamo l'Utente da aggiornare dalla repository :
        Utente daAggiornare = utenteRepository.findByEmail(email).get();
        daAggiornare.setPassword("{bcrypt}"+encoder.encode(password));
        daAggiornare.setNome(nome);
        daAggiornare.setCognome(cognome);

        //da questo endpoint diamo i ruoli decisi dall'admin:
        Set<Role> authorities = new HashSet<>();
        for(GrantedAuthority r : collection)
        {
            Role userRole = roleRepository.findByAuthority(r.getAuthority()).get();
            authorities.add(userRole);
        }
        daAggiornare.setAuthorities(authorities);

        //salviamo di nuovo l'utente:
        return utenteRepository.save(daAggiornare);
    }

    /*------------------------------------DELETE------------------------------------*/

    @Transactional(readOnly = false)
    public Utente eliminaUtente(String email) throws EccezioneUtenteNonEsistente, EccezioneAcquistoNonEsistente, EccezioneCarrelloNonEsistente { //chiede alla repository di eliminare l'Utente, altrimenti non esiste.

        //controlliamo se esiste:
        if(!utenteRepository.existsByEmail(email))
            throw new EccezioneUtenteNonEsistente("L'utente da eliminare non esiste.");
        
        //prendiamo l'Utente da eliminare dalla repository :
        Utente daEliminare = utenteRepository.findByEmail(email).get();

        //eliminiamo i suoi Carrelli, chiedendo al carrelloService di farlo:
        for(Carrello c: carrelloRepository.findByUtenteAssociato(daEliminare))
            carrelloService.deleteCarrello(c.getIdCarrello());

        //eliminiamo l'utente:
        utenteRepository.deleteByEmail(email);
        return daEliminare;
    }

    /*------------------------------------SECURITY------------------------------------*/

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //unico metodo di UserDetailsService

        return utenteRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Utente non esistente!")); //il nostro username corrisponde all'email, caricare un Utente per il suo username corrisponde dunque a trovare l'Utente per mezzo dell'email sulla nostra repository.
    }
}