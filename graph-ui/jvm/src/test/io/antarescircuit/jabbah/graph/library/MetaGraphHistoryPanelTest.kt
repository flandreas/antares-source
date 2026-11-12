package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController
import io.antarescircuit.jabbah.graph.view.module.GraphViewModuleJvm
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetaGraphHistoryPanelTest {

	private val historyService: FileMetaGraphHistoryService = mock {
		every { getHistory(any()) } returns emptyList()
	}

	@BeforeTest
	fun setup() {
		GraphViewModuleJvm.require()
	}

	@Test
	fun shouldUnregisterAllEventHandlersWhenDisposed() {
		val graphDataViewController = GraphDataViewController()
		val baseline = BaseModule.eventBus.size

		val panel = MetaGraphHistoryPanel(graphDataViewController, ContainerLibraryElement(), historyService) {}
		assertTrue(BaseModule.eventBus.size > baseline)

		panel.dispose()
		assertEquals(baseline, BaseModule.eventBus.size)
	}
}
