package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.HelpAction
import io.antarescircuit.jabbah.edit.module.EditModuleJvm
import io.antarescircuit.jabbah.edit.properties.PropertySheetPanelFactory
import io.antarescircuit.jabbah.graph.view.GraphView
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

	companion object {
		val HELP_ID = HelpId("usecasesView")
		val helpAction: Action = HelpAction.withSmallImage(HELP_ID)
	}

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

		treeViewScrollPane.preferredSize = Dimension(300, treeView.preferredSize.height)
		propertyPanel.preferredSize = Dimension(300, propertyPanel.preferredSize.height)

		splitPane.border = null
		splitPane.add(treeViewScrollPane)
		splitPane.add(propertyPanel)
		splitPane.dividerLocation = BaseModule.settings.getInt("usecasePanel.splitPos", 400)

		add(splitPane, BorderLayout.CENTER)
	}
}
