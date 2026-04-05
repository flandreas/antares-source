package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.Disposable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.view.CanvasJvm
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.draw.view.FocusDrawablePlayer
import io.antarescircuit.jabbah.draw.view.FocusPanel
import io.antarescircuit.jabbah.draw.view.find.SearchBarSwing
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.draw.view.find.Searchable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.SearchInMetaGraphRequest
import io.antarescircuit.jabbah.graph.module.GraphModuleJvm
import io.antarescircuit.jabbah.graph.ui.desktop.AbstractGraphDesktopViewItemSwing
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopItemHeaderPanelSwing
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItemElementDepthRef
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItemElementRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.VerticeView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*

/**
 * A [javax.swing] implementation of [GraphNavigationView].
 */
class GraphNavigationViewSwing(
	val controller: GraphNavigationViewController,
	override val drawingView: DrawingView<GraphView>,
	private val viewManager: ContentViewManager,
	reusable: Boolean,
	private val contextBorderColor: CompositeColor? = null,
	private val eventBus: EventBus = BaseModule.eventBus,
	allowCloseInHeader: Boolean = true,
) : AbstractGraphDesktopViewItemSwing(reusable), GraphNavigationView {

	private val navigationStack: NavigationStack<GraphView> = controller.navigationStack

	private val mainPanel = JPanel()

	private val navigationStackView = NavigationStackViewSwing(controller.navigationStackViewController)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(
		this,
		navigationStackView,
		{ navigationStack.entry(0)?.name ?: "<Unknown>" },
		eventBus,
		allowClose = allowCloseInHeader
	)

	private val searchBar: SearchBarSwing by lazy {
		val bar = SearchBarSwing(this)
		bar.maximumSize = Dimension(Integer.MAX_VALUE, bar.preferredSize.height)
		bar.border = BorderFactory.createEmptyBorder(3, 3, 3, 0)
		bar
	}

	private var searchBarShown: Boolean = false

	private val hideSearchBarAction = HideSearchBarAction()

	private val searchInMetaGraphHandler: EventHandler<SearchInMetaGraphRequest> = {
		if (it.metaGraphId == controller.drawingView.drawing.graph?.uuid) {
			execute(it.searchRequest)
			if (drawingView.selectionManager.selectionCount > 0) {
				FocusDrawablePlayer.playFocus(drawingView.selectionManager.selection.first(), drawingView)
			}
		}
	}

	private val canvas = CanvasJvm(drawingView)

	private var header: JPanel? = GraphModuleJvm.graphNavigationViewHeaderFactory.createHeader(drawingView.drawing)

	override val showsNavigationRoot: Boolean get() = navigationStack.size == 1

	/**
	 * The instance of [GraphDesktopViewItem] to be taken on drag&drop actions.
	 * This is usually this [GraphNavigationView], but in case it is wrapped within an outer [GraphDesktopViewItem],
	 * that outer one can be set here.
	 */
	var draggedGraphDesktopViewItem: GraphDesktopViewItem = this
		set(value) {
			field = value
			headerPanel.draggedGraphDesktopViewItem = value
		}

	/**
	 * The instance of [JComponent] to be taken on drag&drop actions.
	 * This is usually this [GraphNavigationView], but in case it is wrapped within an outer [JComponent],
	 * that outer one can be set here.
	 */
	var draggedComponent: JComponent = this
		set(value) {
			field = value
			headerPanel.draggedComponent = value
		}

	override val layoutWidth: Int get() = draggedComponent.width

	override val layoutHeight: Int get() = draggedComponent.height

	init {
		controller.view = this
		eventBus.register(SearchInMetaGraphRequest::class, searchInMetaGraphHandler)

		buildUI(contextBorderColor)

		drawingView.addPropertyChangeListener { event ->
			if (event.source === drawingView && event.name == DrawingView.PROP_DRAWING) {
				io.antarescircuit.jabbah.base.System.invokeLater {
					header = GraphModuleJvm.graphNavigationViewHeaderFactory.createHeader(drawingView.drawing)
					updateMainPanel(contextBorderColor?.foregroundColor)
					refresh()
				}
			}
		}
	}

	override fun dispose() {
		canvas.dispose()
		eventBus.unregister(searchInMetaGraphHandler)
		if (header is Disposable) {
			(header as Disposable).dispose()
		}
	}

	private fun buildUI(contextColor: CompositeColor?) {
		mainPanel.layout = BoxLayout(mainPanel, BoxLayout.PAGE_AXIS)

		updateMainPanel(contextColor?.foregroundColor)

		layout = BorderLayout()
		add(mainPanel, BorderLayout.CENTER)
	}

	private fun updateMainPanel(borderColor: Color?) {
		mainPanel.removeAll()
		mainPanel.add(headerPanel)
		if (header != null) {
			mainPanel.add(header)
		}
		if (searchBarShown) {
			mainPanel.add(searchBar)
		}
		if (borderColor == null) {
			mainPanel.add(FocusPanel(drawingView.canvas as JComponent, this, drawingView.canvas as JComponent, viewManager))
		} else {
			val borderPanel = JPanel(BorderLayout())
			borderPanel.border = createContextColorBorder(borderColor)
			borderPanel.add(drawingView.canvas as JComponent)
			mainPanel.add(FocusPanel(borderPanel, this, drawingView.canvas as JComponent, viewManager), BorderLayout.CENTER)
		}
	}

	/** ---- [Searchable] interface */

	override fun showSearchBar() {
		if (searchBarShown) {
			return
		}
		searchBarShown = true
		updateMainPanel(contextColor?.foregroundColor)
		revalidate()
		repaint()

		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hideSearch")
		actionMap.put("hideSearch", hideSearchBarAction)

		searchBar.handleShown()
	}

	override fun execute(request: SearchRequest) {
		EditModule.drawingViewSearchFactory.invoke().execute(drawingView as DrawingView<Drawing<Component>>, request)
	}

	override fun hideSearchBar() {
		if (!searchBarShown) {
			return
		}
		searchBarShown = false
		updateMainPanel(contextColor?.foregroundColor)
		revalidate()
		repaint()

		searchBar.handleHidden()
	}

	/** ---- [GraphNavigationView] interface */

	override val graphView: GraphView get() = controller.drawingView.drawing

	override fun refresh() {
		invalidate()
		revalidate()
	}

	/** ---- [GraphDesktopViewItem] */

	override fun disposeItem() {
		controller.dispose()
	}

	override fun displays(content: Any?): Boolean = content === graphView

	override val isDetached: Boolean = controller.isDetached

	override fun addContextColorBorder(color: Color) {
		updateMainPanel(color)
	}

	override fun removeContextColorBorder() {
		updateMainPanel(null)
	}

	override fun createCloseRequest(): Any = CloseViewRequest(controller.closeTarget)

	override fun createElementRef(verticeViewId: Int): GraphDesktopViewItemElementRef {
		return GraphDesktopViewItemElementDepthRef(verticeViewId, navigationStack.size - 1)
	}

	override fun findElementWithRef(ref: GraphDesktopViewItemElementRef): VerticeView<*>? {
		if (ref !is GraphDesktopViewItemElementDepthRef) {
			return null
		}
		if (ref.depth < 0 || ref.depth > navigationStack.size) {
			return null
		}
		val element = navigationStack.entry(ref.depth)?.content?.drawing?.getWithId(ref.verticeViewId)
		if (element !is VerticeView<*>) {
			return null
		}
		return element
	}

	/** ---- [Searchable]*/

	override val canSearch: Boolean get() = true

	/** ---- [GraphNavigationViewSwing] */

	/** Finds the first [DrawingViewContent] in the navigation stack that fulfills the specified condition, if any.*/
	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? =
		navigationStack.find(condition)

	private inner class HideSearchBarAction : AbstractAction() {
		override fun actionPerformed(e: ActionEvent?) {
			hideSearchBar()
		}
	}
}