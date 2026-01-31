package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.Disposable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.FocusDrawablePlayer
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.view.find.SearchBarSwing
import ch.scorpion.jabbah.draw.view.find.SearchRequest
import ch.scorpion.jabbah.draw.view.find.Searchable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.SearchInMetaGraphRequest
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.desktop.AbstractGraphDesktopViewItemSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopItemHeaderPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemElementDepthRef
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemElementRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
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
				ch.scorpion.jabbah.base.System.invokeLater {
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