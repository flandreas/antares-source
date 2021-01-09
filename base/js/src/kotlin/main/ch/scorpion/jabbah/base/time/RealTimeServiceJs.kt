package ch.scorpion.jabbah.base.time

import kotlin.js.Date

class RealTimeServiceJs : TimeService {

	override fun nowMillis(): Long = Date.now().toLong()

	override fun nowNanos(): Long = 1_000_000 * nowMillis()
}