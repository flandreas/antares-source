package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.base.dsl.SemanticAnalyserFactory
import ch.scorpion.jabbah.base.module.BaseModule

object GraphDslModule : AbstractModule() {

	var graphViewExternalFunctionsFactory: () -> GraphViewExternalFunctions = { GraphViewExternalFunctions() }
	var usecaseExternalFunctions: UsecaseActionExternalFunctions = GraphUsecaseActionExternalFunctions

	override fun initialize() {
		BaseModule.require()

		BaseModule.semanticAnalyserFactory = SemanticAnalyserFactory { st -> GraphDslSemanticAnalyser(st) }
		BaseModule.parserFactory = ParserFactory { p, s -> GraphDslParser(BaseModule.lexerFactory(p), s) }
		BaseModule.interpreterFactory = { n, m -> GraphDslInterpreter(n, m) }

	}
}