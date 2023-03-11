package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice

interface GraphType {

	val typeName: String get() = toString()

	val customName: String

	val needsGraphViewForExecution: Boolean

	val isCombiningNets: Boolean

	/**
	 * Determines whether a [Graph] of this [GraphType] can contain [Vertice]s
	 * instantiated from the specified [LibraryElement]
	 */
	fun canImport(libraryElement: LibraryElement): Boolean = libraryElement.graphType === this

	fun <I: Any, O: Any> adaptTo(other: GraphType): GraphTypeSignalAdapter<I, O>

	fun <T: Any> createOscilloscopeProbeVertice(name: String? = null): OscilloscopeProbeVertice<T>
		= OscilloscopeProbeVertice(this)
}

object GenericGraphType: GraphType {

	override val customName: String = "generic"

	override val needsGraphViewForExecution: Boolean get() = false

	override val isCombiningNets: Boolean get() = true

	override fun <I: Any, O: Any> adaptTo(other: GraphType): GraphTypeSignalAdapter<I, O> {
		throw UnsupportedOperationException("not implemented")
	}
}

class GraphTypeRegistry {

	private val registeredGraphTypes = mutableSetOf<GraphType>()

	lateinit var default: GraphType
		private set

	val graphTypes: Set<GraphType> get() = registeredGraphTypes

	fun clear() {
		registeredGraphTypes.clear()
	}

	fun register(graphType: GraphType, asDefault: Boolean = false) {
		registeredGraphTypes.add(graphType)
		if (asDefault) {
			default = graphType
		}
	}

	fun withCustomName(customName: String): GraphType =
		registeredGraphTypes.firstOrNull { it.customName == customName }
			?: throw IllegalArgumentException("unknown GraphType '$customName'")
}

/**
 * Adapts signals in a [Graph] of one [GraphType] to those in another [GraphType].
 * @param I the type of signal in the inner [Graph]
 * @param O the type of signal in the outer [Graph]
 */
interface GraphTypeSignalAdapter<I: Any, O: Any> {

	/**
	 * Converts a [signal] from an outer [Graph] to a signal of type [I] in an inner [Graph].
	 */
	fun convertIncomingSignal(signal: O?): I?

	fun convertOutgoingSignal(signal: I?): O?
}
