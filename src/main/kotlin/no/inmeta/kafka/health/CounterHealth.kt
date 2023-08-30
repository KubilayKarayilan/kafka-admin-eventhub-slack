package no.inmeta.kafka.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * A sample custom health check. You can add your own health checks that verifies the proper operational status of your
 * application.
 */
@Component
class CounterHealth() : HealthIndicator {

    override fun health(): Health {
        return Health.up()
            .build()
    }
}
