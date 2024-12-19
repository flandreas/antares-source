package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane

/**
 * Displays a tree of [Testcase]s of a [DigitalGraph].
 */
class TestcaseViewSwing(
	controller: TestcaseViewController,
	applicationDataHolder: ApplicationDataHolder,
	applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	sheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), TestcaseView {

	companion object {
		val HELP_ID = HelpId("testcaseView")
	}

	private val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val treeView = TestcaseTreeView(applicationDataHolder, applicationModeHolder, controller.applicationContextHolder, eventBus)

	private val propertyPanel = TestcasePropertyPanelSwing(controller.propertyPanelController, sheetFactory)

	/**
	 * Runs either the selected [Testcase], or all [Testcase]s of the [DigitalGraph], depending on
	 * the selection in the tree.
	 */
	val runAction: Action = RunAction()

	val helpAction: Action = HelpAction.withSmallImage(HELP_ID)

	override var graph: DigitalGraph? = null
		set(value) {
			field = value
			treeView.graph = value
		}

	init {
		controller.view = this

		treeView.addTreeSelectionListener {
			val testcase = treeView.selectedTestcase
			eventBus.post(TestcaseSelectionEvent(graph!!, testcase))
			updateAction()
		}

		treeView.preferredSize = Dimension(300, treeView.preferredSize.height)
		propertyPanel.preferredSize = Dimension(300, propertyPanel.preferredSize.height)

		buildUI()
		updateAction()
	}

	override fun dispose() {
		BaseModule.settings.set("testcasePanel.splitPos", splitPane.dividerLocation)
		treeView.dispose()
		propertyPanel.dispose()
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
		splitPane.dividerLocation = BaseModule.settings.getInt("testcasePanel.splitPos", 400)

		add(splitPane, BorderLayout.CENTER)
	}

	private fun updateAction() {
		runAction.enabled = treeView.selectionCount > 0
	}

	private inner class RunAction : AbstractAction(
		"antares.testcase.action.run",
		"/img/run-16.png"
	) {

		override fun execute(event: ActionEvent) {
			if (treeView.selectedTestcase != null) {
				treeView.runSelectedTestcaseAction.execute(event)
			} else {
				treeView.runAllTestcasesAction.execute(event)
			}
		}
	}
}