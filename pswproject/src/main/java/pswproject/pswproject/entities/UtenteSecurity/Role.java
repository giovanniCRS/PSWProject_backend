package pswproject.pswproject.entities.UtenteSecurity;

import org.springframework.security.core.GrantedAuthority;
import jakarta.persistence.*;
import lombok.*;

//lombok
@Setter
@Getter

//java.persistance mappa l'entità su una tabella del database.
@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority{ //i Ruoli sono imposti (stringhe identificative), le GrantedAuthority sono autorità che assegnano le operazioni eseguibili da ciascun ruolo (sempre predefinite). 

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) //notazione che lascia al database la strategia sui valori da assegnare.
    @Column(name="id")
    private int id;

    private String authority; //rappresenta il ruolo.

    public Role(){
        super();
    }

    public Role(String authority){
        this.authority=authority;
    }

    public Role(int id, String authority){
        this.id=id;
        this.authority=authority;
    }

    @Override
    public String getAuthority() { //unico metodo di GrantedAuthority.
        return this.authority;
    }

}