package io.antarescircuit.jabbah.graph.view.vertice

import dev.mokkery.mock
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for functionality in [AbstractVerticeView].*/
class AbstractVerticeViewTest {

	init {
		GraphViewTestRule.configure()
	}

	@Test
	fun shouldCloneIncludingModel() {
		val vv = TestVerticeView("test")

		val clone = vv.doClone() as TestVerticeView

		assertEquals("test", clone.model.name)
	}

	@Test
	fun shouldIncludeTypeDescriptionWithoutInstanceDescription() {
		val vv = TestVerticeView()

		assertEquals("*(Test:) TestDescription.", vv.getTooltip(InputEventContext(mock(), x = 100.0))?.text)
	}

	@Test
	fun shouldIncludeInstanceAndTypeDescriptions() {
		val vv = TestVerticeView()
		vv.description = Description("Instance description")

		assertEquals("*(Test:) Instance description.\n\nTestDescription.", vv.getTooltip(InputEventContext(mock(), x = 100.0))?.text)
	}

	@Test
	fun shouldKeepMatchingDescriptionsForNonSubGraphVertices() {
		val vv = TestVerticeView()
		vv.description = Description("TestDescription")

		assertEquals("*(Test:) TestDescription.\n\nTestDescription.", vv.getTooltip(InputEventContext(mock(), x = 100.0))?.text)
	}
}
