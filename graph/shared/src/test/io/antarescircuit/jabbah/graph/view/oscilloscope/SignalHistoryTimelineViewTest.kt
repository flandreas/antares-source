package io.antarescircuit.jabbah.graph.view.oscilloscope

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignalHistoryTimelineViewTest {

	private lateinit var timeline: OscilloscopeViewTimeline
	private lateinit var view: SignalHistoryTimelineViewImpl

	@BeforeTest
	fun setup() {
		var maxTime = 1_000L
		timeline = OscilloscopeViewTimeline(scale = 1_000.0) { maxTime }
		view = SignalHistoryTimelineViewImpl(rightInset = 20)
		view.setBounds(0.0, 0.0, 100.0, 20.0)
		view.bind(null, timeline)
	}

	@Test
	fun shouldUseCenterAsZero() {
		val time = view.getTime(40.0)

		assertEquals(600L, time.absoluteTime)
		assertEquals(0L, time.relativeTime)
	}

	@Test
	fun shouldCalculateRelativeTimeFromDivisionPosition() {
		assertEquals(-80L, view.getTime(32.0).relativeTime)
		assertEquals(80L, view.getTime(48.0).relativeTime)
	}

	@Test
	fun shouldMoveTriggerLineWithoutChangingCurveTimeMapping() {
		view.moveTriggerTo(60.0)

		assertEquals(0L, view.getTime(60.0).relativeTime)
		assertEquals(-200L, view.getTime(40.0).relativeTime)
	}

	@Test
	fun shouldClampTriggerLineToTimelineBounds() {
		view.moveTriggerTo(-100.0)
		assertEquals(0L, view.getTime(0.0).relativeTime)

		view.moveTriggerTo(200.0)
		assertEquals(0L, view.getTime(80.0).relativeTime)
	}
}
