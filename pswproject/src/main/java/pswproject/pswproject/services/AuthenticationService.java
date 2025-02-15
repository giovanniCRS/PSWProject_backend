package pswproject.pswproject.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pswproject.pswproject.Exceptions.EccezioneEmailGiaUtilizzata;
import pswproject.pswproject.Exceptions.EccezioneUtenteNonEsistente;
import pswproject.pswproject.dto.AuthResponseDto;
import pswproject.pswproject.entities.Carrello;
import pswproject.pswproject.entities.Utente;
import pswproject.pswproject.entities.UtenteSecurity.Role;
import pswproject.pswproject.repositories.RoleRepository;
import pswproject.pswproject.repositories.UtenteRepository;

//componente univoco che si occupa dell'aspetto logico delle operazioni.
@Service
public class AuthenticationService {

    @Autowired                                           //rappresenta che l'oggetto in questione, univoco per il be, venga iniettato in questo componente.
    private UtenteRepository utenteRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
 
    @Autowired
    private AuthenticationManager authenticationManager; //si occupa dell'autenticazione dell'Utente.

    @Autowired
    private TokenService tokenService;                   //si occupa di generare dei token per l'Utente.


    @Transactional(readOnly = false)
    public Utente registraUtente( String nome, String cognome, String email, String password ) throws EccezioneEmailGiaUtilizzata{ //chiede alla repository di registrare un Utente.

        //controlliamo se già esiste un utente registrato con questa email:
        if (utenteRepository.existsByEmail(email)) {
            throw new EccezioneEmailGiaUtilizzata("Email già in utilizzo!");
        }

        //codifichiamo la password:
        //String encodedPassword = passwordEncoder.encode(password);

        //da questo endpoint diamo il ruolo predefinito di Utente.
        Role userRole = roleRepository.findByAuthority("USER").get();
        Set<Role> authorities = new HashSet<>();
        authorities.add(userRole);

        //generiamo un oggetto Utente da salvare nella repository:
        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome(nome); nuovoUtente.setCognome(cognome);
        nuovoUtente.setEmail(email); nuovoUtente.setPassword(passwordEncoder.encode(password));
        nuovoUtente.setAuthorities(authorities);
       // nuovoUtente.setPassword(password);

        //generiamo un Carrello per il nuovo Utente:
        List<Carrello> ordini = new ArrayList<>();
        nuovoUtente.setOrdini(ordini);

        //salviamo il nuovo Utente:
        Utente u = utenteRepository.save(nuovoUtente);
        /* 
        Authentication auth =null ;
        //Autenticazione:
        
        try {
             auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password)) ;
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        //generiamo un nuovo Token:
        String token = tokenService.generateJwt(auth);*/
        //restituiamo una risposta adatta al fe:
        return utenteRepository.save(u);
    }

    @Transactional(readOnly = false)
    public AuthResponseDto loginUtente(String username, String password) throws EccezioneUtenteNonEsistente{ //chiede all'AuthenticationManager di loggare un Utente.

        //controlliamo se non esiste già un utente registrato con questa email:
        if (!utenteRepository.existsByEmail(username)) {
            throw new EccezioneUtenteNonEsistente("L'utente non esiste!");
        }

        try{

            //Autenticazione:
            Authentication auth = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(username, password));

            //generiamo un nuovo Token:
            String token = tokenService.generateJwt(auth);
            

            //restituiamo una risposta adatta al fe:
            return new AuthResponseDto(utenteRepository.findByEmail(username).get(), token);

        }catch(AuthenticationException e){
            return new AuthResponseDto(null, ""); //in caso di non avvenuto Login, restituiamo una risposta vuota, così che il fe possa prenderne atto.
        }
    }
}
