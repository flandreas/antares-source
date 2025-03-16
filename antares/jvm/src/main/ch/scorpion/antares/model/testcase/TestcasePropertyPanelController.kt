package ch.scorpion.antares.model.testcase

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractPropertyPanelController
import ch.scorpion.jabbah.edit.properties.PropertyPanel

interface TestcasePropertyPanel : PropertyPanel

class TestcasePropertyPanelController(
	editor: Editor,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractPropertyPanelController<TestcasePropertyPanel>(editor) {

	private val testcaseSelectionHandler: EventHandler<TestcaseSelectionEvent> = { bean = it.testcase }

	init {
		eventBus.register(TestcaseSelectionEvent::class, testcaseSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(testcaseSelectionHandler)
	}

	override val description: String?
		get() = when (bean) {
			is Testcase -> (bean as Testcase).name.value
			else -> null
		}
}