package pswproject.pswproject.services;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

//questo componente si occupa della codifica dei Bearer Token.
@Service
public class TokenService {
    
    @Autowired                     //rappresenta che l'oggetto in questione, univoco per il be, venga iniettato in questo componente.
    private JwtEncoder jwtEncoder; //esegue la codifica.

    @SuppressWarnings({"unused"}) 
    @Autowired
    private JwtDecoder jwtDecoder; //esegue la decodifica.

    public String generateJwt(Authentication auth){ //genera un Token JWT.

        //rappresentiamo l'istante di creazione del Token:
        Instant now = Instant.now();

        //prendiamo le autorità (per cui il Token sarà valido):
        String scope = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

        //settiamo le caratteristiche del Token creato:
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .subject(auth.getName())
            .claim("roles", scope)
            .build();

        //restituiamo una codifica del token:
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
