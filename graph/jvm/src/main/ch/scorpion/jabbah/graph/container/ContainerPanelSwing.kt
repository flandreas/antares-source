package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.FocusPanel
import ch.scorpion.jabbah.edit.ComponentTransferHandler
import ch.scorpion.jabbah.edit.ComponentTransferable
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.figure.FigureGroupsPanel
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelSwing
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.module.GraphModuleJvm
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelController
import ch.scorpion.jabbah.graph.ui.container.ContainerPanelView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

class ContainerPanelSwing(
	val controller: ContainerPanelController,
	application: Application?,
	propertySheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory,
	eventBus: EventBus = BaseModule.eventBus,
	viewManager: ContentViewManager = DrawViewModule.viewManager
) : JPanel(), ContainerPanelView {

	companion object {
		private const val PROP_MAIN_SPLIT_POS = "containerPanel.mainSplitPos"
		private const val PROP_LEFT_SPLIT_POS = "containerPanel.leftSplitPos"
		private const val PROP_CONTENT_SPLIT_POS = "containerPanel.contentSplitPos"
	}

	/**
	 * The [ContainerTreeView] containing all objects of the underlying [GraphView]
	 * that have not yet been added to the [ContainerDrawing].
	 */
	private val treeView = GraphModuleJvm.containerTreeViewFactory.invoke()

	private val propertyPanel: ComponentPropertyPanelSwing

	/** Splits between [treeView] and the [FigureGroupsPanel]. */
	private val contentSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/** Splits between [contentSplitPane] and [propertyPanel]. */
	private val leftSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	/** Splits between [leftSplitPane] and the [DrawingView]'s canvas.*/
	private val mainSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)

	/** Displays the (inverted) value of [ContainerPanelController.isManualContainerCurrent] in the UI.*/
	private val isGeneratedContainerCheckbox = JCheckBox(Translations.getString("graph.property.ContainerDrawing.generated"))

	val toolbars = GraphViewModuleJvm.containerToolBarBuilderFactory().buildToolBars(application, controller.editor, separator = true)

	init {
		controller.view = this

		CanvasJvm(controller.drawingView)
		propertyPanel = ComponentPropertyPanelSwing(controller.propertyPanelController, "container", propertySheetFactory)

		treeView.transferHandler = ContainerTransferHandler()
		(controller.editor.view.canvas as JPanel).transferHandler = ComponentTransferHandler(controller.editor, eventBus, ComponentTransferable.FLAVOR)

		buildUI(viewManager)

		toolbars.add(createMiscellaneousToolbar())
	}

	override fun dispose() {
		treeView.dispose()

		BaseModule.settings.set(PROP_MAIN_SPLIT_POS, mainSplitPane.dividerLocation)
		BaseModule.settings.set(PROP_LEFT_SPLIT_POS, leftSplitPane.dividerLocation)
		BaseModule.settings.set(PROP_CONTENT_SPLIT_POS, contentSplitPane.dividerLocation)
	}

	fun initialize() {
		controller.editor.view.initialize()
	}

	/** ---- [ContainerPanelView] */

	override fun dataChanged() {
		if (controller.graphView == null || controller.containerDrawing == null) {
			removeAll()
		} else {
			if (componentCount == 0) {
				add(mainSplitPane)
			}
			treeView.update(
				controller.graphView!!,
				controller.containerDrawing!!,
				controller.editable
			)
		}
	}

	override fun activeChanged() {
		treeView.updateUI()
	}

	override fun updateIsManualContainer(isManualContainer: Boolean) {
		isGeneratedContainerCheckbox.isSelected = !isManualContainer
		treeView.isManualContainer = isManualContainer
	}

	override fun conformGenerateContainerDrawing(): Boolean =
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("graph.action.containerLayout.question"),
			name,
			JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION

	override fun generateContainerDrawing() {
		treeView.containerTree?.generateContainerDrawing()
	}

	/** ---- [ContainerPanelSwing] */

	private fun createMiscellaneousToolbar(): ToolBar {
		val toolbar = ToolBar(null)
		toolbar.addSeparator()

		isGeneratedContainerCheckbox.isEnabled = false

		// Create JPanel so that JButton displays a border
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
		panel.add(isGeneratedContainerCheckbox)
		panel.add(Box.createHorizontalStrut(10))
		panel.add(JButton(ActionWrapperSwing(controller.generateContainerAction)))
		panel.add(Box.createHorizontalGlue())
		toolbar.add(panel)

		return toolbar
	}

	private fun buildUI(viewManager: ContentViewManager) {
		layout = BorderLayout()

		val treeViewScrollPanel = JScrollPane(treeView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
		treeViewScrollPanel.border = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(1, 2, 0, 2),
			treeViewScrollPanel.border
		)
		treeViewScrollPanel.minimumSize = Dimension(treeViewScrollPanel.minimumSize.width, 200)

		val figuresPanel = FigureGroupsPanel()
		val figuresScrollPane = JScrollPane(figuresPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
		figuresScrollPane.minimumSize = Dimension(figuresScrollPane.width, 200)

		contentSplitPane.border = null
		if (BaseModule.settings.containsKey(PROP_CONTENT_SPLIT_POS)) {
			contentSplitPane.dividerLocation = BaseModule.settings.getInt(PROP_CONTENT_SPLIT_POS, 0)
		}
		contentSplitPane.add(treeViewScrollPanel)
		contentSplitPane.add(figuresScrollPane)

		leftSplitPane.border = null
		if (BaseModule.settings.containsKey(PROP_LEFT_SPLIT_POS)) {
			leftSplitPane.dividerLocation = BaseModule.settings.getInt(PROP_LEFT_SPLIT_POS, 0)
		}
		leftSplitPane.add(contentSplitPane)
		leftSplitPane.add(propertyPanel)

		if (BaseModule.settings.containsKey(PROP_MAIN_SPLIT_POS)) {
			mainSplitPane.dividerLocation = BaseModule.settings.getInt(PROP_MAIN_SPLIT_POS, 0)
		}
		mainSplitPane.add(leftSplitPane)
		mainSplitPane.add(FocusPanel(controller.drawingView.canvas as JComponent, controller.drawingView, controller.drawingView.canvas as JComponent, viewManager))

		add(mainSplitPane)
	}
}
