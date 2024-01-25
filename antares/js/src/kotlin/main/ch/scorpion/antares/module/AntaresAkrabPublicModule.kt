package ch.scorpion.antares.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.AkrabRestLibraryPersistenceServiceJs
import ch.scorpion.jabbah.graph.project.ProjectModule

object AntaresAkrabPublicModule : AbstractModule() {

    override fun initialize() {
        AntaresModuleJs.require()

        // TODO: This doesn't exist yet
        ProjectModule.projectLibraryPersistenceService = AkrabRestLibraryPersistenceServiceJs(
            baseUrl = "/public/api/projects",
            dictionaryName = "circuits"
        )
    }
}