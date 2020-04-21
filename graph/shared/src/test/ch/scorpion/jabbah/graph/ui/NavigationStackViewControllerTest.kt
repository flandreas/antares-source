package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
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

	private val eventBus = EventBusImpl()
	private val view = mockk<NavigationStackView>(relaxed = true)
	private val controller = NavigationStackViewController(view, eventBus)

	@Test
	fun shouldUpdateOnRootNameChange() {
		controller.navigationStack.push(entry("Test"))
		controller.navigationStack.rootEntry!!.graphName!!.translation = TranslatableText("New")
		verify(exactly = 1) { view.update() }
	}

	@Test
	fun shouldUpdateOnHeadChange() {
		controller.navigationStack.push(entry("Test"))
		controller.navigationStack.push(entry("Test2"))
		verify(exactly = 2) { view.update() }
	}

	private fun entry(name: String): NavigationStackEntry<GraphView> {
		val content = mockk<DrawingViewContent<GraphView>>()
		val entry = mockk<NavigationStackEntry<GraphView>>()
		every { entry.graphName } returns Name(name)
		every { entry.content } returns content
		return entry
	}
}