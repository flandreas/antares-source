package ch.scorpion.antares.filebased.library

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.AntaresApplication
import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.app.AbstractDesktopApplication
import ch.scorpion.jabbah.app.CurrentApplicationVersion
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.FileLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.ResourceLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.nonvolatile.NonVolatileServiceJvm
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.project.ProjectManagementService
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.assertEquals

abstract class AbstractSystemLibraryTest : AbstractCircuitTest() {

    companion object {

        @JvmStatic
        protected fun configure() {
            CurrentApplicationVersion.version = AbstractDesktopApplication.readVersion()

            AntaresTestRule.configure()
            LibraryModule.DEF_LIBRARY_UUID = AntaresApplication.DEF_LIBRARY_UUID

            // System library loaded as JVM resources
            LibraryModule.systemLibraryPersistenceService = ResourceLibraryPersistenceService()
            LibraryModule.systemLibraryDictionaryService = LibraryDictionaryService(ResourceLibraryDictionaryPersistenceService())
            LibraryModule.libraryManagementService = LibraryManagementService()

            // User project in temporary files
            val tempDir = Files.createTempDirectory(null)
            ProjectModule.projectLibraryPersistenceService = FileLibraryPersistenceService({ tempDir.absolutePathString() }, "projects")
            ProjectModule.projectLibraryService = LibraryService(userLibraryPersisterProvider = { ProjectModule.projectLibraryPersistenceService } )
            ProjectModule.projectDictionaryService = LibraryDictionaryService(FileLibraryDictionaryPersistenceService({ tempDir.absolutePathString() }, "projects"))
            ProjectModule.projectManagementService = ProjectManagementService()

            //val path = Paths.get("jvm/rsc/test").toAbsolutePath()
            GraphModelModule.nonVolatileService = NonVolatileServiceJvm({ tempDir.toString() }, "nonVolatile")
        }
    }

    protected lateinit var openedCircuitView: GraphView

    protected lateinit var subGraphVV: SubGraphVerticeView<SubGraphVerticeRef>

    override fun getCircuitView(): GraphView = openedCircuitView

    protected fun openCircuitWithElement(uuid: UUID) {
        // Create new user project
        val properties = LibraryProperties(
            name = TranslatableText("test"),
            importUuid = AntaresApplication.DEF_LIBRARY_UUID)
        val project = ProjectModule.projectManagementService.create(properties)
        ProjectModule.projectManagementService.open(LibraryIdentification(project.uuid, null))

        openedCircuitView = LibraryModule.libraryHolder.library.getMetaGraph(
            (project.directory.getItems().first() as ContainerLibraryElement).uuid
        ).graph.graphView

        subGraphVV = LibraryModule.libraryHolder.library.getContainerLibraryElement(uuid)!!.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView
        openedCircuitView.add(subGraphVV)
    }

    protected fun execute(inputs: Map<String, DigitalSignal>, outputs: Map<String, DigitalSignal>) {
        try {
            scheduler.isDeepExecution = false
            startSimulation()
            proceedUntilQueueIsEmpty()

            val executionTime = scheduler.executionTime
            val propagationDelay = subGraphVV.model.propagationDelay.value

            // Capture initial output values
            val initialOutputValues = mutableMapOf<String, DigitalSignal>()
            subGraphVV.model.getOutputs().forEach {
                if (it.name != null) {
                    initialOutputValues[it.name!!] = it.getOutgoingSignal() as DigitalSignal
                }
            }

            // Apply test inputs
            inputs.forEach { entry ->
                subGraphVV.model.getInput<DigitalSignal>(entry.key).setIncomingSignal(entry.value, scheduler)
            }

            // Make sure that outputs are NOT available before propagationDelay is over,
            // i.e. that the initial output values are still present
            proceedToNanos(executionTime + propagationDelay - 1)
            subGraphVV.model.getOutputs().forEach {
                if (it.name != null) {
                    val initialOutputValue = initialOutputValues[it.name!!]
                    if (initialOutputValue != null) {
                        assertEquals(initialOutputValue, it.getOutgoingSignal() as DigitalSignal)
                    }
                }
            }

            // Make sure that outputs are available after propagationDelay is over.
            proceedToNanos(executionTime + propagationDelay + 1)
            outputs.forEach { entry ->
                assertEquals(entry.value, subGraphVV.model.getOutput<DigitalSignal>(entry.key).getOutgoingSignal())
            }

        } finally {
            scheduler.isActive = false
            timeService.reset()
        }
    }
}