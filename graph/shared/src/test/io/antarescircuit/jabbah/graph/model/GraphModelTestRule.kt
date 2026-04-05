package io.antarescircuit.jabbah.graph.model

import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.io.IOModule

/**
 * Basic setup of unit tests in the [io.antarescircuit.jabbah.graph.model] package.
 */
object GraphModelTestRule {

    fun configure() {
        GraphModelModule.reset()

	    BaseModule.require()
	    IOModule.require()
        IOModule.typeMap.register("testVertice", TestVertice::class)
        GraphModelModule.require()
    }
}
