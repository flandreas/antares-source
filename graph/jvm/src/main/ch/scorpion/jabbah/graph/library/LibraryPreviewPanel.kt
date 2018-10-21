package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.DefaultDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.VerticeView
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.BorderFactory
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.JTextPane


/**
 * A [JPanel] that provides a preview of the [LibraryElement] that is currently selected in a [LibraryTreeView].
 */
class LibraryPreviewPanel(
    eventBus: EventBus,
    private val libraryTreeView: LibraryTreeView
) : JPanel() {

    companion object {
	    private val LOG by logger(LibraryPreviewPanel::class)
        private val BACKGROUND_COLOR = Color.WHITE
    }

    init {
        eventBus.register(CurrentSavableEvent::class) { updateWithSelectedItem() }
    }

    @Suppress("unused")
    constructor(libraryTreeView: LibraryTreeView): this(BaseModule.eventBus, libraryTreeView)

    /** Maps a [LibraryElement] to the instantiated [Component] to be displayed as preview.*/
    private val map: MutableMap<LibraryElement, Component> = mutableMapOf()

    private val componentDisplay = ComponentDisplay()

    private val descriptionArea = JTextPane()

    /** Stores the preview [Component] of the currently selected [LibraryElement]. */
    private var selection: Component? = null

    init {
        descriptionArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        descriptionArea.contentType = "text/html"
        descriptionArea.isEditable = false
        descriptionArea.preferredSize = Dimension(150, 50)
        descriptionArea.background = BACKGROUND_COLOR

        eventBus.register(LibrarySelectionChangedEvent::class) { handleLibrarySelectionChanged(it) }
	    eventBus.register(LibraryItemUpdatedEvent::class) { map.remove(it.item) }

	    buildUI()

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                componentDisplay.updateLayout()
            }
        })
    }

    fun addDrawableDrawer(drawableDrawer: DrawableDrawer<Component>) {
        componentDisplay.addDrawableDrawer(drawableDrawer)
    }

    private fun buildUI() {
        background = BACKGROUND_COLOR
        layout = BorderLayout()
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10))
        descriptionArea.border = BorderFactory.createEmptyBorder(0, 10, 0, 0)
        add(componentDisplay, BorderLayout.WEST)
        add(descriptionArea, BorderLayout.CENTER)
    }

    private fun handleLibrarySelectionChanged(e: LibrarySelectionChangedEvent) {
        if (e.libraryTreeView !== this.libraryTreeView) {
            return
        }
		updateWithSelectedItem()
    }

	private fun updateWithSelectedItem() {
		val selectedItem = libraryTreeView.getSelectedItem()
		if (selectedItem !is LibraryElement) {
			updateSelection(null)
		} else {
			try {
				updateSelection(selectedItem)
			} catch (e: Exception) {
				LOG.error("Exception when handling LibraryImpl selection change: ${e.message}")
			}
		}
		repaint()
	}

    private fun updateSelection(libraryElement: LibraryElement?) {
        if (libraryElement == null) {
			selection = null
			descriptionArea.text = null
			return
		}

		val component = map[libraryElement]
		if (component != null) {
			updateSelectionImpl(component)
			return
		}

        InvocationHandler.invoke(Runnable {
            val c = libraryElement.getNewInstance<GraphElement>()
	        map[libraryElement] = c
            updateSelectionImpl(c)
        })
    }

    private fun updateSelectionImpl(component: Component) {
        selection = component
        selection!!.styleProvider = Themes.uiStyleProvider
        componentDisplay.updateLayout()
        descriptionArea.text = System.get().buildToolTipText(selection!!.type, (selection as VerticeView<*>).shortDescription)
    }

    /** Displays the graphical preview of the selected {@link Component}. */
    private inner class ComponentDisplay : JPanel() {

        private var drawableDrawer: DrawableDrawer<Component> = DefaultDrawableDrawer()
        private var scale: Double = 1.0

        init {
            background = BACKGROUND_COLOR
            preferredSize = Dimension(75, 150)
        }

        fun addDrawableDrawer(drawableDrawer: DrawableDrawer<Component>) {
            drawableDrawer.successor = this.drawableDrawer
            this.drawableDrawer = drawableDrawer
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g as Graphics2D
            super.paintComponent(g)

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON)

            if (scale != 1.0) {
                g2.scale(scale, scale)
            }

            if (selection != null) {
                drawableDrawer.process(DrawContext(Graphics2DJvm(g2), GraphApplicationContext()), selection!!)
            }

            if (scale != 1.0) {
                g2.scale(1 / scale, 1 / scale)
            }
        }

        fun updateLayout() {
            if (selection == null) {
                return
            }

            selection!!.location = Point2D(0, 0)
            val bbox = selection!!.boundingBox

            val fx = this.width / bbox.width
            val fy = this.height / bbox.height

            scale = 1.0
            if (fx < 1 || fy < 1) {
                scale = Math.min(fx, fy)
            }

            // Horizontally centered
            val dx = (this.width.toDouble() - bbox.width * scale) / 2 - bbox.x
            // Vertically top aligned
            val dy = -bbox.y

            selection!!.moveBy(dx, dy)
            repaint()
        }
    }
}