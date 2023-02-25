package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.math.LinearEquationSystemSolver
import ch.scorpion.jabbah.base.math.UndefinedLinearEquationSystemSolver
import ch.scorpion.jabbah.base.help.HelpProvider
import ch.scorpion.jabbah.base.time.ControlledTimeService
import ch.scorpion.jabbah.base.time.TimeService

/**
 * Module definitions for the [ch.scorpion.jabbah.base] package.
 */
object BaseModule : AbstractModule() {

	val properties: Properties = Properties()

    var settings: Settings = Settings()

    var eventBus: EventBus = EventBusImpl()

    var timeService: TimeService = ControlledTimeService()

	var lexerFactory: LexerFactory = { program -> Lexer(program) }

	var semanticAnalyserFactory: SemanticAnalyserFactory = { symbolTable -> SemanticAnalyser(symbolTable) }

	var parserFactory: ParserFactory = { program, semanticAnalyser -> Parser(lexerFactory(program), semanticAnalyser) }

	var storingActivationRecordFactory: ActivationRecordFactory = { name, parent -> StoringActivationRecord(name, parent) }

	var interpreterFactory: InterpreterFactory = { node, memory -> Interpreter(node, memory) }

	var dslGlobalFunctions: DslGlobalFunctions = DslGlobalFunctions()

	var baseDocumentationUrl: (() -> String)? = null

	var linearEquationSystemSolver: LinearEquationSystemSolver = UndefinedLinearEquationSystemSolver

	lateinit var helpProvider: HelpProvider

    override fun initialize() {
	    Translations.addBundle("jabbah-base")
	    fillProperties(properties)
    }

	private fun fillProperties(properties: Properties) {
		properties.set(Language.PROP_LANGUAGE, Language.English.code)
		properties.set(LogSystem.PROP_LOG_LEVEL, LogLevel.Info.name)
		properties.set(PROP_BEGINNER_HELP_TOOLTIP, true)
		properties.set(DataLocation.PROP_DATA_LOCATION, DataLocation.Local.customName)
	}
}