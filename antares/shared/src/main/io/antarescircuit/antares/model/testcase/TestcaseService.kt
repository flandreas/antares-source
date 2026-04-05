package io.antarescircuit.antares.model.testcase

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.model.param.GraphParamValues
import io.antarescircuit.jabbah.graph.model.semantic.GraphSemantic
import io.antarescircuit.jabbah.io.StorableCloner

object TestcaseService {

	fun runAllLibraryTests(library: Library): List<CombinedTestRunResult> {
		val results = mutableListOf<CombinedTestRunResult>()
		library.metaGraphIds.forEach { uuid ->
			val metaGraph = library.getMetaGraph(uuid)
			if (metaGraph.type == AntaresGraphTypes.Digital) {
				val circuit = metaGraph.graph.model!! as DigitalGraph
				results.addAll(run(metaGraph, circuit.testcases.testcases))
			}
		}
		return results
	}

	/**
	 * Used by various types of "Run" methods to run [Testcase] of a particular [MetaGraph].
	 * Encapsulates [DigitalGraph] cloning/disposing and setup of [GraphParamValues].
	 */
	fun run(metaGraph: MetaGraph, testcases: List<Testcase>): List<CombinedTestRunResult> {
		val circuit = metaGraph.graph.model as DigitalGraph

		// Clone circuit to avoid interference from various objects of the main application,
		// such as GraphViewExecutionAnimator that listen on Actors of the main circuit
		val clone = StorableCloner.clone(circuit)
		clone.name = Name(circuit.name.translation)
		clone.uuid = circuit.uuid

		try {
			// Setup parameter values (generics)
			clone.parameterDefinitions = metaGraph.parameterDefinitions
			clone.parameterValues = GraphParamValues.withDefaults(metaGraph.parameterDefinitions)

			val execScriptAST = circuit.script?.let {
				if (StringUtils.isNotBlank(it)) {
					circuit.createParser(it, null).parse()
				} else {
					null
				}
			}

			val subGraphPropagationDelay = getSubGraphVerticePropagationDelay(clone)

			val results = mutableListOf<CombinedTestRunResult>()
			for (testcase in testcases) {
				if (testcase.ignored) {
					results.add(CombinedTestRunResult.ignored(circuit, testcase))
				} else {
					results.add(
						CombinedTestcaseRunner(testcase, clone, execScriptAST, subGraphPropagationDelay) {
							(metaGraph.containerDrawing.getPortViewComponent(it)?.port as DigitalPort?)
								?.logic ?: Logic.POSITIVE
						}.run()
					)
				}
			}
			return results
		} finally {
			clone.dispose()
		}
	}

	private fun getSubGraphVerticePropagationDelay(circuit: DigitalGraph): Long {
		return (circuit.parameterValues.firstOrNullWithSemantic(GraphSemantic.PropagationDelay)?.value as LongValue?)?.value
			?: circuit.effectivePropagationDelay
	}
}