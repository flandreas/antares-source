package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.edit.properties.magnitude.Magnitude
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistory
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/** Draws a [SignalHistoryTimeline] as a single row of an [OscilloscopeView]. */
interface SignalHistoryTimelineView : RectangularDrawable, ActorView {

	/** The distance in model coordinates by which the trigger grid is displaced. */
	var scrollX: Double

	fun bind(
		gridSignalHistory: SignalHistory<Any>?,
		timeline: SignalHistoryTimeline?
	)

	/** Returns the simulation time corresponding with an x-coordinate on this view. */
	fun getTime(x: Double): SignalHistoryTimelineTime

	/** Returns whether [x] is close enough to the trigger line to start dragging it. */
	fun isTriggerLineAt(x: Double): Boolean

	/** Moves the trigger line to [x], clamped to the drawable timeline area. */
	fun moveTriggerTo(x: Double)

	fun updateGeometry()
}

data class SignalHistoryTimelineTime(
	val absoluteTime: Long,
	val relativeTime: Long
)

/** Draws a fixed ten-division triggered-sweep ruler. */
class SignalHistoryTimelineViewImpl(
	private val rightInset: Int
) : AbstractRectangle(), SignalHistoryTimelineView {

	companion object {
		const val DIVISION_COUNT = 10
		private const val LINE_LENGTH = 4.0
		private const val LABEL_INSET = 3.0
		private const val MIN_LABEL_GAP = 10.0
		private const val TRIGGER_HIT_WIDTH = 8.0
		private val TRIGGER_ANNOTATION = System.createPath()
			.moveTo(0.0, 0.0)
			.lineTo(-5.0, 6.0)
			.lineTo(5.0, 6.0)
			.close()
	}

	private var timeline: SignalHistoryTimeline? = null
	private val actorHandler = TimelineActorHandler()

	private val label = Label(
		text = "",
		font = Themes.get<GraphTheme>().annotation.font,
		color = Themes.get<GraphTheme>().annotation.color.textColor,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.TOP)

	private val leftBorder: Double get() = bounds.minX
	private val rightBorder: Double get() = bounds.x + bounds.width - rightInset
	private val defaultDivisionWidth: Double get() = (rightBorder - leftBorder) / DIVISION_COUNT
	private val divisionWidth: Double
		get() = timeline?.divisionWidth?.takeIf { it > 0.0 } ?: defaultDivisionWidth
	private val centerX: Double get() = leftBorder + (rightBorder - leftBorder) / 2.0
	private val triggerX: Double get() = centerX + (timeline?.triggerOffsetX ?: 0.0)

	override var scrollX: Double
		get() = timeline?.triggerOffsetX ?: 0.0
		set(value) {
			timeline?.triggerOffsetX = clampTriggerOffset(value)
		}

	override fun bind(gridSignalHistory: SignalHistory<Any>?, timeline: SignalHistoryTimeline?) {
		this.timeline = timeline
		timeline?.divisionWidth = 0.0
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = actorHandler

	override fun getTime(x: Double): SignalHistoryTimelineTime {
		val boundTimeline = timeline!!
		val absoluteTime = boundTimeline.getTime(rightBorder - x)
		val triggerTime = boundTimeline.getTime(rightBorder - triggerX)
		return SignalHistoryTimelineTime(absoluteTime, absoluteTime - triggerTime)
	}

	override fun isTriggerLineAt(x: Double): Boolean = abs(x - triggerX) <= TRIGGER_HIT_WIDTH

	override fun moveTriggerTo(x: Double) {
		if (timeline != null) {
			timeline!!.triggerOffsetX = clampTriggerOffset(x - centerX)
		}
	}

	override fun updateGeometry() {
		if (timeline != null) {
			timeline!!.triggerOffsetX = clampTriggerOffset(timeline!!.triggerOffsetX)
			if (timeline!!.divisionWidth <= 0.0) {
				timeline!!.divisionWidth = defaultDivisionWidth
			} else {
				timeline!!.divisionWidth = clampDivisionWidth(timeline!!.divisionWidth, 1)
			}
		}
	}

	override fun draw(context: DrawContext) {
		if (timeline == null) {
			return
		}

		context.g.color = Themes.get<GraphTheme>().annotation.color.foregroundColor
		context.g.font = Themes.get<GraphTheme>().annotation.font

		val triggerTime = timeline!!.getTime(rightBorder - triggerX)
		val drawnLabelBounds = mutableListOf<Pair<Double, Double>>()
		val divisions = buildList {
			add(0)
			for (distance in 1..DIVISION_COUNT / 2) {
				add(-distance)
				add(distance)
			}
		}

		for (division in divisions) {
			val x = triggerX + division * divisionWidth
			if (x !in leftBorder..rightBorder) {
				continue
			}

			val time = timeline!!.getTime(rightBorder - x)
			label.text = formatTime(time - triggerTime)
			label.location = Point2D(x, LINE_LENGTH + LABEL_INSET)

			val labelBounds = label.boundingBox
			val overlapsExistingLabel = drawnLabelBounds.any { (minX, maxX) ->
				labelBounds.minX < maxX + MIN_LABEL_GAP && labelBounds.maxX > minX - MIN_LABEL_GAP
			}
			if (!overlapsExistingLabel) {
				if (division == 0) {
					context.translated(x, 0.0) {
						it.g.fill(TRIGGER_ANNOTATION)
					}
				} else {
					context.g.drawLine(x, 0.0, x, LINE_LENGTH)
				}
				label.draw(context)
				drawnLabelBounds.add(labelBounds.minX to labelBounds.maxX)
			}
		}
	}

	override val lineWidth: Double get() = 0.0

	private fun clampTriggerOffset(offset: Double): Double {
		val maxOffset = (rightBorder - leftBorder) / 2.0
		return max(-maxOffset, min(maxOffset, offset))
	}

	private fun formatTime(time: Long): String {
		val sign = if (time < 0) "-" else ""
		val normalized = MagnitudeValue(
			kotlin.math.abs(time),
			Magnitude.Nano,
			SIUnit.Second)
			.normalize()
		return sign + normalized
	}

	private fun clampDivisionWidth(width: Double, lineIndex: Int): Double {
		val minWidth = 8.0
		val maxWidth = (rightBorder - leftBorder) / abs(lineIndex).coerceAtLeast(1)
		return max(minWidth, min(maxWidth, width))
	}

	private fun gridLineIndexAt(x: Double): Int? {
		if (timeline == null) {
			return null
		}

		return (-DIVISION_COUNT / 2..DIVISION_COUNT / 2)
			.minByOrNull { abs(x - (triggerX + it * divisionWidth)) }
			?.takeIf { abs(x - (triggerX + it * divisionWidth)) <= TRIGGER_HIT_WIDTH }
	}

	private inner class TimelineActorHandler : InputEventHandlerAdapter<ActorInteractionContext>() {
		private var pressX = 0.0
		private var pressOffsetX = 0.0
		private var draggedLineIndex: Int? = null

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
			context.view.setCursor(cursorFor(gridLineIndexAt(context.location.x)))
			return null
		}

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}

			draggedLineIndex = gridLineIndexAt(context.location.x)
			if (draggedLineIndex == null) {
				return null
			}
			context.view.setCursor(cursorFor(draggedLineIndex))

			pressX = context.location.x
			pressOffsetX = scrollX
			return this
		}

		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler? {
			val lineIndex = draggedLineIndex!!
			if (lineIndex == 0) {
				scrollX = pressOffsetX + context.location.x - pressX
			} else {
				val signedDistance = (context.location.x - (centerX + pressOffsetX)) * sign(lineIndex.toDouble())
				val newDivisionWidth = signedDistance / abs(lineIndex)
				timeline!!.divisionWidth = clampDivisionWidth(newDivisionWidth, lineIndex)
			}
			invalidate()
			validate()
			return this
		}

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			draggedLineIndex = null
			return null
		}

		private fun cursorFor(lineIndex: Int?): Cursor =
			when {
				lineIndex == null -> Cursor.DEFAULT
				lineIndex == 0 -> Cursor.MOVE
				else -> Cursor.E_RESIZE
			}
	}
}
