package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.ui.desktop.AbstractGraphDesktopItemPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopItemHeaderPanelSwing
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import javax.swing.JComponent
import javax.swing.JLayeredPane
import javax.swing.JPanel


/**
 * A [javax.swing] implementation of [GraphNavigationView].
 */
class GraphNavigationViewSwing(
	private val controller: GraphNavigationViewController,
	override val drawingView: DrawingView<GraphView>,
	private val viewManager: ViewManager,
	contextBorderColor: CompositeColor? = null,
	eventBus: EventBus = BaseModule.eventBus,
	allowCloseInHeader: Boolean = true
) : AbstractGraphDesktopItemPanelSwing(), GraphNavigationView {

	private val navigationStack: NavigationStack<GraphView> = controller.navigationStack

	private val mainPanel = JPanel(BorderLayout())

	private val navigationStackView = NavigationStackViewSwing(controller.navigationStackViewController)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(this, navigationStackView, eventBus, allowClose = allowCloseInHeader)

	private val layeredPane = JLayeredPane()

	val showsNavigationRoot: Boolean get() = navigationStack.size == 1

	init {
		controller.view = this
		buildUI(contextBorderColor)
	}

	override fun dispose() { }

	/** ---- [GraphNavigationView] interface */

	override fun refresh() {
		invalidate()
		revalidate()
	}

	/** ---- [GraphDesktopViewItem] */

	override fun disposeItem() {
		controller.dispose()
	}

	override val isDetached: Boolean = controller.isDetached

	override fun addContextColorBorder(color: Color) {
		mainPanel.removeAll()
		mainPanel.add(headerPanel, BorderLayout.NORTH)
		val borderPanel = JPanel(BorderLayout())
		borderPanel.border = createContextColorBorder(color)
		borderPanel.add(layeredPane)
		mainPanel.add(FocusPanel(borderPanel, drawingView, viewManager), BorderLayout.CENTER)
	}

	override fun removeContextColorBorder() {
		mainPanel.removeAll()
		mainPanel.add(headerPanel, BorderLayout.NORTH)
		mainPanel.add(FocusPanel(layeredPane, drawingView, viewManager))
	}

	override fun createCloseRequest(): Any = CloseViewRequest(drawingView)

	/** ---- [GraphNavigationViewSwing] */

	@Suppress("unused")
	fun setGlassPaneComponent(component: JComponent) {
		removeGlassPaneComponent()
		layeredPane.add(component, JLayeredPane.DRAG_LAYER)
		revalidate()
		repaint()
	}

	@Suppress("MemberVisibilityCanBePrivate")
	fun removeGlassPaneComponent() {
		val components = layeredPane.getComponentsInLayer(JLayeredPane.DRAG_LAYER)
		if (components != null) {
			for (i in components.indices) {
				layeredPane.remove(components[i])
			}
			revalidate()
			repaint()
		}
	}

	/** Finds the first [DrawingViewContent] in the navigation stack that fulfills the specified condition, if any.*/
	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? =
		navigationStack.find(condition)

	private fun buildUI(contextColor: CompositeColor?) {
		CanvasJvm(drawingView)

		layeredPane.layout = LayerLayoutManager()
		layeredPane.add(drawingView.canvas as JComponent, JLayeredPane.DEFAULT_LAYER)

		mainPanel.add(headerPanel, BorderLayout.NORTH)

		mainPanel.add(FocusPanel(layeredPane, drawingView, viewManager))
		this.contextColor = contextColor

		layout = BorderLayout()
		add(mainPanel, BorderLayout.CENTER)
	}

	/** Layouts all contained [Component]s to fully take up the entire size of the parent [Container].*/
	private inner class LayerLayoutManager : LayoutManager {

		override fun layoutContainer(parent: Container?) {
			synchronized(parent!!.treeLock) {
				if (parent.componentCount > 0) {
					for (i in 0 until parent.componentCount) {
						parent.getComponent(i).setBounds(0, 0, parent.width, parent.height)
					}
				}
			}
		}

		override fun preferredLayoutSize(parent: Container?): Dimension =
			(drawingView.canvas as JComponent).preferredSize

		override fun minimumLayoutSize(parent: Container?): Dimension = Dimension()

		override fun addLayoutComponent(name: String?, comp: java.awt.Component?) { }

		override fun removeLayoutComponent(comp: java.awt.Component?) { }
	}
}