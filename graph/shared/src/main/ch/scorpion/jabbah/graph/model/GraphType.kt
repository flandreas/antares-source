package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.graph.library.LibraryElement

interface GraphType {

	val typeName: String get() = toString()

	val customName: String

	/**
	 * Determines whether a [Graph] of this [GraphType] can contain [Vertice]s
	 * instantiated from the specified [LibraryElement]
	 */
	fun canImport(libraryElement: LibraryElement): Boolean = libraryElement.graphType === this
}

object GenericGraphType: GraphType {
	override val customName: String = "generic"
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