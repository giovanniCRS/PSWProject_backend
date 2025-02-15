package pswproject.pswproject.security;

//import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Permetti richieste CORS da tutti i domini
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")  // L'indirizzo dell'app Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // I metodi HTTP permessi
                .allowedHeaders("*")  // Accetta tutti gli header
                .allowCredentials(true);  // Consente i cookie e le credenziali
    }
    
}
