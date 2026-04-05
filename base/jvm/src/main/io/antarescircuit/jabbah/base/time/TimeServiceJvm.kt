package io.antarescircuit.jabbah.base.time

import java.util.*

/**
 * A [TimeService] implementation on the JVM that provides the real time.
 */
class RealTimeServiceJvm : TimeService {

    override fun nowMillis(): Long = Date().time

    override fun nowNanos(): Long = 1_000_000 * nowMillis()
}

