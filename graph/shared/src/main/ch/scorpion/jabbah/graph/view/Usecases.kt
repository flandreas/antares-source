package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.base.event.EventBus

/**
 * Encapsulates the [Usecase]s of a [GraphView] and the corresponding management methods
 * in a single object.
 */
interface Usecases : Storable {

	/**
	 * The [GraphView] that owns this [Usecases].
	 * Can be `null` in order to be instantiated by deserialization. Uses for sending events when adding or
	 * removing [Usecase]s.
	 */
	var graphView: GraphView?

	val isEmpty: Boolean

	val hasTest: Boolean get() = getUsecases().any { it.hasTest }

	fun dispose()

	fun getUsecases(): Iterable<Usecase>

	fun get(id: Int): Usecase

	/**
	 * Adds a new [Usecase] with the specified name as the last one in this [Usecases].
	 * Posts a [UsecaseAddedEvent] on this [Usecases]' [EventBus].
	 */
	fun add(name: String)

	/**
	 * Adds the specified [Usecase] as the last one in this [Usecases].
	 * Posts a [UsecaseAddedEvent] on this [Usecases]' [EventBus].
	 */
	fun add(usecase: Usecase)

	/**
	 * Adds the specified [Usecase] at the specified index to this [Usecase].
	 * Posts a [UsecaseAddedEvent] on this [Usecases]' [EventBus].
	 */
	fun add(usecase: Usecase, index: Int)

	/**
	 * Removes the specified [Usecase] from this [Usecases].
	 * Posts a [UsecaseRemovedEvent] on this [Usecases]' [EventBus].
	 */
	fun remove(usecase: Usecase)

	/** Returns the index of the specified [Usecase] (starting with 0) in this [Usecases].*/
	fun indexOfUsecase(usecase: Usecase): Int

	/** Returns all [Usecase] that contain a test.*/
	fun withTests(): List<Usecase>

}

data class UsecaseAddedEvent(val graphView: GraphView, val usecase: Usecase)
data class UsecaseRemovedEvent(val graphView: GraphView, val usecase: Usecase)