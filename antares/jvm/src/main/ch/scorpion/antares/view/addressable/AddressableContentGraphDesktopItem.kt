package ch.scorpion.antares.view.addressable

import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import javax.swing.JLabel

/** Wraps a [AddressableContentsPanel] as a [GraphDesktopViewItem] so it can be added to the [GraphDesktopView]. */
class AddressableContentGraphDesktopItem(
	drawingView: DrawingView<GraphView>,
	addressableId: Int,
	title: String,
	applicationContextHolder: GraphApplicationContextHolder,
	cmdManager: CommandManager = EditModule.commandManager,
	contextColor: CompositeColor
) : AbstractGraphDesktopItemPanel() {

	private val memoryContentPanel = AddressableContentsPanel(drawingView, applicationContextHolder, addressableId, cmdManager)

	private val headerPanel = GraphDesktopItemHeaderPanel(this, JLabel(title), allowClose = true)

	init {
		buildUI(contextColor)
	}

	private fun buildUI(contextColor: CompositeColor) {
		layout = BorderLayout()
		add(headerPanel, BorderLayout.NORTH)
		add(memoryContentPanel, BorderLayout.CENTER)
		super.contextColor = contextColor
	}

	/** ---- [GraphDesktopViewItem] */

	override val drawingView: DrawingView<GraphView>? get() = null

	override fun disposeItem() {
		memoryContentPanel.dispose()
	}

	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

	override fun addContextColorBorder(color: Color) {
		memoryContentPanel.border = createContextColorBorder(color)
	}

	override fun removeContextColorBorder() {
		memoryContentPanel.border = null
	}

	override fun createCloseRequest(): Any = GraphDesktopViewItemCloseRequest(this, false)
}