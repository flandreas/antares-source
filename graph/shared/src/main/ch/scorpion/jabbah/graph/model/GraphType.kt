package ch.scorpion.jabbah.graph.model

interface GraphType {
	val customName: String
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