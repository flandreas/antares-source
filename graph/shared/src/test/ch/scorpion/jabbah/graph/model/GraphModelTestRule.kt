package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.script.GraphScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptEngineJvm
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.IOModuleJvm
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.model] package.
 */
object GraphModelTestRule {

	/*
    override fun apply(statement: Statement?, p1: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                configure()
                try {
                    statement!!.evaluate()
                } finally {
                    // empty
                }
            }
        }
    }
    */

    fun configure() {
	    BaseModuleJvm.require()
	    IOModuleJvm.require()
        IOModule.typeMap.register("testVertice", TestVertice::class)
        ScriptModule.scriptEngineProvider = { ScriptEngineJvm() }
        ScriptModule.scriptGatewayProvider = { GraphScriptGateway(ScriptModule.scriptEngineProvider.invoke()) }
        GraphModelModule.require()
    }
}
