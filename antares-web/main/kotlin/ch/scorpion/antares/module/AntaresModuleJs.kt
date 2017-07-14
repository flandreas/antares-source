package ch.scorpion.antares.module

import ch.scoprion.jabbah.graph.library.RestLibraryService
import ch.scorpion.antares.script.ScriptEngineJs
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.module.EditModuleJs
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.script.ScriptModule

/**
 * Module definition of the [ch.scorpion.antares] module for the JS platform.
 */
object AntaresModuleJs : AbstractModule() {

    override fun initialize() {
        ScriptModule.scriptEngineProvider = { ScriptEngineJs() }
        EditModuleJs.require()
        AntaresViewModule.require()

        LibraryModule.libraryService = RestLibraryService()
        LibraryModule.libraryFactory = { LibraryImpl("library.lib", null) }
        LibraryModule.libraryHolder = LibraryHolder(LibraryModule.libraryFactory.invoke())
    }
}