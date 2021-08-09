package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyOwner
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyOwnerImpl

/**
 * An [ApplicationContext] is a group of central objects used by core application classes such as [View].
 * Rather than making them globally accessible, multiple [ApplicationContext]s can exist at the same time,
 * forming a kind of parallel universes.
 *
 * A sample usage of this construct would be an application that supports simulation by a simulator,
 * where multiple objects such as [View] depend on the same simulator instance. For each simulation universe,
 * a new [ApplicationContext] would be spawned, so that they can be started and stopped independently.
 */
typealias ApplicationContext = Any

/**
 * Holds the [ApplicationContext] to be added to the [DrawContext] of a [View].
 */
open class ApplicationContextHolder(
	applicationContext: ApplicationContext? = null,
	private val propertyOwner: PropertyOwner<Any> = PropertyOwnerImpl()
) : PropertyOwner<Any> by propertyOwner {

	companion object {
		const val PROP_APPLICATION_CONTEXT = "PROP_APPLICATION_CONTEXT"
	}

	/**
	 * Holds the current (immutable) [ApplicationContext] object.
	 * Changing posts a [PROP_APPLICATION_CONTEXT] [PropertyChangeEvent] to all registered [PropertyChangeListener]s.
	 */
	var applicationContext: ApplicationContext? = applicationContext
		protected set(value) {
			val oldValue = field
			field = value
			propertyOwner.fire(PROP_APPLICATION_CONTEXT, oldValue, value)
		}

	init {
		propertyOwner.source = this
	}

	open fun dispose() { }
}