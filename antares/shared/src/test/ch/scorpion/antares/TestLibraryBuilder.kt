package ch.scorpion.antares

import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

/**
 * A test utility class that builds Antares circuits using [TestCircuitBuilder], and adds them to the [Library].
 */
class TestLibraryBuilder(
    val portFactory: PortFactory
) {

    constructor(): this(GraphViewModule.portFactory)

    companion object {
        val CUSTOM_NOT = "CustomNOT"
        val CUSTOM_NAND = "CustomNAND"
    }

    /**
     * Adds a custom NOT (as of [TestCircuitBuilder.buildCustomNot]) to the specified [LibraryDirectory].
     * @return the created {@link MetaGraph} that contains the custom NOT
     */
    fun addCustomNot(libraryDirectory: LibraryDirectory): MetaGraph {
        val customNOT = TestCircuitBuilder(CUSTOM_NOT).buildCustomNot()
        val containerDrawing = createContainerDrawing(customNOT)
        val metaGraph = MetaGraph(GraphStorable(customNOT), containerDrawing)
        libraryDirectory.addContainerElement(metaGraph)
        return metaGraph
    }

    fun addCustomNand(libraryDirectory: LibraryDirectory): MetaGraph {
        val myNandCircuit = TestCircuitBuilder(CUSTOM_NAND).buildCustomNAND(createSubGraphVerticeView(CUSTOM_NOT, libraryDirectory))
        val containerDrawing = createContainerDrawing(myNandCircuit)
        val metaGraph = MetaGraph(GraphStorable(myNandCircuit), containerDrawing)
        libraryDirectory.addContainerElement(metaGraph)
        return metaGraph
    }

    private fun createSubGraphVerticeView(name: String, libraryDirectory: LibraryDirectory): SubGraphVerticeViewImpl {
        return (libraryDirectory.get(name) as LibraryElement).getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeViewImpl
    }

    private fun createContainerDrawing(circuitView: GraphView<*>): ContainerDrawing {
        val containerDrawing = GraphViewModule.createContainerDrawing()

        containerDrawing.model.graphUUID = circuitView.graph!!.uuid
        containerDrawing.model.name = circuitView.graph!!.name

        for (circuitInput in circuitView.graph!!.graphInputs) {
            containerDrawing.add(
                portFactory.createPortViewComponent(portFactory.createPortView(portFactory.createSubGraphPort(circuitInput))))
        }
        for (circuitOutput in circuitView.graph!!.graphOutputs) {
            containerDrawing.add(
            portFactory.createPortViewComponent(portFactory.createPortView(portFactory.createSubGraphPort(circuitOutput))))
        }

        return containerDrawing
    }
}