package ch.scorpion.antares.model.testcase

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.edit.properties.PropertySheetPanelFactory
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane

class TestcaseViewSwing(
	controller: TestcaseViewController,
	application: Application,
	applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	sheetFactory: PropertySheetPanelFactory = EditModuleJvm.propertySheetPanelFactory
) : JPanel(), TestcaseView {

	private val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)

	private val treeView = TestcaseTreeView(application, applicationModeHolder, controller.applicationContextHolder, eventBus)

	private val propertyPanel = TestcasePropertyPanelSwing(controller.propertyPanelController, sheetFactory)

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
		}

		treeView.preferredSize = Dimension(300, treeView.preferredSize.height)
		propertyPanel.preferredSize = Dimension(300, propertyPanel.preferredSize.height)

		buildUI()
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
}