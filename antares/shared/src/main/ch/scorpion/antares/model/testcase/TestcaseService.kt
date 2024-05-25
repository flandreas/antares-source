package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.io.StorableCloner

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
	 * Uses by various types of "Run" methods to run [Testcase] of a particular [MetaGraph].
	 * Encapsulates [DigitalGraph] cloning/disposing and setup of [GraphParamValues].
	 */
	fun run(metaGraph: MetaGraph, testcases: List<Testcase>): List<CombinedTestRunResult> {
		val circuit = metaGraph.graph.model as DigitalGraph

		// Clone circuit to avoid interference from various objects of the main application,
		// such as GraphViewExecutionAnimator that listen on Actors of the main circuit
		val clone = StorableCloner.clone(circuit)
		clone.name = Name(circuit.name.translation)

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

			val results = mutableListOf<CombinedTestRunResult>()
			for (testcase in testcases) {
				if (testcase.ignored) {
					results.add(CombinedTestRunResult.ignored(circuit, testcase))
				} else {
					results.add(CombinedTestcaseRunner(testcase, clone, execScriptAST) {
						(metaGraph.containerDrawing.getPortViewComponent(it)?.port as DigitalPort?)?.logic
							?: Logic.POSITIVE
					}.run())
				}
			}
			return results
		} finally {
			clone.dispose()
		}
	}
}