package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule

object GraphDslModule : AbstractModule() {

	var graphViewExternalFunctionsFactory: () -> GraphViewExternalFunctions = { GraphViewExternalFunctions() }
	var usecaseActionExternalFunctions: UsecaseActionExternalFunctions = GraphUsecaseActionExternalFunctions
	var usecaseTestExternalFunctions: UsecaseTestExternalFunctions = GraphUsecaseTestExternalFunctions
	var scenarioExternalFunctions: ScenarioExternalFunctions = GraphScenarioExternalFunctions

	override fun initialize() {
		BaseModule.require()

		BaseModule.semanticAnalyserFactory = { st -> GraphDslSemanticAnalyser(st) }
		BaseModule.parserFactory = { p, s -> GraphDslParser(BaseModule.lexerFactory(p), s) }
		BaseModule.interpreterFactory = { n, m -> GraphDslInterpreter(n, m) }
	}
}