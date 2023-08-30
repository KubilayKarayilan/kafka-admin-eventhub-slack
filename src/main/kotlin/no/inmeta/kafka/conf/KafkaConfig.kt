package no.inmeta.kafka.conf

import java.util.Properties
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(
    @Value("\${spring.application.name}") val appName: String,
    @Value("\${kafka.bootstrapServers}") val bootstrapServers: String,
    @Value("\${kafka.sharedAccessKey}") val sharedAccessKey: String,
) {
    @Bean
    fun adminClient(): AdminClient? {
        val config = Properties()
        config[AdminClientConfig.CLIENT_ID_CONFIG] = appName
        config[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        config[SaslConfigs.SASL_MECHANISM] = "PLAIN"
        config[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
        config[SaslConfigs.SASL_JAAS_CONFIG] = hentSaslConfig()
        return AdminClient.create(config)
    }

    @Bean
    fun eventHubConsumer(): KafkaConsumer<String, String> {
        val properties = Properties()
        properties[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        properties[ConsumerConfig.CLIENT_ID_CONFIG] = "inmeta-kafka-admin-consumer"
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "inmeta-kafka-admin-consumer-group"
        properties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        properties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        properties[SaslConfigs.SASL_MECHANISM] = "PLAIN"
        properties[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
        properties[SaslConfigs.SASL_JAAS_CONFIG] = hentSaslConfig()
        return KafkaConsumer(properties)
    }
    private fun hentSaslConfig(): String {
        return "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"\$ConnectionString\"" +
            " password=\"Endpoint=sb://$bootstrapServers/;" +
            "SharedAccessKeyName=listenSAS;SharedAccessKey=$sharedAccessKey\";"
    }
}
