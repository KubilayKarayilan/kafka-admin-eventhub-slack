package no.inmeta.kafka.conf

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun mattermostWebClient(): WebClient {
        return WebClient.builder()
            //TODO: slackify
            .baseUrl("https://chat.sits.no/hooks/cm6box5zgp8qj8c8ont7q36dzo")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}
