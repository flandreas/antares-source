package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.expression.DslBooleanExpressionWriter
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.quinemccluskey.DnfToBooleanExpression
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.antares.model.testcase.TestcaseScriptGenerator
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.view.synthesis.CircuitSynthesisType
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.AbstractContainerDrawingFiller
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryDirectory
import io.antarescircuit.jabbah.graph.library.Library

/**
 * Creates circuits from truth tables, ported from the desktop's `CreateCircuitFromTruthTableService`
 * (which lives in the JVM-only UI module). The circuit building itself is shared code.
 */
internal object WebSynthesis {

	/** The synthesis types offered for [truthTable], as on the desktop. */
	fun types(truthTable: TruthTable?): List<CircuitSynthesisType> =
		CircuitSynthesisType.entries.filter { truthTable == null || truthTable.stateColumnCount == 0 || it.supportFSM }

	/**
	 * Adds a new circuit built from [truthTable] to [directory] of [library] and returns it.
	 * @throws io.antarescircuit.antares.view.synthesis.CircuitFromTruthTableBuilderError if gate input limits are exceeded
	 */
	fun create(
		truthTable: TruthTable,
		library: Library,
		directory: LibraryDirectory,
		circuitName: String,
		type: CircuitSynthesisType,
		twoInputGatesOnly: Boolean,
		createTestcase: Boolean,
	): ContainerLibraryElement {
		MetaGraph.validateName(TranslatableText(circuitName))
		val combinational = truthTable.stateColumnCount == 0
		val metaGraph = MetaGraph.create(TranslatableText(circuitName), AntaresGraphTypes.Digital)
		metaGraph.graph.model!!.script = if (combinational) executionScript(truthTable) else null
		type.build(truthTable, twoInputGatesOnly && type.supportTwoInputGatesOnly, metaGraph.graph)
		AbstractContainerDrawingFiller.fillStandard(metaGraph)
		if (createTestcase && combinational) {
			val testcaseName = truthTable.name.value.ifBlank { circuitName }
			(metaGraph.graph.model as DigitalGraph).testcases.add(Testcase(testcaseName, TestcaseScriptGenerator.generate(truthTable)))
		}
		return library.libraryService.addContainerLibraryElement(library, metaGraph, directory)
	}

	/** The circuit's execution script: the minimized function of each output as DSL statement. */
	private fun executionScript(truthTable: TruthTable): String = buildString {
		AntaresModelModule.truthTableService.generateDnfs(truthTable).forEachIndexed { index, dnf ->
			val expression = DnfToBooleanExpression(truthTable, dnf, andParenthesis = true).build()
			appendLine(
				DslBooleanExpressionWriter().write(
					truthTable,
					expression,
					truthTable.inputColumnCount + index,
					omitAndForSingleCharacterVariables = false,
				)
			)
		}
	}
}
