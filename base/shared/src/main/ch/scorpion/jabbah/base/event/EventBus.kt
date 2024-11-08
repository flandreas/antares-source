package ch.scorpion.jabbah.base.event

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import kotlin.reflect.KClass

typealias EventHandler<T> = (T) -> Unit
typealias VetoHandler<T> = (T) -> Unit

/**
 * Represents a veto for a particular action.
 * @param msg an internationalized message to be displayed, if necessary.
 */
class VetoException(msg: String): Throwable(msg)

/**
 * A central event dispatcher according to the whiteboard pattern.
 */
interface EventBus {

	/** Returns the number of registered [EventHandler]s. */
	val size: Int

    /** Registers a handler function to be called whenever an event with the specified class is posted. */
    fun <T: Any> register(eventClass: KClass<out T>, handler: EventHandler<T>)

    /** Unregisters a handler function from listening to events of the specified class.*/
    fun <T: Any> unregister(eventClass: KClass<out T>, handler: EventHandler<T>)

    /** Unregisters a handler function from listening to any events.*/
    fun unregister(handler: EventHandler<*>)

    /** Posts an event by calling all handlers that have been registered for the class of the event. */
    fun post(event: Any)

    /**
     * Posts an event by calling all handlers that have been registered for the class of the event.
     * If one of the handlers throws a [VetoException], the specified [undoEvent] is sent to all handlers
     * that have already processed the event, and the specified [elseHandler] is called in order to give
     * the initiator of the event a chance to undo things.
     * @param thenHandler the code to be executed if no veto occurred
     * @param elseHandler the code to be executed if a veto occurred
     */
    @Deprecated("Use postTwoPhase")
    fun postVetoable(event: Any, undoEvent: Any, thenHandler: VetoHandler<Any> = {}, elseHandler: VetoHandler<VetoException> = {})

	fun postTwoPhase(prepareEvent: Any, execEvent: Any, thenHandler: (() -> Unit)? = null)

	fun postTwoPhase(prepareEvent: Any, thenHandler: () -> Unit)

	/** Unregisters all [EventHandler]s. Primarily for integration testing.*/
	fun clear()

    fun createStatistics(): EventBusStatistics

    fun printRegistrations(eventClass: KClass<*>): String
}

class EventBusImpl : EventBus {

    companion object {
        private val LOG by logger(EventBusImpl::class)
    }

    /** Maps event simple class names to all handlers that have been registered for that event class.*/
    private val registrations: MutableMap<String, MutableList<EventHandler<Any>>> = mutableMapOf()

    /** ---- [EventBus] interface */

	override val size: Int get() = registrations.size

    override fun <T: Any> register(eventClass: KClass<out T>, handler: EventHandler<T>) {
        val eventName = getEventClassName(eventClass)
        LOG.debug("EventBus ${hashCode()}: Register $eventClass")

        val list = registrations.getOrPut(eventName) { mutableListOf() }
        if (!list.contains(handler)) {
            @Suppress("UNCHECKED_CAST")
            list.add(handler as (Any) -> Unit)
        }
    }

    override fun <T : Any> unregister(eventClass: KClass<out T>, handler: EventHandler<T>) {
        val eventName = getEventClassName(eventClass)
	    @Suppress("UNCHECKED_CAST")
        unregister(eventName, handler as EventHandler<Any>)
    }

    override fun unregister(handler: EventHandler<*>) {
        registrations.keys.forEach { unregister(it, handler) }
    }

    override fun post(event: Any) {
        registrations[getEventClassName(event)]?.toList()?.forEach { it.invoke(event) }
    }

    @Deprecated("Use postTwoPhase")
    override fun postVetoable(event: Any, undoEvent: Any, thenHandler: VetoHandler<Any>, elseHandler: VetoHandler<VetoException>) {
        val successHandlers = mutableListOf<EventHandler<Any>>()
        try {
            registrations[getEventClassName(event)]?.forEach {
                it.invoke(event)
                successHandlers.add(it)
            }
	        thenHandler.invoke(event)
        } catch (x: VetoException) {
            successHandlers.forEach {it.invoke(undoEvent)}
            elseHandler.invoke(x)
        }
    }

	override fun postTwoPhase(prepareEvent: Any, execEvent: Any, thenHandler: (() -> Unit)?) {
		try {
			registrations[getEventClassName(prepareEvent)]?.forEach {
				it.invoke(prepareEvent)
			}
			registrations[getEventClassName(execEvent)]?.forEach {
				it.invoke(execEvent)
			}
			thenHandler?.invoke()
		} catch (e: VetoException) {
			// do nothing, operation vetoed
		}
	}

	override fun postTwoPhase(prepareEvent: Any, thenHandler: () -> Unit) {
		try {
			registrations[getEventClassName(prepareEvent)]?.forEach {
				it.invoke(prepareEvent)
			}
			thenHandler()
		} catch (e: VetoException) {
			// do nothing, operation vetoed
		}
	}

	override fun clear() {
		registrations.clear()
	}

    /** ---- [EventBusImpl] */

    private fun unregister(eventClassName: String, handler: EventHandler<*>) {
        LOG.debug("Unregister $eventClassName")
        registrations[eventClassName]?.remove(handler)
    }

    private fun <T: Any> getEventClassName(eventClass: KClass<out T>): String = System.getClassName(eventClass)

    private fun getEventClassName(event: Any): String = System.getClassName(event)

    override fun createStatistics(): EventBusStatistics =
        EventBusStatistics(hashCode().toString()).apply {
            registrations.entries.forEach { entry -> addRegistrationCount(entry.key, entry.value.size) }
        }

    override fun printRegistrations(eventClass: KClass<*>): String {
        val sb = StringBuilder()
        val eventName = getEventClassName(eventClass)
        sb.appendLine("Registrations for event $eventName:")
        registrations[eventName]?.forEach {
            sb.appendLine("- ${it::class.qualifiedName}")
        }
        return sb.toString()
    }
}