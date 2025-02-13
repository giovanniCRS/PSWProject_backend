package pswproject.pswproject.entities;

import java.util.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import pswproject.pswproject.entities.UtenteSecurity.Role;

//lombok
@ToString
@EqualsAndHashCode

//java.persistance mappa l'entità su una tabella del database.
@Getter
@Setter
@Entity
@Table(name = "utente")
public class Utente implements UserDetails{ //implementa questa interfaccia che offre metodi per la gestione dell'account, soprattutto ci permette di avere dei ruoli con relativi permessi d'accesso.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //notazione che lascia al database la decisione sui valori da assegnare.
    @Column(name = "idUtente", nullable = false)
    private int idUtente;

    @Basic
    @Column(name = "nome", length = 1024)
    private String nome;

    @Basic
    @Column(name = "cognome", length = 1024)
    private String cognome;

    @Basic
    @Column(name = "email", length = 1024)//questo campo all'atto pratico rappresenta l'identificativo univoco di ciascun utente.
    private String email;

    @Basic
    @Column(name = "password", length = 1024)
    private String password;

    @OneToMany(targetEntity = Carrello.class, mappedBy = "utenteAssociato", cascade = CascadeType.MERGE) //relazione 1 a molti : a 1 utente corrispondono più ordini (MERGE prende le colonne e le carica assieme).
    @JsonIgnore                                                                                          //non possiamo portarci dietro questo campo in quanto l'oggetto JSON sarebbe ciclico, rappresenterebbe la relazione ricorsivamente (in Carrello è presente UtenteAssociato).
    private List<Carrello> ordini;                                                                       //lato be i Carrelli associati all'Utente sono acquisti già avvenuti (Storico acquisti).

    //Sicurezza 
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.DETACH}) //relazione molti a molti : a molti utenti corrispondono più ruoli, nel nostro caso 2 'ADMIN', 'USER', ma la logica può essere modificata in seguito (DETACH fa si che 1 colonna carichi l'altra, quindi se viene eliminata 1 si elimina anche l'altra)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "idUtente"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))  //questa notazione crea un'altra tabella nel database non associata a un'entità, che rappresenta la relazione molti a molti.
    private Set<Role> authorities;                                                            //usiamo un Set che impone il fatto che non ci possano essere ruoli duplicati.

    public Utente(){
        super();
        this.authorities=new HashSet<Role>();
    }

    public Utente(int id, String name, String surname, String email, String password, Set<Role> authorities){
        super();
        this.idUtente=id;
        this.nome=name;
        this.cognome=surname;
        this.email=email;
        this.password=password;
        //this.authorities=authorities;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { //uno dei metodi di UserDetails.
        return this.authorities;
    }

    public void setAuthorities(Set<Role> auth) {
        this.authorities=auth;
    }

    public int getIdUtente(){
        return this.idUtente;
    }

    public void setIdUtente(int id){
        this.idUtente=id;
    }

    
    @Override
    public String getUsername() { //l'email in quanto identificante, corrisponde al campo username di UserDetails.
        return this.email;
    }

    public void setUsername(String username) {
        this.email=username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email=email;
    }

     
    @Override
    public String getPassword() { //uno dei metodi di UserDetails.
        return this.password;
    }

    public void setPassword(String password) {
        this.password=password;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome=nome;
    }

    public String getCognome() {
        return this.cognome;
    }

    public void setCognome(String cognome) {
        this.cognome=cognome;
    }

    public List<Carrello> getOrdini(){
        return this.ordini;
    }

    public void setOrdini(List<Carrello> ordini){
        this.ordini=ordini;
    }
 
    @Override
    public boolean isAccountNonExpired() { //uno dei metodi di UserDetails.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() { //uno dei metodi di UserDetails.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { //uno dei metodi di UserDetails.
        return true;
    }

    @Override
    public boolean isEnabled() { //uno dei metodi di UserDetails.
        return true;
    }



}
