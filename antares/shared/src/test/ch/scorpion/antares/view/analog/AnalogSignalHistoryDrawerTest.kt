package ch.scorpion.antares.view.analog

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoryImpl
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewTimeline
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalogSignalHistoryDrawerTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHistory = SignalHistoryImpl<AnalogSignal>(100)
	private val timeline = OscilloscopeViewTimeline(100_000.0, signalHistory::maxTime)
	private val yAxis = AnalogSignalHistoryYAxis()
	private val drawer = AnalogSignalHistoryDrawer(yAxis)
	private val lines = mutableListOf<Pair<Point2D, Point2D>>()
	private val drawContext = createDrawContext()

	@BeforeTest
	fun setup() {
		drawer.setBounds(0, 0, 1000, 100)
		yAxis.setBounds(1000, 0, 100, 100)
		drawer.bind(signalHistory, signalHistory, timeline, DrawGraphicsModule.BLACK)
	}

	@Test
	fun shouldDrawPositiveVoltageCurve() {
		signalHistory.add(AnalogSignal(0.0), 1)
		signalHistory.add(AnalogSignal(5.0), 2)
		signalHistory.add(AnalogSignal(0.0), 3)

		drawer.drawCurve(drawContext)

		assertEquals(4, lines.size)
		assertCurveChangeY(3, 98.0, 26.0)
		assertCurveChangeY(1, 26.0, 98.0)
	}

	@Test
	fun shouldDrawNegativeVoltageCurve() {
		signalHistory.add(AnalogSignal(5.0), 1)
		signalHistory.add(AnalogSignal(-5.0), 2)

		drawer.drawCurve(drawContext)

		assertEquals(2, lines.size)
		assertTrue(lines.all { it.first.y <= 100 && it.second.y <= 100 })
	}

	private fun createDrawContext(): DrawContext {
		val drawContext = mockk<DrawContext>(relaxed = true)
		val g2 = mockk<Graphics2D>(relaxed = true)

		val x1 = slot<Double>()
		val y1 = slot<Double>()
		val x2 = slot<Double>()
		val y2 = slot<Double>()
		every { g2.drawLine(capture(x1), capture(y1), capture(x2), capture(y2)) } answers {
			lines.add(Pair(Point2D(x1.captured, y1.captured), Point2D(x2.captured, y2.captured)))
		}

		every { drawContext.g } returns g2
		return drawContext
	}

	private fun assertCurveChangeY(i: Int, y1: Double, y2: Double) {
		assertEquals(y1, lines[i].first.y)
		assertEquals(y1, lines[i].second.y)
		assertTrue(lines[i - 1].first.y == y2 || lines[i - 1].second.y == y2)
	}
}