package hexlet.code.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.datafaker.Faker;

@Configuration
public class FakerConfig {

    @Bean
    public Faker getFaker() {
        return new Faker();
    }
}
