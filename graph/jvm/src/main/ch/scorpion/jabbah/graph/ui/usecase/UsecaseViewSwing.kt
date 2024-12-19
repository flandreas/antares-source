package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane

class UsecaseViewSwing(
	controller: UsecaseViewController,
	private val eventBus: EventBus = BaseModule.eventBus,
	sheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), UsecaseView {

	private val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val treeView = UsecaseTreeView(controller, eventBus)

	private val propertyPanel = UsecasePropertyPanelSwing(controller.propertyPanelController, sheetFactory)

	override var graphView: GraphView? = null
		set(value) {
			field = value
			treeView.graphView = value
		}

	init {
		controller.view = this

		treeView.addTreeSelectionListener {
			val usecase = treeView.selectedUsecase
			eventBus.post(UsecaseSelectionEvent(graphView!!, usecase))
		}
		treeView.preferredSize = Dimension(300, treeView.preferredSize.height)
		propertyPanel.preferredSize = Dimension(300, propertyPanel.preferredSize.height)

		buildUI()
	}

	override fun dispose() {
		BaseModule.settings.set("usecasePanel.splitPos", splitPane.dividerLocation)
		treeView.dispose()
		propertyPanel.dispose()
	}

	/** ---- [UsecaseView] */

	override fun getNewUsecaseName(): String? {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("usecases.action.addUsecase.question"),
			Translations.getString("usecases.action.addUsecase.name"),
			JOptionPane.QUESTION_MESSAGE
		)
		if (StringUtils.isEmpty(name)) {
			return null
		}
		return name
	}

	/** ---- [UsecaseViewSwing] */

	fun clearSelection() {
		treeView.selectionModel.clearSelection()
	}

	private fun buildUI() {
		layout = BorderLayout()

		val treeViewScrollPane = JScrollPane(
			treeView,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

		splitPane.border = null
		splitPane.add(treeViewScrollPane)
		splitPane.add(propertyPanel)
		splitPane.dividerLocation = BaseModule.settings.getInt("usecasePanel.splitPos", 400)

		add(splitPane, BorderLayout.CENTER)
	}
}
