package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.CutCommand
import ch.scorpion.jabbah.edit.editor.PasteCommand
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.loggerFor
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * An utility class that provides methods for copying a collection of [Components]s to the system clipboard,
 * and pasting them back into the current [Drawing].
 */
object CopyPasteUtility {

    private val LOG by loggerFor(this)

    private val DEFAULT_DISTANCE_FACTOR = 3

    /** Remembers the first copies [Component] in order to repeat dislocations for consecutive pasts. */
    private var origAnchorComponent: Component? = null

    /** The pasted [Component] that corresponds with [origAnchorComponent]. */
    private var pastedAnchorComponent: Component? = null

    /** Tracks the number of consecutive pasts without an intermediate copy. Used to produce equal dislocations. */
    private var pasteCount: Int = 0

    /** Cuts the specified [Component]s from the [Drawing] of the specified [DrawingView].*/
    fun cut(
        view: DrawingView<Drawing<Component>>,
        components: Collection<Component>,
        typeMap: TypeMap,
        cmdManager: CommandManager
    ) {
        copy(view.drawing as GraphView, components, typeMap)
        cmdManager.beginTransaction(CutCommand(view, components.toList()))
        cmdManager.commitTransaction()
    }

    fun copy(
        graphView: GraphView<*>,
        components: Collection<Component>,
        typeMap: TypeMap
    ) {
        ByteArrayOutputStream().use {
            try {
                val xmlWriter = ElectricXmlWriter(it)
                val writer = StoreXmlWriter(
                    xmlWriter,
                    typeMap,
                    GlobalIdentityCreator(),
                    { !(it is GraphElementView<*>) || components.contains(it) }
                )
                val graphStorable = GraphStorable(graphView)
                writer.writeStorable(graphStorable)

                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection(String(it.toByteArray())), null)

                origAnchorComponent = components.iterator().next()
                pastedAnchorComponent = null
                pasteCount = 1
            } catch(e: Exception) {
                LOG.error("Error while copying Components to clipboard: ${e.message}")
                throw RuntimeException(e)
            }
        }
    }

    fun paste(
        view: DrawingView<Drawing<Component>>,
        storableCreator: StorableCreator,
        typeMap: TypeMap,
        cmdManager: CommandManager
    ) {
        // Read the contents from the clipboard
        val transferable = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
        if (transferable == null) {
            Toolkit.getDefaultToolkit().beep()
            return
        }

        ByteArrayInputStream((transferable.getTransferData(DataFlavor.stringFlavor) as String).toByteArray()).use {
            try {
                val xmlReader = ElectricXmlReader(it)
                val reader = StoreXmlReader(xmlReader, typeMap, storableCreator)
                val storable = reader.readStorable()
                val dislocation: Point2D = if (pastedAnchorComponent != null) {
                    pasteCount++
                    pastedAnchorComponent!!.location.subtract(origAnchorComponent!!.location).multiply(pasteCount.toDouble())
                } else {
                    Point2D(
                            DEFAULT_DISTANCE_FACTOR * view.grid.distance,
                            DEFAULT_DISTANCE_FACTOR * view.grid.distance)
                }

                if (storable is GraphStorable) {
                    val copy =  storable
                    val components = mutableListOf<Component>()
                    for (c in copy.graphView!!.backToFrontIterator()) {
                        if (c is VerticeView) {
                            strip(c.vertice, copy.graphView!!)
                        }
                        if (pastedAnchorComponent == null && origAnchorComponent!!.location == c.location) {
                            pastedAnchorComponent = c
                        }
                        if (!(c is EdgeView<*>)) {
                            c.moveBy(dislocation.x, dislocation.y)
                        }
                        components.add(c)
                    }
                    cmdManager.beginTransaction(PasteCommand(view, components))
                    cmdManager.commitTransaction()
                }
            } catch(e: Exception) {
                LOG.error("Error while reading Components from clipboard: ${e.message}")
                throw RuntimeException(e)
            }
        }
    }

    /**
     * Disconnects all [Port]s of a [Vertice] from [Net]s that don't have a
     * corresponding [EdgeView] in the specified [GraphView].
     */
    private fun strip(vertice: Vertice, graphView: GraphView<*>) {
        for (port in vertice.getPorts()) {
            if (port.net != null) {
                val edgeViews = graphView.getElementViews(port.net!!)
                if (edgeViews.isEmpty()) {
                    port.disconnect()
                }
            }
        }
    }
}