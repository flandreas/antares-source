package ch.scorpion.jabbah.base.event

import kotlin.reflect.KClass

/**
 * This implementation of [EventBus] for the JVM uses qualified event class names to allow obfuscation.
 */
actual class EventBusImpl : AbstractEventBus(), EventBus {

	override fun <T : Any> getEventClassName(eventClass: KClass<out T>): String = eventClass.qualifiedName!!

	override fun getEventClassName(event: Any): String = event::class.qualifiedName!!
}