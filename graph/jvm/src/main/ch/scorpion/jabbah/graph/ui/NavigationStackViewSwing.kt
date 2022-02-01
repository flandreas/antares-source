package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.graphics.ResourceImageJvm
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.UIManager


/**
 * A [javax.swing] implementation of a [NavigationStackView].
 */
class NavigationStackViewSwing(
	controller: NavigationStackViewController
) : JPanel(), NavigationStackView {

	companion object {

		const val PROP_FONT = "graph.ui.NavigationStackView.font"

		const val PROP_HEAD_FONT = "graph.ui.NavigationStackView.headFont"

		/** Vertical insets between view border and arrow border.*/
		private const val V_INSETS = 4

		/** The fix height of this view.  */
		private const val HEIGHT = GraphDesktopItemHeaderPanel.PREF_HEIGHT - 2 * V_INSETS

		/** Horizontal insets between view border and arrow border.*/
		private const val H_INSETS = 5

		private const val ELEMENT_DISTANCE = 8

		private const val TEXT_INSET = 10

		private val elementBackgroundColor: java.awt.Color get() = UIManager.getColor("Button.background")
		private val elementBorderColor: java.awt.Color get() = UIManager.getColor("Button.borderColor")
		private val elementHoverBackground: java.awt.Color get() = UIManager.getColor("Button.toolbar.hoverBackground")
		private val elementHoverBorderColor: java.awt.Color get() = UIManager.getColor("Button.hoverBorderColor")
		private val elementTextColor: java.awt.Color get() = UIManager.getColor("Button.foreground")

		private val lockedIcon = ResourceImageJvm.themedImage("/img/locked-16.png")
	}

	private val navigationStack: NavigationStack<GraphView> = controller.navigationStack

	private val elements: MutableList<Element> = mutableListOf()

	private val hoverListener = HoverListener()

	init {
		controller.view = this
		isEnabled = true
		background = GraphDesktopItemHeaderPanel.headerBackgroundColor
		border = BorderFactory.createEmptyBorder(V_INSETS, 0, V_INSETS, H_INSETS)
		refresh()
	}

	override fun dispose() {
		// empty
	}

	/** ---- [NavigationStackView] */

	override var editable: Boolean = true

	override var active: Boolean
		get() = isEnabled
		set(value) {
			super.setEnabled(value)
			if (value) {
				addMouseListener(hoverListener)
				addMouseMotionListener(hoverListener)
			} else {
				removeMouseListener(hoverListener)
				removeMouseMotionListener(hoverListener)
			}
		}

	override fun refresh() {
		elements.clear()

		// Create new Element object
		var i = 0
		val iter = navigationStack.iterator()
		while (iter.hasNext()) {
			val content = iter.next()
			elements.add(createElement(content, i == 0, !iter.hasNext()))
			i++
		}

		// Calculate locations of Elements
		var x = 0.0
		for (element in elements) {
			element.location = Point2D(x, V_INSETS.toDouble())
			x += element.path.boundingBox.width - HEIGHT / 2.0 + ELEMENT_DISTANCE
		}

		repaint()
	}

	override fun paintComponent(g: Graphics?) {
		super.paintComponent(g)
		val gJvm = Graphics2DJvm(g as Graphics2D)
		for (element in elements) {
			element.draw(gJvm)
		}
	}

	override fun getPreferredSize(): Dimension {
		return Dimension(300, HEIGHT + 2 * V_INSETS)
	}

	/** ---- [NavigationStackViewSwing] */

	private fun createElement(entry: NavigationStackEntry<GraphView>, first: Boolean, last: Boolean): Element {
		val textRenderInfo = TextRenderInfoFactory.measureSingleLineText(
			entry.graphName!!.value, DrawModule.properties.getFont(PROP_FONT))
		val textLength = textRenderInfo.textBounds.width
		val showLock = first && !editable
		return Element(
			entry = entry,
			path = if (first) createFirstPath(textLength, showLock) else createNonFirstPath(textLength),
			showLock = showLock,
			isHead = last)
	}

	private fun createFirstPath(textLength: Double, showLock: Boolean): Path {
		val baseLength = if (showLock) {
			3.0 * TEXT_INSET + lockedIcon.width + textLength
		} else {
			2.0 * TEXT_INSET + textLength
		}
		val path = System.createPath()
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
		val path = System.createPath()
		path.moveTo(0, 0)
		path.lineTo(HEIGHT / 2.0 + baseLength, 0.0)
		path.lineTo(HEIGHT + baseLength, HEIGHT / 2.0)
		path.lineTo(HEIGHT / 2.0 + baseLength, HEIGHT.toDouble())
		path.lineTo(0, HEIGHT)
		path.lineTo(HEIGHT / 2.0, HEIGHT / 2.0)
		path.close()
		return path
	}

	/** An element of a [NavigationStackViewSwing] representing a single [DrawingViewContent].*/
	private inner class Element(
		val entry: NavigationStackEntry<GraphView>,
		val path: Path,
		val showLock: Boolean,
		val isHead: Boolean
	) {

		private val label: Label = Label(
			text = entry.content.drawing.graph!!.name.value,
			font = DrawModule.properties.getFont(PROP_FONT),
			horizontalAlignment = if (showLock) HorizontalAlignment.LEFT else HorizontalAlignment.CENTER,
			verticalAlignment = VerticalAlignment.CENTER,
			location = Point2D(
				x = if (showLock) path.boundingBox.minX + TEXT_INSET + lockedIcon.width else path.boundingBox.centerX,
				y = path.boundingBox.centerY))

		var location: Point2D = Point2D.ZERO

		var isHover: Boolean = false

		fun draw(g: Graphics2DJvm) {
			g.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			g.translate(location.x, location.y)

			g.g.color = if (isHead) {
				elementBackgroundColor
			} else {
				if (isHover) elementHoverBackground else elementBackgroundColor
			}
			g.fill(path)

			val borderColor: Color? = if (isHead) {
				Graphics2DJvm.fromAwtColor(elementBorderColor)
			} else {
				if (isHover) Graphics2DJvm.fromAwtColor(elementHoverBorderColor) else Graphics2DJvm.fromAwtColor(elementBorderColor)
			}
			if (borderColor != null) {
				g.color = borderColor
				g.draw(path)
			}

			if (showLock) {
				g.drawImage(
					image = lockedIcon,
					x = (path.boundingBox.minX + TEXT_INSET / 2).toInt(),
					y = (path.boundingBox.centerY - lockedIcon.height / 2).toInt())
			}

			label.color = Graphics2DJvm.fromAwtColor(elementTextColor)
			label.font = if (isHead) {
				DrawModule.properties.getFont(PROP_HEAD_FONT)
			} else {
				DrawModule.properties.getFont(PROP_FONT)
			}
			label.draw(DrawModule.drawContextFactory(g, null))

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

			val changed = if (newHoveredElement != null) {
				newHoveredElement != hoveredElement
			} else {
				hoveredElement != null
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
				this@NavigationStackViewSwing.repaint()
			}
		}

		override fun mouseExited(e: MouseEvent) {
			resetHover()
		}

		override fun mousePressed(e: MouseEvent) {
			if (e.button == MouseEvent.BUTTON1 && hoveredElement != null) {
				navigationStack.navigateBackTo(hoveredElement!!.entry, e.isMetaDown)
				hoveredElement = null
			}
		}

		private fun resetHover() {
			if (hoveredElement != null) {
				hoveredElement!!.isHover = false
				hoveredElement = null
				this@NavigationStackViewSwing.repaint()
			}
		}
	}
}