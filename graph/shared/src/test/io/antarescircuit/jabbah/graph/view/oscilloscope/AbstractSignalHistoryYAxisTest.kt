package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractSignalHistoryYAxisTest {

	companion object {
		private const val DEFAULT_VALUE = 10L
		private const val INSET = 10
		private const val DEFAULT_VALUE_INSET = 20
		private const val HEIGHT = 100
	}

	private var yAxis: LongSignalHistoryYAxis

	init {
		GraphViewTestRule.configure()
		yAxis = LongSignalHistoryYAxis(INSET, INSET, DEFAULT_VALUE, DEFAULT_VALUE_INSET)
		yAxis.setBounds(0, 0, 100, HEIGHT)
	}

	@Test
	fun baselineYShouldBeAtBottomInsetByDefault() {
		assertEquals(HEIGHT - INSET, yAxis.baselineY.toInt())
	}

	@Test
	fun shouldRenderDefaultValueAtDefaultValueTopInset() {
		assertEquals(HEIGHT - 2 * INSET - DEFAULT_VALUE_INSET, -yAxis.signalY(DEFAULT_VALUE).toInt())
	}

	@Test
	fun shouldRenderPositiveNegativeValue() {
		yAxis.setMinMax(-10L, 10L)
		assertEquals(60.0, yAxis.baselineY)
		assertEquals(30.0, yAxis.posScaleMarkY)
		assertEquals(-30.0, yAxis.signalY(10L))
		assertEquals(30.0, yAxis.signalY(-10L))
	}

	@Test
	fun shouldRenderAllNegativeValue() {
		yAxis.setMinMax(-10L, 0L)
		assertEquals(30.0, yAxis.baselineY)
		assertEquals(-0.0, yAxis.signalY(0L))
		assertEquals(60.0, yAxis.signalY(-10L))
	}

	@Test
	fun shouldNotScaleLargerThanDefault() {
		yAxis.setMinMax(-1L, 1L)
		assertEquals(84.0, yAxis.baselineY)
		assertEquals(-6.0, yAxis.signalY(1L))
		assertEquals(6.0, yAxis.signalY(-1L))
	}

	@Test
	fun shouldRenderSinglePositiveValue() {
		yAxis.setMinMax(10L, 10L)
		assertEquals(90.0, yAxis.baselineY)
	}
}

private class LongSignalHistoryYAxis(
	topInset: Int = DEF_TOP_INSET,
	bottomInset: Int = DEF_BOTTOM_INSET,
	defaultValue: Long = 10,
	defaultValueTopInset: Int = DEF_DEFAULT_VALUE_TO_INSET,
	color: CompositeColor = Themes.get<GraphTheme>().figure.color
) : AbstractSignalHistoryYAxis<Long>(topInset, bottomInset, defaultValue, defaultValueTopInset, color) {

	override val lineWidth: Double get() = 0.0

	override val preferredWidth: Int get() = 40

	override fun toMetric(signal: Long): Double = signal.toDouble()
}