package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Module definitions for the [ch.scorpion.jabbah.io] module.
 */
object IOModule : AbstractModule() {

    var typeMap: TypeMap = TypeMapImpl()
    
    override fun initialize() {
        BaseModule.require()
        configureTypeMap(typeMap)
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("string", StringStorable::class)
    }
}