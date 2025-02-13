package pswproject.pswproject.security;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

//lombok
@Getter
@Setter

//specifica le chiavi RSA per la codifica.
@Component
public class RSAKeyProperties {

    private RSAPublicKey publicKey;   //chiave pubblica.
    private RSAPrivateKey privateKey; //chiave privata.

    public RSAKeyProperties(){
        KeyPair pair = KeyGeneratorUtility.generateRsaKey();
        this.publicKey=(RSAPublicKey) pair.getPublic();
        this.privateKey=(RSAPrivateKey) pair.getPrivate();
    }
}
