package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.PROP_BEGINNER_HELP_TOOLTIP
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.Modifier
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopItemHeaderPanelSwing
import ch.scorpion.jabbah.graph.view.GraphView
import org.apache.commons.lang3.SystemUtils
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.UIManager
import kotlin.math.max
import kotlin.math.min

/**
 * A [javax.swing] implementation of a [NavigationStackView].
 */
class NavigationStackViewSwing(
	controller: NavigationStackViewController
) : JPanel(), NavigationStackView {

	companion object {

		/** Vertical insets between view border and arrow border.*/
		private const val V_INSETS = 4

		private val OUTER_HEIGHT = max(
			GraphDesktopItemHeaderPanelSwing.PREF_HEIGHT,
			UIManager.getFont("Label.font").size + 2 * V_INSETS)

		/** The fix height of this view.  */
		private val HEIGHT = OUTER_HEIGHT - 2 * V_INSETS

		/** Horizontal insets between view border and arrow border.*/
		private const val H_INSETS = 5

		private const val ELEMENT_DISTANCE = 8

		private const val TEXT_INSET = 10

		private const val SCROLL_WIDTH = 16
		private const val SCROLL_INSET_V = 4
		private const val SCROLL_INSET_H = 4

		private const val SCROLL_STEP = 50

		// Origin is upper-left corner of NavigationStackViewSwing with entire HEIGHT
		private val SCROLL_LEFT_PATH = System.createPath()
			.moveTo(SCROLL_INSET_H, OUTER_HEIGHT / 2)
			.lineTo(SCROLL_WIDTH - SCROLL_INSET_H, OUTER_HEIGHT - SCROLL_INSET_V)
			.lineTo(SCROLL_WIDTH - SCROLL_INSET_H, SCROLL_INSET_V)
			.close()

		private val SCROLL_RIGHT_PATH = System.createPath()
			.moveTo(SCROLL_WIDTH - SCROLL_INSET_H, OUTER_HEIGHT / 2)
			.lineTo(SCROLL_INSET_H, OUTER_HEIGHT - SCROLL_INSET_V)
			.lineTo(SCROLL_INSET_H, SCROLL_INSET_V)
			.close()

		private val elementBackgroundColor: java.awt.Color = UIManager.getColor("Button.background")
		private val elementBorderColor: java.awt.Color = UIManager.getColor("Button.borderColor")
		private val elementHoverBackground: java.awt.Color = UIManager.getColor("Button.toolbar.hoverBackground")
		private val elementHoverBorderColor: java.awt.Color = UIManager.getColor("Button.hoverBorderColor")
		private val elementTextColor: java.awt.Color = UIManager.getColor("Button.foreground")

		private val SCROLL_FOREGROUND = Graphics2DJvm.fromAwtColor(elementTextColor)
		private val SCROLL_HOVER_BACKGROUND = Graphics2DJvm.fromAwtColor(UiUtil.getBackgroundDivertColor(GraphDesktopItemHeaderPanelSwing.headerBackgroundColor))

		private val lockedIcon = ResourceImageJvm.themedImage("/img/locked-16.png")

		private val elementNavigationTooltip: String by lazy {
			var tooltip = Translations.getString("graph.navigationStack.text")
			if (BaseModule.properties.getBoolean(PROP_BEGINNER_HELP_TOOLTIP)) {
				tooltip += Translations.getString("graph.navigationStack.tip", getQuickModifier().label)
			}
			tooltip
		}

		private fun getQuickModifier(): Modifier =
			if (SystemUtils.IS_OS_MAC) {
				Modifier.Meta
			} else {
				Modifier.Alt
			}
	}

	private val tailFont = FontImpl(
		PhysicalFontFamily(font.name),
		font.style,
		font.size)

	private val headFont = tailFont.deriveFont(FontStyle.BOLD)

	private val navigationStack: NavigationStack<GraphView> = controller.navigationStack

	private val elements: MutableList<AbstractElement> = mutableListOf()

	private val hoverListener = HoverListener()

	private val leftScroll = LeftScroll()

	private val rightScroll = RightScroll()

	private var scrollOffset = 0
		set(value) {
			if (field != value) {
				field = value
				updateScrollButtons()
			}
		}

	private var maxElementX = 0

	init {
		controller.view = this

		isEnabled = true
		background = GraphDesktopItemHeaderPanelSwing.headerBackgroundColor
		border = BorderFactory.createEmptyBorder(V_INSETS, 0, V_INSETS, H_INSETS)
		preferredSize = Dimension(0, OUTER_HEIGHT)

		refresh()

		addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?) {
				scrollToHeader()
			}
		})
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
		scrollOffset = 0

		elements.clear()

		// Create new Element objects
		var i = 0
		val iter = navigationStack.iterator()
		while (iter.hasNext()) {
			val content = iter.next()
			elements.add(createElement(content, i == 0, !iter.hasNext()))
			i++
		}

		// Calculate locations of Elements
		var x = 0.0
		maxElementX = 0
		elements.filterIsInstance<Element>().forEach {
			it.location = Point2D(x, V_INSETS.toDouble())
			maxElementX += it.path.boundingBox.width.toInt()
			x += it.path.boundingBox.width - HEIGHT / 2.0 + ELEMENT_DISTANCE
		}

		// Add as last elements so they get painted over the other elements
		elements.add(leftScroll)
		elements.add(rightScroll)

		scrollToHeader()
		repaint()
	}

	override fun paintComponent(g: Graphics?) {
		super.paintComponent(g)
		val gJvm = Graphics2DJvm(g as Graphics2D)
		for (element in elements) {
			element.draw(gJvm)
		}
	}

	override fun getMaximumSize(): Dimension =
		Dimension(Integer.MAX_VALUE, HEIGHT + 2 * V_INSETS)

	/** ---- [NavigationStackViewSwing] */

	private fun getFont(last: Boolean): Font =
		if (last) headFont else tailFont

	private fun createElement(entry: NavigationStackEntry<GraphView>, first: Boolean, last: Boolean): Element {
		val textRenderInfo = TextRenderInfoFactory.measureSingleLineText(entry.name, getFont(last))
		val textLength = textRenderInfo.textBounds.width
		val showLock = first && !editable
		return Element(
			entry = entry,
			path = if (first) createFirstPath(textLength, showLock) else createNonFirstPath(textLength),
			showLock = showLock,
			isFirst = first,
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

	/**
	 * Shows or hides the scroll buttons depending on the available space, the total size
	 * of all [Element]s, and the current [scrollOffset].
	 */
	private fun updateScrollButtons() {
		leftScroll.visible = scrollOffset < 0
		rightScroll.visible = scrollOffset + maxElementX > width
		repaint()
	}

	private fun scrollLeft() {
		scrollOffset = min(scrollOffset + SCROLL_STEP, 0)
	}

	private fun scrollRight() {
		scrollOffset = max(scrollOffset - SCROLL_STEP, width - maxElementX)
	}

	/** Make sure that the header [Element] is visible.*/
	private fun scrollToHeader() {
		scrollOffset = min(width - maxElementX, 0)
	}

	private abstract inner class AbstractElement(visible: Boolean) {

		abstract val canHover: Boolean
		abstract val tooltip: String

		var visible: Boolean = visible
		var isHover: Boolean = false

		abstract fun contains(x: Int, y: Int): Boolean
		abstract fun draw(g: Graphics2DJvm)
		abstract fun mousePressed(e: MouseEvent)
	}

	/** An element of a [NavigationStackViewSwing] representing a single [DrawingViewContent].*/
	private inner class Element(
		val entry: NavigationStackEntry<GraphView>,
		val path: Path,
		val showLock: Boolean,
		val isFirst: Boolean,
		val isHead: Boolean
	) : AbstractElement(visible = true) {

		private val label: Label = Label(
			text = entry.name,
			font = getFont(isHead),
			horizontalAlignment = if (isFirst) HorizontalAlignment.LEFT else HorizontalAlignment.CENTER,
			verticalAlignment = VerticalAlignment.CENTER,
			location = Point2D(
				x = if (isFirst) {
						if (showLock) {
							path.boundingBox.minX + TEXT_INSET + lockedIcon.width
						} else {
							path.boundingBox.minX + TEXT_INSET
						}
					} else {
						path.boundingBox.centerX
					},
				y = path.boundingBox.centerY))

		var location: Point2D = Point2D.ZERO

		override val canHover: Boolean get() = !isHead

		override val tooltip: String get() = elementNavigationTooltip

		override fun draw(g: Graphics2DJvm) {
			g.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			g.translate(location.x + scrollOffset, location.y)

			g.g.color = if (isHead) {
				elementBackgroundColor
			} else {
				if (isHover) elementHoverBackground else elementBackgroundColor
			}
			g.fill(path)

			val borderColor: Color = if (isHead) {
				Graphics2DJvm.fromAwtColor(elementBorderColor)
			} else {
				if (isHover) Graphics2DJvm.fromAwtColor(elementHoverBorderColor) else Graphics2DJvm.fromAwtColor(elementBorderColor)
			}
			g.color = borderColor
			g.draw(path)

			if (showLock) {
				g.drawImage(
					image = lockedIcon,
					x = (path.boundingBox.minX + TEXT_INSET / 2).toInt(),
					y = (path.boundingBox.centerY - lockedIcon.height / 2).toInt())
			}

			label.color = Graphics2DJvm.fromAwtColor(elementTextColor)
			label.font = getFont(isHead)
			label.draw(DrawModule.drawContextFactory(g, null, null))

			g.translate(-(location.x + scrollOffset), -location.y)
		}

		override fun contains(x: Int, y: Int): Boolean =
			path.contains(x - location.x - scrollOffset, y - location.y)

		override fun mousePressed(e: MouseEvent) {
			navigationStack.navigateBackTo(entry, isQuickMode(e))
			hoverListener.resetHover()
		}

		private fun isQuickMode(e: MouseEvent): Boolean =
			if (SystemUtils.IS_OS_MAC) {
				e.isMetaDown
			} else {
				e.isControlDown
			}
	}

	/**
	 * Listens for mouse movements in order to create a hover effect in the [Element] that currently contains the
	 * mouse pointer.
	 */
	private inner class HoverListener : MouseAdapter() {

		private var hoveredElement: AbstractElement? = null

		override fun mouseMoved(e: MouseEvent) {
			var newHoveredElement: AbstractElement? = null

			// Reverse so that scroll elements take precedence
			val element = elements.reversed().firstOrNull { it.contains(e.x, e.y) }
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
				if (!hoveredElement!!.canHover) {
					hoveredElement = null
				} else {
					hoveredElement!!.isHover = true
				}
			}

			if (changed) {
				if (hoveredElement == null) {
					toolTipText = null
				} else if (hoveredElement!!.canHover) {
					toolTipText = hoveredElement!!.tooltip
				}
				this@NavigationStackViewSwing.repaint()
			}
		}

		override fun mouseExited(e: MouseEvent) {
			resetHover()
		}

		override fun mousePressed(e: MouseEvent) {
			if (e.button == MouseEvent.BUTTON1 && hoveredElement != null) {
				hoveredElement!!.mousePressed(e)
			}
		}

		fun resetHover() {
			if (hoveredElement != null) {
				hoveredElement!!.isHover = false
				hoveredElement = null
				toolTipText = null
				this@NavigationStackViewSwing.repaint()
			}
		}
	}

	private inner class LeftScroll : AbstractElement(visible = false) {

		override val canHover: Boolean get() = true

		override val tooltip: String by lazy { Translations.getString("graph.navigationStack.scrollLeft") }

		override fun contains(x: Int, y: Int): Boolean =
			x in 0..SCROLL_WIDTH && y in 0..OUTER_HEIGHT

		override fun draw(g: Graphics2DJvm) {
			if (visible) {
				g.color = if (isHover) {
					SCROLL_HOVER_BACKGROUND
				} else {
					Graphics2DJvm.fromAwtColor(this@NavigationStackViewSwing.parent.background)
				}
				g.fillRect(0, 0, SCROLL_WIDTH, OUTER_HEIGHT)

				g.color = SCROLL_FOREGROUND
				g.fill(SCROLL_LEFT_PATH)
			}
		}

		override fun mousePressed(e: MouseEvent) {
			scrollLeft()
		}
	}

	private inner class RightScroll : AbstractElement(visible = false) {

		private val minX: Int get() = this@NavigationStackViewSwing.width - SCROLL_WIDTH

		override val canHover: Boolean get() = true

		override val tooltip: String by lazy { Translations.getString("graph.navigationStack.scrollRight") }

		override fun contains(x: Int, y: Int): Boolean {
			return x in minX..minX + SCROLL_WIDTH && y in 0..OUTER_HEIGHT
		}

		override fun draw(g: Graphics2DJvm) {
			if (visible) {
				g.translate(minX.toDouble(), 0.0)

				g.color = if (isHover) {
					SCROLL_HOVER_BACKGROUND
				} else {
					Graphics2DJvm.fromAwtColor(this@NavigationStackViewSwing.parent.background)
				}
				g.fillRect(0, 0, SCROLL_WIDTH, OUTER_HEIGHT)

				g.color = SCROLL_FOREGROUND
				g.fill(SCROLL_RIGHT_PATH)

				g.translate(-minX.toDouble(), 0.0)
			}
		}

		override fun mousePressed(e: MouseEvent) {
			scrollRight()
		}
	}
}