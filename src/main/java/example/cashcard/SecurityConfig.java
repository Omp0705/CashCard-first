package example.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request-> request
                        .requestMatchers("/cashcards/**").hasRole("CARD_OWNER"))
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf->csrf.disable());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder){
        User.UserBuilder users = User.builder();
        UserDetails om = users
                .username("om1")
                .password(passwordEncoder.encode("abc12"))
                .roles("CARD_OWNER")
                .build();
        // does not own any card
        UserDetails sara = users
                .username("sara1")
                .password(passwordEncoder.encode("1234"))
                .roles("NOT_CARD_OWNER")
                .build();
        // one dummy user
        UserDetails kumar = users
                .username("kumar2")
                .password(passwordEncoder.encode("xy12"))
                .roles("CARD_OWNER")
                .build();

        return new InMemoryUserDetailsManager(om,sara,kumar);


    }
}