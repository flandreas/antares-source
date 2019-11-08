package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import kotlin.reflect.KClass

/**
 * A [SignalHandler] implementation that reflects a signal to the [Actor] immediately.
 * Primarily used for testing.
 */
class ForwardSignalHandler : SignalHandler {

	override var isDeepExecution: Boolean
		get() = true
		set(@Suppress("UNUSED_PARAMETER") value) {
			// empty
		}

	override val executionTime: Long get() = 0

	override fun logTrace(clazz: KClass<*>, id: Int, msg: () -> String) {
		// empty
	}

	override fun logActorTrace(actor: Actor, msg: () -> String) {
		// empty
	}

	override fun requestActingAfter(actor: Actor, delay: Long, data: ActorData) {
		actor.act(this, data)
	}

	override fun requestActingTimeFreeze(actor: Actor, data: ActorData) {
		actor.act(this, data)
	}

	override fun actingDone(actor: Actor, data: ActorData?) {
		// empty
	}
}