package pswproject.pswproject;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/* import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import pswproject.pswproject.entities.Utente;
import pswproject.pswproject.entities.UtenteSecurity.Role;
import pswproject.pswproject.repositories.RoleRepository;
import pswproject.pswproject.repositories.UtenteRepository; */

@SpringBootApplication
public class PswprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(PswprojectApplication.class, args);
	}


/*
	//Configurazione iniziale di un Admin che può essere attiva / disattivata a piacimento.

	@Bean
	CommandLineRunner run(RoleRepository roleRepository, UtenteRepository utenteRepository, PasswordEncoder passwordEncoder)
	{
		return args ->{
			if(roleRepository.findByAuthority("ADMIN").isPresent()) return;
			Role adminRole = roleRepository.save(new Role("ADMIN"));
			roleRepository.save(new Role("USER"));

			Set<Role> roles =  new HashSet<>();
			roles.add(adminRole);
			
			Utente admin = new Utente(3, "admin", "admin", "admin", "{bcrypt}"+passwordEncoder.encode("password"), roles);

			utenteRepository.save(admin);
		};
	}

	*/
}
