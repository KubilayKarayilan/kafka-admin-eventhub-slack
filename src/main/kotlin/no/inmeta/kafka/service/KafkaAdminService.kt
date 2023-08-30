package no.inmeta.kafka.service

import java.util.LinkedList
import java.util.concurrent.ExecutionException
import kotlin.math.abs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import no.inmeta.kafka.dto.MattermostDto
import reactor.core.publisher.Mono

@Service
class KafkaAdminService(
    private val adminClient: AdminClient,
    private val eventhubConsumer: KafkaConsumer<String, String>,
    private val mattermostWebClient: WebClient,
) {
    private val advarsel = ":warning:"
    private val alarm = ":alert:"
    private val info = ":info:"
    private val konsumentGroupIdCache = mutableListOf<String>()

    @Scheduled(fixedDelay = 3000, initialDelay = 2000)
    fun hentKonsumentOffset() {
        val eventHubs = adminClient.listTopics()
        eventHubs.listings().get().forEach {
            println(it)
        }
        val consumerGroups = adminClient.listConsumerGroups().all().get()
        val consumerGroupIds = consumerGroups.map { it.groupId() }
        consumerGroupIds.forEach {
            if (it !in konsumentGroupIdCache) {
                konsumentGroupIdCache.add(it)
                sendAlarmTilMattermost("$info a new consumer $it group joined to eventhub")
                println("adding consumer group id $it")
            }
        }
        if (konsumentGroupIdCache.size > consumerGroups.size) {
            val consumerGroupsDiff = konsumentGroupIdCache.filterNot { consumerGroupIds.contains(it) }
            consumerGroupsDiff.forEach {
                sendAlarmTilMattermost(
                    "$advarsel consumer group $it has unregistered from cluster, it might be down",
                )
            }
            konsumentGroupIdCache.removeAll(consumerGroupsDiff)
        }
        konsumentGroupIdCache.forEach {
            println("group id $it")
            analyzeLag(it)
        }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun hentKonsumentGroupOffset(groupId: String): Map<TopicPartition, Long> {
        val info = adminClient.listConsumerGroupOffsets(groupId)
        val topicPartitionOffsetAndMetadataMap: Map<TopicPartition, OffsetAndMetadata> =
            info.partitionsToOffsetAndMetadata().get()
        val groupOffset: MutableMap<TopicPartition, Long> = HashMap()
        for ((key, metadata) in topicPartitionOffsetAndMetadataMap) {
            groupOffset.putIfAbsent(TopicPartition(key.topic(), key.partition()), metadata.offset())
        }
        return groupOffset
    }

    private fun hentProdusentOffset(consumerGrpOffset: Map<TopicPartition, Long>): Map<TopicPartition?, Long?>? {
        val topicPartitions: MutableList<TopicPartition> = LinkedList()
        for ((key) in consumerGrpOffset) {
            topicPartitions.add(TopicPartition(key.topic(), key.partition()))
        }
        return eventhubConsumer.endOffsets(topicPartitions)
    }

    private fun beregnLag(
        consumerGrpOffsets: Map<TopicPartition, Long>,
        producerOffsets: Map<TopicPartition?, Long?>?,
    ): Map<TopicPartition, Long> {
        val lags: MutableMap<TopicPartition, Long> = HashMap()
        for ((key) in consumerGrpOffsets) {
            val producerOffset = producerOffsets?.get(key)
            val consumerOffset = consumerGrpOffsets[key]
            val lag = abs(producerOffset!! - consumerOffset!!)
            lags.putIfAbsent(key, lag)
        }
        return lags
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun analyzeLag(groupId: String?) {
        val consumerGrpOffsets = hentKonsumentGroupOffset(
            groupId!!,
        )
        val producerOffsets = hentProdusentOffset(
            consumerGrpOffsets,
        )
        val lags = beregnLag(consumerGrpOffsets, producerOffsets)
        for ((key, lag) in lags) {
            val topic = key.topic()
            val partition = key.partition()
            println("topic:$topic, partition $partition, lag $lag")
            if (lag > 29) {
                val alertType = if (lag in 30..50) advarsel else alarm
                sendAlarmTilMattermost(
                    "$alertType topic:$topic, partition $partition har lag $lag" +
                        " sjekk om konsumentene i $groupId er oppe eller prosseserer melding fort nok",
                )
            }
        }
    }
    fun sendAlarmTilMattermost(alertInfo: String) {
        mattermostWebClient
            .post()
            .body(
                Mono.just(MattermostDto(alertInfo, "eventhub vaktmester", "eventhub")),
                MattermostDto::class.java,
            )
            .retrieve()
            .toEntity(String::class.java)
            .subscribe()
    }
}
