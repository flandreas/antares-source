package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.PasteCommand
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.edit.app.CopyPasteUtility
import ch.scorpion.jabbah.edit.app.DeleteAction
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.PortView
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * An utility class that provides methods for copying a collection of [Component]s to the system clipboard,
 * and pasting them back into the current [Drawing].
 */
object CopyPasteUtilitySwing : CopyPasteUtility {

    private val LOG by logger(CopyPasteUtilitySwing::class)

    private const val DEFAULT_DISTANCE_FACTOR = 3

    /** Remembers the first copies [Component] in order to repeat dislocations for consecutive pasts. */
    private var origAnchorComponent: Component? = null

    /** The pasted [Component] that corresponds with [origAnchorComponent]. */
    private var pastedAnchorComponent: Component? = null

    /** Tracks the number of consecutive pasts without an intermediate copy. Used to produce equal dislocations. */
    private var pasteCount: Int = 0

    /** Cuts the specified [Component]s from the [Drawing] of the specified [DrawingView].*/
    override fun cut(
        view: DrawingView<Drawing<Component>>,
        components: Collection<Component>,
        typeMap: TypeMap,
        commandManager: CommandManager
    ) {
	    val componentsToDelete = DeleteAction.getComponentsToDelete(components)
	    if (componentsToDelete.isNotEmpty()) {
		    copy(view.drawing as GraphView, componentsToDelete, typeMap)
		    //commandManager.execute(CutCommand(view, componentsToDelete.toList()))
		    GraphViewModule.graphViewService.delete(componentsToDelete, view, "edit.command.cut")
	    }

	    // Don't do 'components.size != selection.size for checking whether everything has been deleted,
	    // because non-deletable (by user selection!) Components might have been deleted as a side effect
	    // of deleting other Components.
	    if (components.any { view.drawing.contains(it) }) {
		    BaseModule.eventBus.post(ComponentMessage(
			    ComponentMessageType.Info,
			    null,
			    "edit.action.undeletable.msg"
		    ))
	    }
    }

    /**
     * Copies the selected [Component]s to the system clipboard as a [String] with the [Storable] XML
     * representation.
     *
     * Note that this implementation restricts the copied [GraphElementView]s to the selected one, but
     * copies much more that needed from the model layer; in fact, it copies the entire model contents.
     * Stripping is done when pasting the clipboard contents again.
     */
    override fun copy(
	    drawing: Drawing<*>,
        components: Collection<Component>,
        typeMap: TypeMap
    ) {
        ByteArrayOutputStream().use {
            try {
                val xmlWriter = ElectricXmlWriter(it)
                val writer = StoreXmlWriter(
                    xmlWriter,
                    typeMap,
                    GlobalIdentityCreator()
                ) { c -> c !is GraphElementView<*> || components.contains(c) }
	            val graphStorable = GraphStorable(drawing as GraphView)
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

    override fun paste(
        view: DrawingView<Drawing<Component>>,
        storableCreator: StorableCreator,
        typeMap: TypeMap,
        commandManager: CommandManager
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
                val copy = reader.readStorable()
                val dislocation: Point2D = if (pastedAnchorComponent != null) {
                    pasteCount++
                    pastedAnchorComponent!!.location.subtract(origAnchorComponent!!.location).multiply(pasteCount.toDouble())
                } else {
	                Point2D(
		                DEFAULT_DISTANCE_FACTOR * view.grid.distance,
		                DEFAULT_DISTANCE_FACTOR * view.grid.distance)
                }

                if (copy is GraphStorable) {
                    val components = mutableListOf<Component>()
                    for (cv in copy.graphView.backToFrontIterator()) {
                        if (cv is VerticeView<*>) {
                            strip(cv, copy.graphView)
                        }
                        if (pastedAnchorComponent == null && origAnchorComponent!!.location == cv.location) {
                            pastedAnchorComponent = cv
                        }
                        components.add(cv)
                    }
	                Locatable.moveLocatables(components, dislocation)
	                commandManager.execute(PasteCommand(view, components))
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
    private fun strip(verticeView: VerticeView<*>, graphView: GraphView) {
        for (pv in verticeView.getPortViews()) {
            if (pv.port.net != null) {
                val edgeViews = graphView.getElementViews(pv.port.net!!)
                if (edgeViews.isEmpty()) {
                    pv.port.disconnect()
                    (pv as PortView<Any>).handleUnconnect(null)
                }
            }
        }
    }
}