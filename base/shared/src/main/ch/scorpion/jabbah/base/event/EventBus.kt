package ch.scorpion.jabbah.base.event

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

	fun postTwoPhase(prepareEvent: Any, execEvent: Any)
}

abstract class AbstractEventBus : EventBus {

    /** Maps event simple class names to all handlers that have been registered for that event class.*/
    private val registrations: MutableMap<String, MutableList<EventHandler<Any>>> = mutableMapOf()

	abstract fun <T: Any> getEventClassName(eventClass: KClass<out T>): String

	abstract fun getEventClassName(event: Any): String

    /** ---- [EventBus] interface */

    override fun <T: Any> register(eventClass: KClass<out T>, handler: EventHandler<T>) {
        @Suppress("UNCHECKED_CAST")
        registrations
	        .getOrPut(getEventClassName(eventClass)) { mutableListOf()}
	        .add(handler as (Any) -> Unit)
    }

    override fun <T : Any> unregister(eventClass: KClass<out T>, handler: EventHandler<T>) {
	    @Suppress("UNCHECKED_CAST")
        unregister(getEventClassName(eventClass), handler as EventHandler<Any>)
    }

    override fun unregister(handler: EventHandler<*>) {
        registrations.keys.forEach { unregister(it, handler) }
    }

    override fun post(event: Any) {
        registrations[getEventClassName(event)]?.toList()?.forEach { it.invoke(event) }
    }

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

	override fun postTwoPhase(prepareEvent: Any, execEvent: Any) {
		try {
			registrations[getEventClassName(prepareEvent)]?.forEach {
				it.invoke(prepareEvent)
			}
			registrations[getEventClassName(execEvent)]?.forEach {
				it.invoke(execEvent)
			}
		} catch (e: VetoException) {
			// do nothing, operation vetoed
		}
	}

    /** ---- [EventBusImpl] */

    private fun unregister(eventClassName: String, handler: EventHandler<*>) {
        registrations[eventClassName]?.remove(handler)
    }
}

expect class EventBusImpl() : EventBus