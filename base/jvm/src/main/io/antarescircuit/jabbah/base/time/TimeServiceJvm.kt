package io.antarescircuit.jabbah.base.time

/** Provides wall-clock milliseconds and monotonic nanoseconds for measuring elapsed time. */
class RealTimeServiceJvm : TimeService {

	override fun nowMillis(): Long = java.lang.System.currentTimeMillis()

	override fun nowNanos(): Long = java.lang.System.nanoTime()
}
