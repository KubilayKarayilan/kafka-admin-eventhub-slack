@file:JvmName("Main")

package no.inmeta.kafka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class inmetaKakfaAdmin

/**
 * It is important not to remove the passing of args, as command line arguments is used for some configuration
 * in Aurora (logging configuration file, for example).
 *
 * We suppress the warning about copying the array, as this is not a large array anyway.
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<inmetaKakfaAdmin>(*args)
}
