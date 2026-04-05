package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class NavigationStackViewControllerTest {

	private lateinit var controller: NavigationStackViewController
	private lateinit var view: NavigationStackView

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
		controller = NavigationStackViewController()
		view = NavigationStackViewMockBuilder(controller).build()
	}

	@Test
	fun shouldUpdateOnRootNameChange() {
		controller.navigationStack.push(entry("Test"))
		controller.navigationStack.rootEntry!!.content.drawing.graph!!.name = Name("New")
		verify(exactly(2)) { view.refresh() }
	}

	@Test
	fun shouldUpdateOnHeadChange() {
		controller.navigationStack.push(entry("Test"))
		controller.navigationStack.push(entry("Test2"))
		verify(exactly(2)) { view.refresh() }
	}

	private fun entry(name: String): NavigationStackEntry<GraphView> {
		val graphView = GraphViewBuilder<Boolean>().build()
		graphView.graph!!.name = Name(name)

		val content = mock<DrawingViewContent<GraphView>>()
		every { content.drawing } returns graphView

		return NavigationStackEntry(subGraphVerticeView = null, content)
	}
}