package ch.scorpion.jabbah.base.event

/**
 * A [PropertyChangeEvent] gets delivered to [PropertyChangeListener]s when a bound property of a bean changes.
 */
data class PropertyChangeEvent<out T> (
	val source: Any,
	val name: String,
	val oldValue: T?,
	val newValue: T?)

/**
 * Listens to [PropertyChangeEvent]s from beans.
 */
interface PropertyChangeListener<in T: Any> {
    fun propertyChanged(e: PropertyChangeEvent<T>)
}

/**
 * Utility class for managing [PropertyChangeListener]s and firing [PropertyChangeEvent]s.
 */
class PropertyChangeSupport<T: Any>(var source: Any) {

    private val listeners: MutableList<PropertyChangeListener<T>> by lazy { mutableListOf() }

    /** Adds the specified [PropertyChangeListener] to listen for [PropertyChangeEvent]s from [source].*/
    fun add(l: PropertyChangeListener<T>) {
        if (!listeners.contains(l)) {
            listeners.add(l)
        }
    }

	fun add(handler: (PropertyChangeEvent<T>) -> Unit): PropertyChangeListener<T> {
		val listener = object : PropertyChangeListener<T> {
			override fun propertyChanged(e: PropertyChangeEvent<T>) {
				handler.invoke(e)
			}
		}
		add(listener)
		return listener
	}

    /** Removes the specified [PropertyChangeListener] to stop listening for [PropertyChangeEvent]s from [source].*/
    fun remove(l: PropertyChangeListener<T>) = listeners.remove(l)

    /** Sends the specified [PropertyChangeEvent] to all registered [PropertyChangeListener]s.*/
    private fun fire(event: PropertyChangeEvent<T>) = listeners.toList().forEach { it.propertyChanged(event) }

    /** Sends a [PropertyChangeEvent] with the specified values to all registered [PropertyChangeListener]s.*/
    fun fire(name: String, oldValue: T?, newValue: T?) = fire(PropertyChangeEvent(source, name, oldValue, newValue))
}

interface PropertyOwner<T : Any> {
	var source: Any
	fun addPropertyChangeListener(l: PropertyChangeListener<T>)
	fun removePropertyChangeListener(l: PropertyChangeListener<T>)
	fun fire(name: String, oldValue: T?, newValue: T?)
}

class PropertyOwnerImpl<T: Any>() : PropertyOwner<T> {

	private lateinit var pcSupport: PropertyChangeSupport<T>

	override var source: Any
		get() = pcSupport.source
		set(value)  {
			pcSupport = PropertyChangeSupport(value)
		}

	override fun addPropertyChangeListener(l: PropertyChangeListener<T>) {
		pcSupport.add(l)
	}

	override fun removePropertyChangeListener(l: PropertyChangeListener<T>) {
		pcSupport.remove(l)
	}

	override fun fire(name: String, oldValue: T?, newValue: T?) {
		pcSupport.fire(name, oldValue, newValue)
	}
}


