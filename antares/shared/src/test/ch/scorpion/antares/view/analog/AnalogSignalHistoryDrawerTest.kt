package ch.scorpion.antares.view.analog

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistory
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeViewTimeline
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalogSignalHistoryDrawerTest {

	private val signalHistory = SignalHistory<AnalogSignal>(100)
	private val timeline = OscilloscopeViewTimeline(100_000.0, signalHistory::maxTime)
	private lateinit var yAxis: AnalogSignalHistoryYAxis
	private lateinit var drawer: AnalogSignalHistoryDrawer
	private val lines = mutableListOf<Pair<Point2D, Point2D>>()
	private val drawContext = createDrawContext()

	@BeforeTest
	fun setup() {
		AntaresTestRule.configure()
		yAxis = AnalogSignalHistoryYAxis(mock(MockMode.autofill))
		drawer = AnalogSignalHistoryDrawer(0, yAxis)

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
		val g2 = mock<Graphics2D>(MockMode.autofill)
		val drawContext = DrawContext(g2)

		val x1 = Capture.slot<Double>()
		val y1 = Capture.slot<Double>()
		val x2 = Capture.slot<Double>()
		val y2 = Capture.slot<Double>()
		every { g2.drawLine(capture<Double>(x1), capture(y1), capture(x2), capture(y2)) } calls {
			lines.add(Pair(Point2D(x1.get(), y1.get()), Point2D(x2.get(), y2.get())))
		}

		return drawContext
	}

	private fun assertCurveChangeY(i: Int, y1: Double, y2: Double) {
		assertEquals(y1, lines[i].first.y)
		assertEquals(y1, lines[i].second.y)
		assertTrue(lines[i - 1].first.y == y2 || lines[i - 1].second.y == y2)
	}
}