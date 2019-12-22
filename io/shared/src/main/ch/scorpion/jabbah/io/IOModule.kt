package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Module definitions for the [ch.scorpion.jabbah.io] module.
 */
object IOModule : AbstractModule() {

    var typeMap: TypeMap = TypeMapImpl()
    
    var storableCreator: StorableCreator = SystemStorableCreator()
    
    override fun initialize() {
        BaseModule.require()
    }
}