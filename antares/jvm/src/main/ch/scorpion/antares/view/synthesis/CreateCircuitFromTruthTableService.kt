package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest

class CreateCircuitFromTruthTableService(
	private val eventBus: EventBus = BaseModule.eventBus
) {

	fun create(item: TruthTableLibraryItem, circuitName: String) {
		val metaGraph = createMetaGraph(item.truthTable, circuitName)

		with (item.library!!) {
			val dir = libraryService.getDirectoryOf(this, item)
			val element = libraryService.addContainerLibraryElement(this, metaGraph, dir)
			eventBus.post(OpenContainerLibraryElementRequest(element))
		}

	}

	private fun createMetaGraph(truthTable: TruthTable, circuitName: String): MetaGraph {
		// TODO
		return MetaGraph.withName(circuitName)
	}
}