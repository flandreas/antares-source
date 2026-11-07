package io.antarescircuit.antares.model.testcase

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.AbstractPropertyPanelController
import io.antarescircuit.jabbah.edit.properties.PropertyPanel

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