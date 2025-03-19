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
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Base class for testing circuits in the standard library, both with deep execution and shallow execution.
 *
 * Does not only check output signals for given input signals, but also makes sure that
 * the configured overall propagation delay used for shallow execution is "near" the measured
 * maximum real circuit propagation delay. This is for verifying "...the scripted components don't accurately
 * simulate propagation delays..." claimed in #890 (Fidelity).
 */
abstract class AbstractSystemLibraryTest : AbstractCircuitTest() {

    companion object {

        /** The maximum accepted deviation factor when comparing real and scripted propagation delay.*/
        private const val DEVIATION = 0.1

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

    protected fun execute(pairs: List<Pair<Map<String, DigitalSignal>, Map<String, DigitalSignal>>>) {

        // First run: Deep, use real circuit. Measure the maximum execution duration.
        var maxDuration = 0L
        pairs.forEach {
            val duration = executeImpl(it.first, it.second, null, true)
            maxDuration = maxOf(maxDuration, duration)
        }

        // Assert that the measured maximum execution time is "near" the configured overall propagation delay
        // used for shallow scripted execution
        val propDelayDiff = abs(maxDuration - subGraphVV.model.propagationDelay.value)
        val delta = DEVIATION * maxDuration
        assertTrue(
            propDelayDiff < delta,
            "Prop delay diff $propDelayDiff is not near enough to $delta")

        // Second run: Shallow, using the configured overall propagation delay
        pairs.forEach {
            executeImpl(it.first, it.second, subGraphVV.model.propagationDelay.value, false)
        }
    }

    private fun executeImpl(
        inputs: Map<String, DigitalSignal>,
        outputs: Map<String, DigitalSignal>,
        propagationDelay: Long?,
        deep: Boolean
    ): Long {
        try {

            scheduler.isDeepExecution = deep
            startSimulation()
            proceedUntilQueueIsEmpty()

            val executionTime = scheduler.executionTime

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
            if (propagationDelay != null) {
                proceedToNanos(executionTime + propagationDelay - 1)
                subGraphVV.model.getOutputs().forEach {
                    if (it.name != null) {
                        val initialOutputValue = initialOutputValues[it.name!!]
                        if (initialOutputValue != null) {
                            assertEquals(initialOutputValue, it.getOutgoingSignal() as DigitalSignal)
                        }
                    }
                }
                proceedUntilQueueIsEmpty()
            }

            // Make sure that outputs are available after propagationDelay is over.
            if (propagationDelay != null) {
                proceedToNanos(executionTime + propagationDelay + 1)
            } else {
                proceedUntilQueueIsEmpty()
            }
            outputs.forEach { entry ->
                assertEquals(entry.value, subGraphVV.model.getOutput<DigitalSignal>(entry.key).getOutgoingSignal())
            }

            return scheduler.executionTime - executionTime

        } finally {
            scheduler.isActive = false
            timeService.reset()
        }
    }
}