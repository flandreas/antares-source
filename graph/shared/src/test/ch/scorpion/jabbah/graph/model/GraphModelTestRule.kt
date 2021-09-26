package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [ch.scorpion.jabbah.graph.model] package.
 */
object GraphModelTestRule {

    fun configure() {
	    BaseModule.require()
	    IOModule.require()
        IOModule.typeMap.register("testVertice", TestVertice::class)
        GraphModelModule.require()
    }
}
