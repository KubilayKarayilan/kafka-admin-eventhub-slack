package no.inmeta.kafka.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import com.fasterxml.jackson.databind.JsonNode


/*
 * An example controller that shows how to do a REST call and how to do an operation with a operations metrics
 * There should be a metric called http_client_requests http_server_requests and operations
 * The controller also shows an example of S3 usage
 */
@RestController
class ExampleController(
    private val restTemplate: RestTemplate,
) {

    @GetMapping("/api/example/ip")
    fun ip(): Map<String, Any> {
        val response = restTemplate.getForObject("http://httpbin.org/ip", JsonNode::class.java)
        val ip = response?.get("origin")?.textValue() ?: "Ip missing from response"
        return mapOf("ip" to ip)
    }

}
