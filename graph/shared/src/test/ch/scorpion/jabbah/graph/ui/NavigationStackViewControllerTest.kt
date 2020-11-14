package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test

class NavigationStackViewControllerTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val view = mockk<NavigationStackView>(relaxed = true)
	private val controller = NavigationStackViewController(view)

	@Test
	fun shouldUpdateOnRootNameChange() {
		controller.navigationStack.push(entry("Test"))
		controller.navigationStack.rootEntry!!.content.drawing.graph!!.name = Name("New")
		verify(exactly = 2) { view.update() }
	}

	@Test
	fun shouldUpdateOnHeadChange() {
		controller.navigationStack.push(entry("Test"))
		controller.navigationStack.push(entry("Test2"))
		verify(exactly = 2) { view.update() }
	}

	private fun entry(name: String): NavigationStackEntry<GraphView> {
		val graphView = GraphViewBuilder<Boolean>().build()
		graphView.graph!!.name = Name(name)

		val content = mockk<DrawingViewContent<GraphView>>()
		every { content.drawing } returns graphView

		return NavigationStackEntry(subGraphVerticeView = null, content)
	}
}