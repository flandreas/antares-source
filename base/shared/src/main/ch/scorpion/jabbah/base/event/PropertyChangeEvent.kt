package ch.scorpion.jabbah.base.event

/**
 * A [PropertyChangeEvent] gets delivered to [PropertyChangeListener]s when a bound property of a bean changes.
 */
data class PropertyChangeEvent<out T> (val source: Any, val name: String, val oldValue: T?, val newValue: T?)

/**
 * Listens to [PropertyChangeEvent]s from beans.
 */
interface PropertyChangeListener<in T: Any> {
    fun propertyChanged(e: PropertyChangeEvent<T>)
}

/**
 * Utility class for managing [PropertyChangeListener]s and firing [PropertyChangeEvent]s.
 */
class PropertyChangeSupport<T: Any>(val source: Any) {

    private val listeners: MutableList<PropertyChangeListener<T>> by lazy { mutableListOf<PropertyChangeListener<T>>() }

    /** Adds the specified [PropertyChangeListener] to listen for [PropertyChangeEvent]s from [source].*/
    fun add(l: PropertyChangeListener<T>) {
        if (!listeners.contains(l)) {
            listeners.add(l)
        }
    }

    /** Removes the specified [PropertyChangeListener] to stop listening for [PropertyChangeEvent]s from [source].*/
    fun remove(l: PropertyChangeListener<T>) = listeners.remove(l)

    /** Sends the specified [PropertyChangeEvent] to all registered [PropertyChangeListener]s.*/
    private fun fire(event: PropertyChangeEvent<T>) = listeners.toList().forEach { it.propertyChanged(event) }

    /** Sends a [PropertyChangeEvent] with the specified values to all registered [PropertyChangeListener]s.*/
    fun fire(name: String, oldValue: T?, newValue: T?) = fire(PropertyChangeEvent(source, name, oldValue, newValue))
}


