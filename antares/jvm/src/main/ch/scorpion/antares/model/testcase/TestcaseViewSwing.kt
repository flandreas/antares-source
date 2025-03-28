package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane

/**
 * Displays a tree of [Testcase]s of a [DigitalGraph].
 */
class TestcaseViewSwing(
	val controller: TestcaseViewController,
	private val eventBus: EventBus = BaseModule.eventBus,
	sheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), TestcaseView {

	companion object {
		val HELP_ID = HelpId("testcaseView")
	}

	private val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val treeView = TestcaseTreeView(controller, eventBus)

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

	/** ---- [TestcaseView] */

	override fun getNewTestcaseName(): String? {
		val name = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("antares.testcase.action.add.question"),
			Translations.getString("antares.testcase.action.add.name"),
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			"Test"
		) as String?
		if (StringUtils.isEmpty(name)) {
			return null
		}
		return name
	}

	override fun confirmDeleteTestcase(): Boolean =
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("antares.testcase.action.delete.question", controller.testcase!!.name.value),
			Translations.getString("antares.testcase.action.delete.name"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION

	/** ---- [TestcaseViewSwing] */

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

		init {
		    description = name
		}
		override fun execute(event: ActionEvent) {
			if (treeView.selectedTestcase != null) {
				treeView.controller.runSelectedTestcaseAction.execute(event)
			} else {
				treeView.controller.runAllTestcasesAction.execute(event)
			}
		}
	}
}