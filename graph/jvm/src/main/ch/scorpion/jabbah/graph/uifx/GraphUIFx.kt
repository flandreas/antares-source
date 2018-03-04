package ch.scorpion.jabbah.graph.uifx

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperFx
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToolBar
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * A controller of the main user interface screen of the graph application that
 * allows to switch between a [GraphDesktopFx] for editing a circuit, and a [ContainerPaneFx]
 * for editing the outside view of a main [GraphView].
 */
class GraphUIFx(
	application: DesktopApplication,
	private val menuBarBuilder: GraphMenuBarBuilderFx,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val viewManager: ViewManager = DrawViewModule.viewManager
) {

	companion object {
		private val LOG by logger(GraphUIFx::class)
	}

	enum class DisplayedView {
		Desktop, Container
	}

	/** Posted by [GraphUIFx] when [displayedView] changes.*/
	data class GraphUIFxEvent(val ui: GraphUIFx, val displayedView: DisplayedView)

	/** The main UI [Node].*/
	private val _node = BorderPane()
	val node: Parent get() = _node

	var displayedView: DisplayedView = DisplayedView.Container
		set(value) {
			if (field != value) {
				field = value
				when(value) {
					DisplayedView.Desktop -> showDesktop()
					DisplayedView.Container -> showContainer()
				}
			}
		}

	private val mainToolBar = createMainToolBar()

	/** Contains the menu bar and the [toolbarPane].*/
	private val topPane = VBox()

	/** Contains the main toolbar and the toolbars of the currently [DisplayedView].*/
	private val toolbarPane = HBox()

	private val desktop: GraphDesktopFx = GraphDesktopFx()

	private val containerPane: ContainerPaneFx = ContainerPaneFx()

	init {
		buildUI()
		displayedView = DisplayedView.Desktop
	}

	/** ---- [GraphUIFx] */

	fun dispose() {
		desktop.dispose()
		containerPane.dispose()
	}

	private fun buildUI() {
		//menuBarBuilder.menuBar.isUseSystemMenuBar = true
		topPane.children.addAll(menuBarBuilder.menuBar, toolbarPane)
		_node.top = topPane
	}

	private fun createMainToolBar(): ToolBar {
		val toolbar = ToolBar()
		toolbar.items.addAll(
			ActionWrapperFx.imageButton(ToggleButton(), ViewDesktopAction(this)),
			ActionWrapperFx.imageButton(ToggleButton(), ViewContainerAction(this)))
		return toolbar
	}

	private fun showDesktop() {
		Platform.runLater {
			fillToolbarPane(desktop.toolbars)
			_node.center = desktop.node
			desktop.activated()
			eventBus.post(GraphUIFxEvent(this, displayedView))
		}
	}

	private fun showContainer() {
		Platform.runLater {
			fillToolbarPane(containerPane.toolbars)
			_node.center = containerPane.node
			containerPane.activated()
			eventBus.post(GraphUIFxEvent(this, displayedView))
		}
	}

	private fun fillToolbarPane(toolbars: List<ToolBar>) {
		toolbarPane.children.clear()
		toolbarPane.children.add(mainToolBar)
		toolbarPane.children.addAll(toolbars)
	}
}

class ViewDesktopAction(
	private val ui: GraphUIFx,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.desktop") {

	init {
		imagePath = "/img/drawing-24.png"
		eventBus.register(GraphUIFx.GraphUIFxEvent::class, { update() })
		update()
	}

	override fun execute(event: ActionEvent) {
		ui.displayedView = GraphUIFx.DisplayedView.Desktop
	}

	private fun update() {
		selected = ui.displayedView == GraphUIFx.DisplayedView.Desktop
	}
}

class ViewContainerAction(
	private val ui: GraphUIFx,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("view.action.container") {

	init {
		imagePath = "/img/container-24.png"
		eventBus.register(GraphUIFx.GraphUIFxEvent::class, { update() })
		update()
	}

	override fun execute(event: ActionEvent) {
		ui.displayedView = GraphUIFx.DisplayedView.Container
	}

	private fun update() {
		selected = ui.displayedView == GraphUIFx.DisplayedView.Container
	}
}
