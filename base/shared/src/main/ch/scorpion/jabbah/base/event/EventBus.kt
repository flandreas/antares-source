package ch.scorpion.jabbah.base.event

import kotlin.reflect.KClass

typealias EventHandler<T> = (T) -> Unit
typealias VetoHandler<T> = (T) -> Unit

class VetoException: Throwable()

/**
 * A central event dispatcher according to the whiteboard pattern.
 */
interface EventBus {

    /** Registers a handler function to be called whenever an event with the specified class is posted. */
    fun <T: Any> register(eventClass: KClass<out T>, handler: EventHandler<T>)

    /** Unregisters a handler function from listening to events of the specified class.*/
    fun <T: Any> unregister(eventClass: KClass<out T>, handler: EventHandler<T>)

    /** Unregisters a handler function from listening to any events.*/
    fun unregister(handler: EventHandler<Any>)

    /** Posts an event by calling all handlers that have been registered for the class of the event. */
    fun post(event: Any)

    /**
     * Posts an event by calling all handlers that have been registered for the class of the event.
     * If one of the handlers throws a [VetoException], the specified [undoEvent] is sent to all handlers
     * that have already processed the event, and the specified [vetoHandler] is called in order to give
     * the initiator of the event a chance to undo things.
     * @param thenHandler the code to be executed if no veto occurred
     * @param elsehandler the code to be executed if a veto occurred
     */
    fun postVetoable(event: Any, undoEvent: Any, thenHandler: VetoHandler<Any> = {}, elseHandler: VetoHandler<Any> = {})
}

class EventBusImpl : EventBus {

    /** Maps event simple class names to all handlers that have been registered for that event class.*/
    private val registrations: MutableMap<String, MutableList<EventHandler<Any>>> = mutableMapOf()

    /** ---- [EventBus] interface */

    override fun <T: Any> register(eventClass: KClass<out T>, handler: EventHandler<T>) {
        registrations.getOrPut(
            eventClass.simpleName!!,
            { mutableListOf<(Any) -> Unit>()})
        .add(handler as (Any) -> Unit)
    }

    override fun <T : Any> unregister(eventClass: KClass<out T>, handler: EventHandler<T>) {
        unregister(eventClass.simpleName!!, handler as EventHandler<Any>)
    }

    override fun unregister(handler: EventHandler<Any>) {
        registrations.keys.forEach { unregister(it, handler) }
    }

    override fun post(event: Any) {
        registrations.get(event::class.simpleName)?.toList()?.forEach { it.invoke(event) }
    }

    override fun postVetoable(event: Any, undoEvent: Any, thenHandler: VetoHandler<Any>, elseHandler: VetoHandler<Any>) {
        val successHandlers = mutableListOf<EventHandler<Any>>()
        try {
            registrations.get(event::class.simpleName)?.forEach {
                it.invoke(event)
                successHandlers.add(it)
            }
	        thenHandler.invoke(event)
        } catch (x: VetoException) {
            successHandlers.forEach {it.invoke(undoEvent)}
            elseHandler.invoke(event)
        }
    }

    /** ---- [EventBusImpl] */

    private fun unregister(eventClassName: String, handler: EventHandler<Any>) {
        registrations.get(eventClassName)?.remove(handler)
    }
}