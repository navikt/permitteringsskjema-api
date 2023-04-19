package no.nav.permitteringsskjemaapi.config

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(Info().title("API for permitteringsskjema"))
            .externalDocs(
                ExternalDocumentation()
                    .description("Repo på github")
                    .url("https://github.com/navikt/permitteringsskjema-api")
            )
    }
}