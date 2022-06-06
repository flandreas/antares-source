package ch.scorpion.antares.view.find

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestCircuitBuilder
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.net.TunnelView
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.view.find.SearchRequest
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.module.EditModule
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DigitalGraphViewSearchTest {

	init {
		AntaresTestRule.configure()
	}

	private val builder = TestCircuitBuilder("test")

	private val view = EditModule.drawingViewFactory.create(builder.graphView as Drawing<Component>, null, false)

	init {
		view.canvas = createCanvas()
	}

	@Test
	fun shouldFindId() {
		builder.add(AndGateView())

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
		val canvas: Canvas = mockk(relaxed = true)
		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
		return canvas
	}
}