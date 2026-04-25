package io.antarescircuit.jabbah.edit.drag

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.edit.Component
import kotlin.reflect.KClass

/** Implemented by [Component] that are generally willing to receive other [Component] being dragged onto them.*/
interface DragDestination : Component {

	/** Returns `true` if this [DragDestination] accepts [component] being dragged onto it.*/
	fun acceptDrag(component: Component): Boolean
}

interface DragDestinationHighlight : Drawable {
	fun handleDragged(component: Component, destination: DragDestination)
}

fun interface DragDestinationHighlightFactory<T : DragDestination> {
	fun create(destination: T): DragDestinationHighlight
}

class DragDestinationHighlightFactoryRegistry {

	/** Maps [KClass] names of destination [Component]s to their [DragDestinationHighlightFactory].*/
	private val registry: MutableMap<String, DragDestinationHighlightFactory<*>> = mutableMapOf()

	fun <T : DragDestination> register(destinationClass: KClass<out T>, factory: DragDestinationHighlightFactory<T>) {
		registry[System.getClassName(destinationClass)] = factory
	}

	fun <T : DragDestination> create(destination: T): DragDestinationHighlight? =
		@Suppress("UNCHECKED_CAST")
		(registry[System.getClassName(destination)] as DragDestinationHighlightFactory<T>?)?.create(destination)
}