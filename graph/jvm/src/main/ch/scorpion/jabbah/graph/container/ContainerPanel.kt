package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.app.ComponentSnapAction
import ch.scorpion.jabbah.edit.app.GridSnapAction
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineTool
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleTool
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.LabelTool
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.GraphPanel
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*

/**
 * A [JPanel] for editing the outside [ContainerDrawing] of a [GraphView].
 */
class ContainerPanel(
	val editor: ContainerEditor,
	propertySheetFactory: PropertySheetPanelFactory,
	private val eventBus: EventBus,
	viewManager: ViewManager
) : JPanel() {

	constructor(
		editor: ContainerEditor,
		viewManager: ViewManager
	) : this(editor, EditModuleJvm.propertySheetPanelFactory, BaseModule.eventBus, viewManager)

	/** The [ContainerTreeView] containing all objects of the underlying [GraphView] that have not yet been added to the [ContainerDrawing].*/
	private val treeView = GraphModuleJvm.containerTreeViewFactory.invoke()

	private val propertyPanel: ComponentPropertyPanel

	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	private var editedContainerDrawing: ContainerDrawing? = null

	private val applicationDataEventHandler: (ApplicationDataEvent) -> Unit = { handle(it) }

	init {
		eventBus.register(ApplicationDataEvent::class, applicationDataEventHandler)

		propertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

		treeView.transferHandler = ContainerTransferHandler()
		(editor.view.canvas as JPanel).transferHandler = ComponentTransferHandler(editor, eventBus, ComponentTransferable.FLAVOR)

		buildUI(viewManager)
	}

	fun dispose() {
		eventBus.unregister(ApplicationDataEvent::class, applicationDataEventHandler)
	}

	fun initialize() {
		editor.view.initialize()
		editor.view.applicationContext = GraphApplicationContext()
	}

	fun createToolbars(separator: Boolean = true): ImmutableList<ToolBar> = listOf(
		createToolbar(editor, separator),
		createSettingsToolBar()).toImmutableList()

	/** Notifies this [ContainerPanel] that is has been activated and that it is now visible.*/
	fun activated() {
		// Update the UI of the JTree in order to recalculate the width of the TreeRenderer's JLabels,
		// which are obviously cached by the JTree's UIManager. The tree nodes display the names of domain object,
		// and these names might have been changed while the ContainerPanel wasn't active
		treeView.updateUI()
	}

	/**
	 * Sets the data to be displayed by this [ContainerPanel].
	 * @param graphView the main [GraphView] in the editable main panel
	 * @param containerDrawing the [ContainerDrawing] that represents the outer view of `graphView`
	 */
	fun setData(graphView: GraphView<*>, containerDrawing: ContainerDrawing) {
		editor.view.drawing = containerDrawing
		treeView.update(graphView, containerDrawing)
	}

	private fun buildUI(viewManager: ViewManager) {
		layout = BorderLayout()
		background = Color.GRAY

		val treeViewScrollPanel = JScrollPane(treeView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

		val leftSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
		leftSplitPane.border = null
		leftSplitPane.dividerLocation = 600
		leftSplitPane.add(treeViewScrollPanel)
		leftSplitPane.add(propertyPanel)

		mainSplitPane.dividerLocation = 250
		mainSplitPane.add(leftSplitPane)
		mainSplitPane.add(FocusPanel(editor.view.canvas as JComponent, editor.view, viewManager))

		add(mainSplitPane)
	}

	private fun createToolbar(editor: Editor, separator: Boolean): ToolBar {
		val toolbar = ToolBar(editor)
		if (separator) {
			toolbar.addSeparator()
		}
		toolbar.addTool(editor.currentTool, "/img/pointer.gif", Translations.getString("edit.tool.select"))
		toolbar.addTool(LabelTool(editor) { LabelComponent() }, "/img/text.gif", Translations.getString("edit.component.label"))
		toolbar.addTool(RectangleTool(editor) { RectangleComponent() }, "/img/rectangle.png", Translations.getString("edit.component.rectangle"))
		toolbar.addTool(RectangleTool(editor) { EllipseComponent() }, "/img/ellipse.png", Translations.getString("edit.component.ellipse"))
		toolbar.addTool(PolylineTool(editor) { PolylineComponent() }, "/img/polyline.gif", Translations.getString("edit.component.polyline"))

		return toolbar
	}

	private fun createSettingsToolBar(): ToolBar {
		val toolBar = ToolBar(editor)
		toolBar.addSeparator()

		val gridButton = JToggleButton(ActionWrapperSwing(GridSnapAction(editor)))
		gridButton.text = null
		gridButton.isFocusPainted = false
		gridButton.icon = ImageIcon(GraphPanel::class.java.getResource("/img/snapGrid.gif"))
		gridButton.toolTipText = Translations.getString("edit.action.grid.snap.name")
		toolBar.add(gridButton)

		val button = JToggleButton(ActionWrapperSwing(ComponentSnapAction(editor)))
		button.text = null
		button.isFocusPainted = false
		button.icon = ImageIcon(GraphPanel::class.java.getResource("/img/snap.gif"))
		button.toolTipText = Translations.getString("edit.tool.align.name")
		toolBar.add(button)

		return toolBar
	}

	private fun updateEditability() {
		editor.active = editedContainerDrawing != null
	}

	private fun handle(event: ApplicationDataEvent) {
		if (event.newData == null) {
			editedContainerDrawing = null
			removeAll()
		} else {
			if (event.oldData == null) {
				add(mainSplitPane)
			}
			val metaGraph = event.newData as MetaGraph
			editedContainerDrawing = metaGraph.containerDrawing
			setData(metaGraph.graph.graphView, editedContainerDrawing!!)
		}
		updateEditability()
	}
}