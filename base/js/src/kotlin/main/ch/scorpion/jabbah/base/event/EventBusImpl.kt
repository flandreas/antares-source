package ch.scorpion.jabbah.base.event

import kotlin.reflect.KClass

/**
 * This implementation of [EventBus] for JS uses simple event class names because reflection is not yet
 * supported in Kotlin on the JS platform.
 */
actual class EventBusImpl : AbstractEventBus(), EventBus {

	override fun <T : Any> getEventClassName(eventClass: KClass<out T>): String = eventClass.simpleName!!

	override fun getEventClassName(event: Any): String = event::class.simpleName!!
}