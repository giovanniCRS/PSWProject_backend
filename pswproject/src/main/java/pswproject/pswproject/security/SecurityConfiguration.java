package pswproject.pswproject.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.*;

//rappresenta una configurazione per la gestione delle richieste in arrivo al be.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true)
public class SecurityConfiguration {

    private final RSAKeyProperties keys; //chiave per il meccanismo di codifica della password.

    //settiamo le chiavi per il PasswordEncoder:
    public SecurityConfiguration(RSAKeyProperties keys){
        this.keys=keys;
    }

    
        @Bean
        public PasswordEncoder passwordEncoder() {
            //return new BCryptPasswordEncoder();
            Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("bcrypt", new BCryptPasswordEncoder());
    return new DelegatingPasswordEncoder("bcrypt", encoders);
        }
    
 
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService detailsService){ //si occuperà di Autenticare l'Utente in base alla sessione.
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(detailsService);
        return new ProviderManager(daoProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{ //serie di filtri che si applicano alle richieste in arrivo al be ; i filtri non sono altro che delle regole che le richieste devono rispettare:
        return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authorizeRequests -> {
                authorizeRequests.requestMatchers( "/auth/**").permitAll();

                authorizeRequests.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                authorizeRequests.requestMatchers(HttpMethod.GET,"/users/all").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.GET,"/users/byId/**").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.GET,"/users/byEmail/**").hasAnyRole("USER","ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.POST,"/users").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.PUT,"/users/**").hasAnyRole("USER","ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.DELETE,"/users/**").hasAnyRole("USER","ADMIN");

                authorizeRequests.requestMatchers(HttpMethod.GET,"/products/**").permitAll();
                authorizeRequests.requestMatchers(HttpMethod.POST,"/products/add").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.PUT,"/products/**").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.DELETE,"/products/**").hasRole("ADMIN");

                authorizeRequests.requestMatchers(HttpMethod.GET,"/categories/**").permitAll();
                authorizeRequests.requestMatchers(HttpMethod.POST,"/categories").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.PUT,"/categories/**").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.DELETE,"/categories/**").hasRole("ADMIN");

                authorizeRequests.requestMatchers(HttpMethod.GET,"/carts/all").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.GET,"/carts/byId/**").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.GET,"/carts/byUser/**").hasAnyRole("USER","ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.POST,"/carts").hasAnyRole("USER","ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.PUT,"/carts/**").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.DELETE,"/carts/**").hasRole("ADMIN");

                authorizeRequests.requestMatchers(HttpMethod.GET,"/purchases/all").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.GET,"/purchases/byId/**").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.GET,"/purchases/byCart/**").hasAnyRole("USER","ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.POST,"/purchases/**").hasAnyRole("USER","ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.PUT,"/purchases/**").hasRole("ADMIN");
                authorizeRequests.requestMatchers(HttpMethod.DELETE,"/purchases/**").hasRole("ADMIN");

        authorizeRequests.anyRequest().authenticated(); //ogni richiesta che non rispetta nessuna delle regole precedenti dovrà essere Autenticata.
        })
        .oauth2ResourceServer(oauth -> oauth.jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter())) //specifichiamo il meccanismo della raccolta dei Token.
        )
        
        .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //specifichiamo una politica di Sessione senza stato (il be non memorizzerà nessun tipo di sessione).
        .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(){ //decodifica del Token JWT in arrivo.
        return NimbusJwtDecoder.withPublicKey(keys.getPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(){ //decodifica del Token JWT in uscita.
        JWK jwk = new RSAKey.Builder(keys.getPublicKey()).privateKey(keys.getPrivateKey()).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){ //setta autoimaticamente un prefisso 'ROLE_' per risolvere il conflitto di definizione tra GrantedAuthorities e i requestMatchers (che vogliono solo il nome del ruolo).
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtConverter;
    }
}
