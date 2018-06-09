package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.AbstractApplicationFrame
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.action.AbstractApplicationAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.container.ContainerPanel
import ch.scorpion.jabbah.graph.model.GraphNameChangedEvent
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.BorderLayout
import javax.swing.*


/**
 * The main [AbstractApplicationFrame] of a graph [Application] that allows to switch between
 * a [GraphDesktop] and a [ContainerPanel] for editing the outside view of the main [GraphView].
 */
class GraphFrame(
	application: DesktopApplication,
	private val eventBus: EventBus,
	val viewManager: ViewManager,
	scheduler: Scheduler
) : AbstractApplicationFrame(application) {

	enum class DisplayedView {
		Desktop, Container
	}

	private val LOG by logger(GraphFrame::class)

	private val desktopAction = ViewDesktopAction(this, application, eventBus)

	private val containerAction = ViewContainerAction(this, application, eventBus)

	private val mainToolBar: JToolBar

	private val toolbarPanel: JPanel

	private var displayedView: DisplayedView = DisplayedView.Container

	private val containerPanel = ContainerPanel(GraphViewModule.containerEditorFactory.invoke(eventBus), viewManager)

	val desktop = GraphDesktop(eventBus, viewManager, GraphModuleJvm.graphNavigationPanelFactory, scheduler)

	init {
		eventBus.register(GraphNameChangedEvent::class, { handle(it) })

		toolbarPanel = JPanel()
		toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
		mainToolBar = createMainToolBar()

		showDesktop()
	}

	override val editor: Editor
		get() = desktop.masterGraphPanel!!.editor

	override fun dispose() {
		super.dispose()
		desktop.dispose()
	}

	/** ---- [AbstractApplicationFrame] */

	/** The application data has changed if there are undoable [Command]s in the [CommandManager].*/
	override val applicationDataChanged: Boolean
		get() = desktop.masterGraphPanel!!.editor.commandManager.canUndo()

	/** ---- [GraphFrame] */

	private fun handle(event: GraphNameChangedEvent) {
		if (event.graph === (desktop.masterGraphPanel!!.editor.view.drawing as GraphView<*>).graph!!) {
			containerPanel.setGraphName(event.newName)
		}
	}

	fun showDesktop() {
		if (displayedView == DisplayedView.Desktop) {
			return
		}
		SwingUtilities.invokeLater {
			contentPane.removeAll()
			fillToolbarPanel(desktop.getToolBars())
			contentPane.add(toolbarPanel, BorderLayout.NORTH)
			contentPane.add(desktop, BorderLayout.CENTER)
			invalidate()
			revalidate()
			repaint()

			displayedView = DisplayedView.Desktop
			viewManager.activeView = desktop.masterGraphPanel!!.editor.view
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	fun showContainer() {
		if (displayedView == DisplayedView.Container) {
			return
		}

		SwingUtilities.invokeLater {
			contentPane.removeAll()
			fillToolbarPanel(containerPanel.toolbars)
			contentPane.add(toolbarPanel, BorderLayout.NORTH)
			contentPane.add(containerPanel, BorderLayout.CENTER)
			invalidate()
			revalidate()
			repaint()

			containerPanel.initialize()

			displayedView = DisplayedView.Container
			viewManager.activeView = containerPanel.editor.view
			containerPanel.activated()
			eventBus.post(GraphFrameEvent(this, displayedView))
		}
	}

	private fun createMainToolBar(): JToolBar {
		val toolbar = JToolBar()
		toolbar.isFloatable = false

		val viewDesktopButton = JToggleButton(ActionWrapperSwing(desktopAction))
		viewDesktopButton.icon = ImageIcon(GraphFrame::class.java.getResource("/img/drawing-24.png"))
		viewDesktopButton.text = null
		toolbar.add(viewDesktopButton)

		val viewContainerButton = JToggleButton(ActionWrapperSwing(containerAction))
		viewContainerButton.icon = ImageIcon(GraphFrame::class.java.getResource("/img/container-24.png"))
		viewContainerButton.text = null
		toolbar.add(viewContainerButton)

		return toolbar
	}

	private fun fillToolbarPanel(toolbars: List<JToolBar>) {
		toolbarPanel.removeAll()
		toolbarPanel.add(mainToolBar)
		toolbars.forEach { toolbarPanel.add(it) }
		toolbarPanel.add(Box.createHorizontalGlue())
	}
}

class ViewDesktopAction(
	private val graphFrame: GraphFrame,
	app: DesktopApplication,
	eventBus: EventBus
) : AbstractApplicationAction("view.action.desktop", app) {

	init {
		eventBus.register(GraphFrameEvent::class, {
			selected = it.displayedView == GraphFrame.DisplayedView.Desktop
		})
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		graphFrame.showDesktop()
	}
}

class ViewContainerAction(
	private val graphFrame: GraphFrame,
	app: DesktopApplication,
	eventBus: EventBus
) : AbstractApplicationAction("view.action.container", app) {

	init {
		eventBus.register(GraphFrameEvent::class, {
			selected = it.displayedView == GraphFrame.DisplayedView.Container
		})
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		graphFrame.showContainer()
	}
}

/** Posted by [GraphFrame] when [GraphFrame.DisplayedView] has changed.*/
data class GraphFrameEvent(val graphFrame: GraphFrame, val displayedView: GraphFrame.DisplayedView)