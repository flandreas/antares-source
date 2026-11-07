package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.expression.DslBooleanExpressionWriter
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.quinemccluskey.DNF
import io.antarescircuit.antares.model.quinemccluskey.DnfToBooleanExpression
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.AbstractContainerDrawingFiller
import io.antarescircuit.jabbah.graph.library.LibraryItem
import io.antarescircuit.jabbah.graph.library.OpenContainerLibraryElementRequest

/**
 * Service for creating a combinational or sequential circuit from a [TruthTable].
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
		LOG.userTrail("Create Circuit from TruthTable in directory ${item.name.value}")

		val dnfs = truthTableService.generateDnfs(truthTable)
		val executionScript = if (truthTable.stateColumnCount == 0) {
			createExecutionScript(truthTable, dnfs)
		} else {
			null
		}

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
		executionScript: String?
	): MetaGraph {
		val metaGraph = MetaGraph.create(TranslatableText(circuitName), AntaresGraphTypes.Digital)
		metaGraph.graph.model!!.script = executionScript
		circuitType.build(truthTable, dnfs, metaGraph.graph)
		AbstractContainerDrawingFiller.fillStandard(metaGraph)
		return metaGraph
	}
}