package pswproject.pswproject.dto;


import lombok.Data;

//lombok
@Data
public class LoginDto{ //rappresenta una richiesta di login proveniente dal fe verso il be.

    //Indichiamo una coppia username-password utile per far si che UserDetails possa far loggare l'utente.
    private String email;
    private String password;

    public LoginDto(){
        super();
    }

    public LoginDto( String email, String password ){
        super();
        this.email=email;
        this.password=password;
    }
}
