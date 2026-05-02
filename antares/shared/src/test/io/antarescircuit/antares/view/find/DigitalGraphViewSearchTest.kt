package io.antarescircuit.antares.view.find

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.model.net.Tunnel
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.net.tunnel.TunnelView
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.draw.Canvas
import io.antarescircuit.jabbah.draw.view.find.SearchRequest
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.module.EditModule
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DigitalGraphViewSearchTest {

	init {
		AntaresTestRule.configure()
	}

	private val builder = TestCircuitBuilder("test")

	@Suppress("UNCHECKED_CAST")
	private val view = EditModule.drawingViewFactory.create(builder.graphView as Drawing<Component>, null, false, "")

	init {
		view.canvas = createCanvas()
	}

	@Test
	fun shouldFindId() {
		builder.add(LogicGateView.andGateView())

		EditModule.drawingViewSearchFactory().execute(view, SearchRequest("1"))

		assertEquals(1, view.selectionManager.selectionCount)
	}

	@Test
	fun shouldFindInputNames() {
		val a = builder.addInput("AAA")
		val b = builder.addInput("BBB")

		EditModule.drawingViewSearchFactory().execute(view, SearchRequest("BBB"))

		assertEquals(1, view.selectionManager.selectionCount)
		assertFalse(view.selectionManager.isSelected(a))
		assertTrue(view.selectionManager.isSelected(b))
	}

	@Test
	fun shouldFindTunnelNames() {
		val a = builder.add(TunnelView(model = Tunnel("AAA")))
		val b = builder.add(TunnelView(model = Tunnel("BBB")))

		EditModule.drawingViewSearchFactory().execute(view, SearchRequest("BBB"))

		assertEquals(1, view.selectionManager.selectionCount)
		assertFalse(view.selectionManager.isSelected(a))
		assertTrue(view.selectionManager.isSelected(b))
	}

	private fun createCanvas(): Canvas {
		val canvas: Canvas = mock(MockMode.autofill)
		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
		return canvas
	}
}