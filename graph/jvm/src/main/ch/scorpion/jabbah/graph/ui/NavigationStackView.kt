package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.GraphNameChangedEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.ui.NavigationStack
import ch.scorpion.jabbah.graph.view.ui.NavigationStackEvent
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.draw.module.DrawModule
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JComponent


/**
 * A breadcrumb-like view of a [NavigationStack].
 */
class NavigationStackView(
    val navigationStack: NavigationStack,
    eventBus: EventBus
) : JPanel() {

    constructor(navigationStack: NavigationStack): this(navigationStack, BaseModule.eventBus)
    constructor(): this(NavigationStack(BaseModule.eventBus))

    companion object {

        val PROP_FONT = "graph.ui.NavigationStackView.font"

        val PROP_HEAD_FONT = "graph.ui.NavigationStackView.headFont"

        val PROP_BACKGROUND_COLOR = "graph.ui.NavigationStackView.backgroundColor"

        val PROP_HOVER_BACKGROUND_COLOR = "graph.ui.NavigationStackView.hoverBackgroundColor"

        val PROP_HEAD_BACKGROUND_COLOR = "graph.ui.NavigationStackView.headBackgroundColor"

        val PROP_BORDER_COLOR = "graph.ui.NavigationStackView.borderColor"

        val PROP_HOVER_BORDER_COLOR = "graph.ui.NavigationStackView.hoverBorderColor"

        val PROP_HEAD_BORDER_COLOR = "graph.ui.NavigationStackView.headBorderColor"

        val PROP_TEXT_COLOR = "graph.ui.NavigationStackView.textColor"

        val PROP_HOVER_TEXT_COLOR = "graph.ui.NavigationStackView.hoverTextColor"

        val PROP_HEAD_TEXT_COLOR = "graph.ui.NavigationStackView.headTextColor"

        /** The fix height of this view.  */
        private val HEIGHT = 26

        private val INSETS = 5

        private val ELEMENT_DISTANCE = 8

        private val TEXT_INSET = 15
    }

    private val elements: MutableList<Element> = mutableListOf()

    private val hoverListener = HoverListener()

    init {
        isEnabled = true

        eventBus.register(NavigationStackEvent::class, {
            if (it.navigationStack == navigationStack) {
                update()
            }
        })

        eventBus.register(GraphNameChangedEvent::class, {
            if (navigationStack.rootGraphView != null && navigationStack.rootGraphView!!.graph == it.graph) {
                update()
            }
        })

        border = BorderFactory.createEmptyBorder(INSETS, INSETS, INSETS, INSETS)
        update()
    }

    /** ---- [JComponent] */

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            addMouseListener(hoverListener)
            addMouseMotionListener(hoverListener)
        } else {
            removeMouseListener(hoverListener)
            removeMouseMotionListener(hoverListener)
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val gJvm = Graphics2DJvm(g as Graphics2D)
        for (element in elements) {
            element.draw(gJvm)
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(300, HEIGHT + 2 * INSETS)
    }

    /** ---- [NavigationStackView] */

    /** Executes the specified handler for each [GraphView] of this [NavigationStack].*/
    fun forEach(action: (GraphView<GraphElementView<*>>) -> Unit) {
        elements.forEach { action.invoke(it.graphView) }
    }

    private fun update() {
        elements.clear()

		// Create new Element object
		var i = 0
		val iter = navigationStack.iterator()
		while (iter.hasNext()) {
			val graphView = iter.next()
			elements.add(createElement(graphView, i == 0, !iter.hasNext()))
			i++
		}

		// Calculate locations of Elements
		var x = 0.0
		for (element in elements) {
			element.location = Point2D(x, INSETS.toDouble())
			x += element.path.boundingBox.width - HEIGHT / 2.0 + ELEMENT_DISTANCE
		}

		repaint()
    }

    private fun createElement(graphView: GraphView<GraphElementView<*>>, first: Boolean, last: Boolean): Element {
        val textRenderInfo = DrawModule.textRenderInfoFactory.invoke(graphView.graph!!.name, DrawModule.properties.getFont(PROP_FONT))
        val textLength = textRenderInfo.textBounds.width

        return Element(graphView, if (first) createFirstPath(textLength) else createNonFirstPath(textLength), last)
    }

    private fun createFirstPath(textLength: Double): Path {
        val baseLength = 2.0 * TEXT_INSET + textLength
        val path = System.get().createPath()
        path.moveTo(0, 0)
        path.lineTo(baseLength, 0.0)
        path.lineTo(baseLength + HEIGHT / 2.0, HEIGHT / 2.0)
        path.lineTo(baseLength, HEIGHT.toDouble())
        path.lineTo(0.0, HEIGHT.toDouble())
        path.close()

        return path
    }

    private fun createNonFirstPath(textLength: Double): Path {
        val baseLength = 2 * TEXT_INSET + textLength
        val path = System.get().createPath()
        path.moveTo(0, 0)
        path.lineTo(HEIGHT / 2.0 + baseLength, 0.0)
        path.lineTo(HEIGHT + baseLength, HEIGHT / 2.0)
        path.lineTo(HEIGHT / 2.0 + baseLength, HEIGHT.toDouble())
        path.lineTo(0, HEIGHT)
        path.lineTo(HEIGHT / 2.0, HEIGHT / 2.0)
        path.close()
        return path
    }

    /** An element of a [NavigationStackView] representing a single [GraphView].*/
    private inner class Element(
        val graphView: GraphView<GraphElementView<*>>,
        val path: Path,
        val isHead: Boolean
    ) {

        private val label: Label = Label(
                text = graphView.graph!!.name,
                font = DrawModule.properties.getFont(PROP_FONT),
                color = DrawModule.properties.getColor(PROP_TEXT_COLOR),
                horizontalAlignment = Label.HorizontalAlignment.CENTER,
                verticalAlignment = Label.VerticalAlignment.CENTER,
                location = Point2D(path.boundingBox.centerX, path.boundingBox.centerY))

        var location: Point2D = Point2D()

        var isHover: Boolean = false

        fun draw(g: Graphics2DJvm) {
            g.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.translate(location.x, location.y)
            if (isHead) {
                g.color = DrawModule.properties.getColor(PROP_HEAD_BACKGROUND_COLOR)
            } else {
                g.color = if (isHover) DrawModule.properties.getColor(PROP_HOVER_BORDER_COLOR) else DrawModule.properties.getColor(PROP_BACKGROUND_COLOR)
            }
            g.fill(path)

            val borderColor: Color?
            if (isHead) {
                borderColor = DrawModule.properties.getColor(PROP_HEAD_BORDER_COLOR)
            } else {
                borderColor = if (isHover) DrawModule.properties.getColor(PROP_HOVER_BORDER_COLOR) else DrawModule.properties.getOptionalColor(PROP_BORDER_COLOR)
            }
            if (borderColor != null) {
                g.color = borderColor
                g.draw(path)
            }

            if (isHead) {
                label.font = DrawModule.properties.getFont(PROP_HEAD_FONT)
                label.color = DrawModule.properties.getColor(PROP_HEAD_TEXT_COLOR)
            } else {
                label.font = DrawModule.properties.getFont(PROP_FONT)
                label.color = if (isHover) DrawModule.properties.getColor(PROP_HOVER_TEXT_COLOR) else DrawModule.properties.getColor(PROP_TEXT_COLOR)
            }
            label.draw(DrawContext(g))
            g.translate(-location.x, -location.y)
        }

        fun contains(x: Int, y: Int): Boolean {
            return path.contains(x - location.x, y - location.y)
        }
    }

    /**
     * Listens for mouse movements in order to create a hover effect in the [Element] that currently contains the
     * mouse pointer.
     */
    private inner class HoverListener : MouseAdapter() {

        private var hoveredElement: Element? = null

        override fun mouseMoved(e: MouseEvent) {
            var newHoveredElement: Element? = null
            val element = elements.firstOrNull { it.contains(e.x, e.y) }
            if (element != null) {
                newHoveredElement = element
            }

            val changed: Boolean
            if (newHoveredElement != null) {
                changed = newHoveredElement != hoveredElement
            } else {
                changed = hoveredElement != null
            }

            if (hoveredElement != null) {
                hoveredElement!!.isHover = false
            }

            hoveredElement = newHoveredElement
            if (hoveredElement != null) {
                if (hoveredElement!!.isHead) {
                    hoveredElement = null
                } else {
                    hoveredElement!!.isHover = true
                }
            }

            if (changed) {
                this@NavigationStackView.repaint()
            }
        }

        override fun mouseExited(e: MouseEvent) {
            resetHover()
        }

        override fun mousePressed(e: MouseEvent) {
            if (hoveredElement != null) {
                navigationStack.navigateBackTo(hoveredElement!!.graphView)
            }
        }

        private fun resetHover() {
            if (hoveredElement != null) {
                hoveredElement!!.isHover = false
                hoveredElement = null
                this@NavigationStackView.repaint()
            }
        }
    }
}