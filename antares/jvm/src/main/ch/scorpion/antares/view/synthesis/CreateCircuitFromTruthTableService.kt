package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest

/**
 * Service for creating a combinational circuit from a [TruthTable].
 */
class CreateCircuitFromTruthTableService(
	private val eventBus: EventBus = BaseModule.eventBus
) {
	companion object {
		private val LOG by logger(CreateCircuitFromTruthTableService::class)
	}

	/**
	 * Creates a new [MetaGraph] for the given [TruthTable] in the same directory.
	 * @throws CircuitFromTruthTableBuilderError if the required gate input counts exceed the system limit
	 */
	fun create(item: TruthTableLibraryItem, circuitName: String) {
		val metaGraph = createMetaGraph(item.truthTable, circuitName)

		with (item.library!!) {
			val dir = libraryService.getDirectoryOf(this, item)
			val element = libraryService.addContainerLibraryElement(this, metaGraph, dir)
			eventBus.post(OpenContainerLibraryElementRequest(element))
		}

	}

	private fun createMetaGraph(truthTable: TruthTable, circuitName: String): MetaGraph {
		val metaGraph = MetaGraph.withName(circuitName)
		CircuitFromTruthTableBuilder(truthTable, metaGraph.graph).build()
		return metaGraph
	}
}