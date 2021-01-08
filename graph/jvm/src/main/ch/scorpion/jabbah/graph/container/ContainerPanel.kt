package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.draw.ZoomStrategy
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.ComponentTransferHandler
import ch.scorpion.jabbah.edit.ComponentTransferable
import ch.scorpion.jabbah.edit.Editor
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
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanel
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import java.awt.BorderLayout
import javax.swing.*

/**
 * A [JPanel] for editing the outside [ContainerDrawing] of a [GraphView].
 *
 * The current [ContainerDrawing] is established by listening for [ApplicationDataEvent] on the specified
 * [EventBus].
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

	private val applicationDataContentEventHandler : (ApplicationDataContentEvent) -> Unit = { handle (it)}

	private var editable: Boolean = true

	var active: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				// Update the UI of the JTree in order to recalculate the width of the TreeRenderer's JLabels,
				// which are obviously cached by the JTree's UIManager. The tree nodes display the names of domain object,
				// and these names might have been changed while the ContainerPanel wasn't active
				treeView.updateUI()
				editor.active = value && editable
			}
		}

	init {
		eventBus.register(ApplicationDataEvent::class, applicationDataEventHandler)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentEventHandler)

		propertyPanel = ComponentPropertyPanel(editor, propertySheetFactory, eventBus)

		treeView.transferHandler = ContainerTransferHandler()
		(editor.view.canvas as JPanel).transferHandler = ComponentTransferHandler(editor, eventBus, ComponentTransferable.FLAVOR)

		buildUI(viewManager)
	}

	fun dispose() {
		eventBus.unregister(applicationDataEventHandler)
		eventBus.unregister(applicationDataEventHandler)
	}

	fun initialize() {
		editor.view.initialize()
	}

	fun createToolbars(separator: Boolean = true): ImmutableList<ToolBar> {
		val toolbars = listOf(
			createToolbar(editor, separator),
			createSettingsToolBar()).toImmutableList()
		toolbars.forEach { it.isFloatable = false }
		return toolbars
	}

	/**
	 * Sets the data to be displayed by this [ContainerPanel]. This method is used if this
	 * [ContainerPanel] is NOT used for the main application data (in which case it's [ContainerDrawing]
	 * would be indirectly set as of [ApplicationDataEvent]), but in additional / separate context,
	 * e.g. when editing the symbol of a [SubGraphVerticeView].
	 *
	 * @param graphView the main [GraphView] in the editable main panel
	 * @param containerDrawing the [ContainerDrawing] that represents the outer view of `graphView`
	 * @param editable `true` if the user is authorized to edit the [ContainerDrawing]
	 */
	fun setData(graphView: GraphView, containerDrawing: ContainerDrawing, editable: Boolean, applyZoomStrategy: Boolean = true) {
		this.editable = editable

		val oldZoomStrategy = editor.view.defaultZoomStrategy
		if (!applyZoomStrategy) {
			editor.view.defaultZoomStrategy = ZoomStrategy.NONE
		}
		editor.view.drawing = containerDrawing
		if (!applyZoomStrategy) {
			editor.view.defaultZoomStrategy = oldZoomStrategy
		}

		treeView.update(graphView, containerDrawing, editable)
	}

	private fun buildUI(viewManager: ViewManager) {
		layout = BorderLayout()

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
		toolbar.addTool(editor.selectionTool, "/img/pointer24.png", Translations.getString("edit.tool.select"))
		toolbar.addTool(LabelTool(editor, factory = { LabelComponent() } ), "/img/text24.png", Translations.getString("edit.component.label"))
		toolbar.addTool(RectangleTool(editor, factory = { RectangleComponent() }), "/img/rectangle24.png", Translations.getString("edit.component.rectangle"))
		toolbar.addTool(RectangleTool(editor, factory = { EllipseComponent() }), "/img/oval24.png", Translations.getString("edit.component.ellipse"))
		toolbar.addTool(PolylineTool(editor, factory = { PolylineComponent() }), "/img/polyline24.png", Translations.getString("edit.component.polyline"))

		return toolbar
	}

	private fun createSettingsToolBar(): ToolBar {
		val toolBar = ToolBar(editor)
		toolBar.addSeparator()

		val gridButton = JToggleButton(ActionWrapperSwing(GridSnapAction(editor)))
		gridButton.text = null
		gridButton.isFocusPainted = false
		gridButton.icon = UiUtil.themedIcon("/img/grid24.png")
		gridButton.toolTipText = Translations.getString("edit.action.grid.snap.name")
		toolBar.add(gridButton)

		val button = JToggleButton(ActionWrapperSwing(ComponentSnapAction(editor)))
		button.text = null
		button.isFocusPainted = false
		button.icon = UiUtil.themedIcon("/img/snap24.png")
		button.toolTipText = Translations.getString("edit.tool.align.name")
		toolBar.add(button)

		return toolBar
	}

	private fun updateEditability() {
		editor.active = editable && editedContainerDrawing != null
		editor.view.editable = editor.active
	}

	private fun handle(event: ApplicationDataEvent) {
		if (event.newData == null) {
			editedContainerDrawing = null
			editable = false
			removeAll()
		} else {
			if (event.oldData == null) {
				add(mainSplitPane)
			}
			val metaGraph = event.newData!!.content as MetaGraph
			editedContainerDrawing = metaGraph.containerDrawing
			setData(metaGraph.graph.graphView, editedContainerDrawing!!, event.newData?.savable?.editable ?: false)
		}
		updateEditability()
	}

	private fun handle(event: ApplicationDataContentEvent) {
		val metaGraph = event.data.content as MetaGraph
		editedContainerDrawing = metaGraph.containerDrawing
		setData(metaGraph.graph.graphView, editedContainerDrawing!!, editable, applyZoomStrategy = false)
	}
}