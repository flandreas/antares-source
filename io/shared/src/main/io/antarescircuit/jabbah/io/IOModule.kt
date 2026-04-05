package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.module.BaseModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.io] module.
 */
object IOModule : AbstractModule() {

    var typeMap: TypeMap = TypeMapImpl()
    
    override fun initialize() {
        BaseModule.require()
        configureTypeMap(typeMap)
    }

    override fun resetDependencies() {
        BaseModule.reset()
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("string", StringStorable::class)
    }
}