package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.expression.DslBooleanExpressionWriter
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToBooleanExpression
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableService
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.NarrowContainerDrawingFiller
import ch.scorpion.jabbah.graph.library.LibraryItem
import ch.scorpion.jabbah.graph.library.OpenContainerLibraryElementRequest

/**
 * Service for creating a combinational circuit from a [TruthTable].
 */
class CreateCircuitFromTruthTableService(
	private val truthTableService: TruthTableService = AntaresModelModule.truthTableService,
	private val eventBus: EventBus = BaseModule.eventBus
) {
	companion object {
		private val LOG by logger(CreateCircuitFromTruthTableService::class)
	}

	/**
	 * Creates a new [MetaGraph] for the given [TruthTable] in the same directory.
	 * @throws CircuitFromTruthTableBuilderError if the required gate input counts exceed the system limit
	 */
	fun create(truthTable: TruthTable, item: LibraryItem, circuitName: String, circuitType: CircuitSynthesisType) {
		LOG.debug("Create Circuit from TruthTable in directory ${item.name.value}")

		val dnfs = truthTableService.generateDnfs(truthTable)
		val executionScript = createExecutionScript(truthTable, dnfs)

		val metaGraph = createMetaGraph(truthTable, dnfs, circuitName, circuitType, executionScript)
		with (item.library!!) {
			val dir = libraryService.getDirectoryOf(this, item)
			val element = libraryService.addContainerLibraryElement(this, metaGraph, dir)
			eventBus.post(OpenContainerLibraryElementRequest(element))
		}
	}

	private fun createExecutionScript(truthTable: TruthTable, dnfs: List<DNF>): String {
		val result = StringBuilder()
		dnfs.forEachIndexed { index, dnf ->
			val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = true).build()
			val statement = DslBooleanExpressionWriter().write(
				truthTable,
				expression,
				truthTable.inputColumnCount + index,
				omitAndForSingleCharacterVariables = false)
			result.append(statement)
			result.appendLine()
		}
		return result.toString()
	}

	private fun createMetaGraph(
		truthTable: TruthTable,
		dnfs: List<DNF>,
		circuitName: String,
		circuitType: CircuitSynthesisType,
		executionScript: String
	): MetaGraph {
		val metaGraph = MetaGraph.withName(circuitName)
		metaGraph.graph.model!!.script = executionScript
		circuitType.build(truthTable, dnfs, metaGraph.graph)
		NarrowContainerDrawingFiller(metaGraph.graph.graphView, metaGraph.containerDrawing).fill()
		return metaGraph
	}
}