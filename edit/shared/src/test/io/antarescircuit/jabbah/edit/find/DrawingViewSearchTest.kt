package io.antarescircuit.jabbah.edit.find

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.draw.Canvas
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.ComponentMockBuilder
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.module.EditModule
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DrawingViewSearchTest {

	init {
		EditTestRule.configure()
	}

	private val drawing = DrawingImpl<Component>()

	private val view: DrawingView<Component, Drawing<Component>> = EditModule.drawingViewFactory.create(drawing, null, false, "")

	init {
		view.canvas = createCanvas()
	}

	@Test
	fun shouldFindComponentIds() {
		val c1 = ComponentMockBuilder().withId(7).build().also { drawing.add(it) }
		val c2 = ComponentMockBuilder().withId(42).build().also { drawing.add(it) }

		EditModule.drawingViewSearchFactory().execute(view, SearchRequest("7"))

		assertEquals(1, view.selectionManager.selectionCount)
		assertTrue(view.selectionManager.isSelected(c1))
		assertFalse(view.selectionManager.isSelected(c2))
	}

	@Test
	fun shouldFindTypeNames() {
		val c1 = ComponentMockBuilder().withType("Hello").build().also { drawing.add(it) }
		val c2 = ComponentMockBuilder().withType("World").build().also { drawing.add(it) }

		EditModule.drawingViewSearchFactory().execute(view, SearchRequest("World"))

		assertEquals(1, view.selectionManager.selectionCount)
		assertFalse(view.selectionManager.isSelected(c1))
		assertTrue(view.selectionManager.isSelected(c2))
	}

	private fun createCanvas(): Canvas {
		val canvas: Canvas = mock(MockMode.autofill)
		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
		return canvas
	}
}