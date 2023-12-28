package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice

interface GraphType: Bean {

	val typeName: String get() = toString()

	val customName: String

	val needsGraphViewForExecution: Boolean

	val isCombiningNets: Boolean

	/**
	 * Checks whether a [Graph] of this [GraphType] can contain [Vertice]s
	 * instantiated from the specified [LibraryElement].
	 * @return a translated message explaining why import is not possible, `null` if import is possible
	 */
	fun checkImport(libraryElement: LibraryElement): String? =
		if (libraryElement.graphType === this) {
			null
		} else {
			Translations.getString("graph.graphTypeError.msg", libraryElement.graphType, this)
		}

	/**
	 * Yields an object that adapts signals from this [GraphType] to signals of [other] [GraphType],
	 * and vice versa.
	 */
	fun <I: Any, O: Any> adaptTo(other: GraphType): GraphTypeSignalAdapter<I, O>

	/**
	 * Converts a literal value, typically coming from a script and of basic type such as [Long] or [Float],
	 * to a specific signal type defined by higher-level code.
	 */
	fun literalToSignal(literal: Any): Any

	fun <T: Any> createOscilloscopeProbeVertice(name: String? = null): OscilloscopeProbeVertice<T>
		= OscilloscopeProbeVertice(this)
}

object GenericGraphType: GraphType {

	override val customName: String = "generic"

	override val needsGraphViewForExecution: Boolean get() = false

	override val isCombiningNets: Boolean get() = true

	override fun <I: Any, O: Any> adaptTo(other: GraphType): GraphTypeSignalAdapter<I, O> =
		GenericGraphTypeSignalAdapter as GraphTypeSignalAdapter<I, O>

	override fun literalToSignal(literal: Any): Any = literal
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

object GenericGraphTypeSignalAdapter : GraphTypeSignalAdapter<Any, Any> {

	override fun convertIncomingSignal(signal: Any?): Any? = signal

	override fun convertOutgoingSignal(signal: Any?): Any? = signal

}
