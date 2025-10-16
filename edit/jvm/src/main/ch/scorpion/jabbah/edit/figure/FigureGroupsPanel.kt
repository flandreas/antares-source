package ch.scorpion.jabbah.edit.figure

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.swing.FixedGridLayout
import ch.scorpion.jabbah.base.swing.taskpane.JabbahTaskPaneContainer
import ch.scorpion.jabbah.draw.drawable.DefaultDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentTransferable
import org.jdesktop.swingx.JXTaskPane
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.datatransfer.Transferable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.TransferHandler
import javax.swing.UIManager
import kotlin.math.min

/**
 * Displays all registered [ShapeGroups][FigureGroup] as expandable containers that
 * contain a draggable prototype [Figure] for every registered [Figure].
 */
class FigureGroupsPanel : JabbahTaskPaneContainer() {

	init {
		buildUI()
	}

	private fun buildUI() {
		FigureRegistry.groups.forEach {
			val view = FigureGroupView(it)
			val pane = JXTaskPane(it.name)
			pane.isCollapsed = true
			pane.add(view)
			add(pane)
		}
	}
}

/**
 * Displays all [Figures][Figure] of a single [FigureGroup] in a [FixedGridLayout].
 */
private class FigureGroupView(private val figureGroup: FigureGroup) : JPanel() {

	companion object {
		val BACKGROUND_COLOR = UIManager.getColor("Table.background")!!
	}

	init {
		buildUI()
		fillContent()
	}

	private fun buildUI() {
		background = BACKGROUND_COLOR
		layout = FixedGridLayout(cellSize = 70, cellGap = 1)
	}

	private fun fillContent() {
		for (provider in figureGroup.providers) {
			add(FigurePanel(provider))
		}
	}
}

/**
 * Displays a single [Figure] and allows the user to drag it into a destination [JComponent].
 */
private class FigurePanel(
	val figureProvider: FigureProvider
) : JPanel() {

	companion object {
		private const val INSET = 10
		private var drawableDrawer: DrawableDrawer<Component> = DefaultDrawableDrawer()
	}

	private val figure = figureProvider.factory.create()

	private var scale: Double = 1.0

	init {
		background = FigureGroupView.BACKGROUND_COLOR
		toolTipText = figureProvider.name
		transferHandler = FigureTransferHandler
		addMouseListener(DnDMouseAdapter())
	}

	private fun updateLayout() {
		figure.location = Point2D.ZERO
		val bbox = figure.boundingBox

		val fx = (this.width - 2 * INSET) / bbox.width
		val fy = (this.height - 2 * INSET) / bbox.height

		scale = 1.0
		if (fx < 1 || fy < 1) {
			scale = min(fx, fy)
		}

		figure.moveBy(
			this.width / 2 / scale - bbox.centerX,
			this.height / 2 / scale - bbox.centerY
		)
	}

	override fun paintComponent(g: Graphics?) {
		val g2 = g as Graphics2D
		super.paintComponent(g)

		updateLayout()

		g2.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON)

		if (scale != 1.0) {
			g2.scale(scale, scale)
		}

		drawableDrawer.process(DrawModule.drawContextFactory(Graphics2DJvm(g2), null, null), figure)

		if (scale != 1.0) {
			g2.scale(1 / scale, 1 / scale)
		}
	}

	/** Initiates drag&drop when the mouse is clicked within this [FigurePanel]. */
	private class DnDMouseAdapter : MouseAdapter() {
		override fun mousePressed(e: MouseEvent?) {
			if (e?.component is FigurePanel) {
				(e.component as FigurePanel).transferHandler.exportAsDrag(e.component as FigurePanel, e, TransferHandler.COPY)
			}
		}
	}
}

/**
 * Supports dragging a [Figure] from a [FigurePanel] into another [JComponent].
 */
private object FigureTransferHandler : TransferHandler() {

	private val dummyImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

	override fun getSourceActions(c: JComponent): Int = COPY

	override fun canImport(support: TransferSupport?): Boolean = false

	override fun createTransferable(c: JComponent?): Transferable? {
		if (c is FigurePanel) {
			dragImage = dummyImage
			dragImageOffset = Point(0, 0)
			return ComponentTransferable(c.figureProvider.factory.create())
		}
		return null
	}
}