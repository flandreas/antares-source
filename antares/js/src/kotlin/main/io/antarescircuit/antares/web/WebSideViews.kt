package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.ui.GraphNavigationView
import io.antarescircuit.jabbah.graph.ui.GraphNavigationViewController
import io.antarescircuit.jabbah.graph.ui.NavigationStackView
import io.antarescircuit.jabbah.graph.ui.NavigationStackViewController
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * A sub-circuit opened in its own view next to the main circuit (Alt + double-click, or "Open in new view"),
 * like the desktop's side-by-side desktop items. Mostly used to watch the inside of a sub-circuit while simulating.
 */
internal class WebSideView(
	val id: Int,
	val navigationController: GraphNavigationViewController,
	override val drawingView: DrawingView<GraphElementView<*>, GraphView>,
	override var contextColor: CompositeColor?,
	private val notify: Notify,
) : GraphNavigationView, NavigationStackView {

	val title: String get() = navigationController.navigationStack.peek().name.let { RichText.stripToPlainText(it) }

	/** ---- [GraphNavigationView] / [GraphDesktopViewItem] */
	override val layoutWidth: Int get() = drawingView.width
	override val layoutHeight: Int get() = drawingView.height
	override val graphView: GraphView get() = drawingView.drawing
	override val reusable: Boolean get() = false
	override val showsNavigationRoot: Boolean get() = navigationController.navigationStack.size <= 1
	override fun refresh() = notify()
	override val isDetached: Boolean get() = navigationController.isDetached
	override fun disposeItem() = navigationController.dispose()
	override fun findContent(condition: (DrawingViewContent<GraphElementView<*>, GraphView>) -> Boolean): DrawingViewContent<*, *>? = null
	override fun createCloseRequest(): Any = CloseViewRequest(this)
	override fun displays(content: Any?): Boolean = content === graphView
	override val mainUI: Any? get() = null
	override val view: View<out EditInputEventContext> get() = drawingView
	override fun dispose() {}

	/** ---- [NavigationStackView] */
	override val controller: NavigationStackViewController get() = navigationController.navigationStackViewController
	override var editable: Boolean = false
	override var active: Boolean = false

	companion object {
		private var nextId = 1

		/** Creates the view for [verticeView], like `GraphDesktopViewSwing.createSubGraphDesktopItem`. */
		fun create(
			verticeView: SubGraphVerticeView<*>,
			referenceColor: CompositeColor?,
			isParentDetached: Boolean,
			applicationContextHolder: GraphApplicationContextHolder,
			notify: Notify,
		): WebSideView {
			val subGraphView = verticeView.createSubGraphView(applicationContextHolder.signalHandlerIfActive)
			val drawingView = EditModule.drawingViewFactory.create(subGraphView, applicationContextHolder, false, "")
			val controller = GraphNavigationViewController(isRoot = false, drawingView = drawingView, isParentDetached = isParentDetached)
			val view = WebSideView(nextId++, controller, drawingView, referenceColor, notify)
			controller.closeTarget = view
			controller.navigationStackViewController.view = view
			controller.navigationStackViewController.navigationStack.rootEntry = rootEntryFor(view)
			controller.view = view
			controller.setRootGraphView(drawingView.drawing, editable = false, applyZoomStrategy = true, originSubGraphVerticeView = verticeView)
			return view
		}
	}
}
