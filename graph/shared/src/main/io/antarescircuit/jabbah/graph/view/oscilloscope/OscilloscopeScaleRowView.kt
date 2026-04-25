package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.drawable.DrawableButton
import io.antarescircuit.jabbah.draw.drawable.IconDrawableButtonRenderer
import io.antarescircuit.jabbah.draw.graphics.AddIcon
import io.antarescircuit.jabbah.draw.graphics.KnobIcon
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.execution.actor.ActorDrawableButton
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorViewContainer
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.ui.knob.KnobLauncherImpl
import io.antarescircuit.jabbah.graph.ui.knob.KnobView
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.ICON_BUTTON_SIZE
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.MAX_ROW_NUMBER
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.ROW_INSET
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.TITLE_HEIGHT

/**
 * Part of an [OscilloscopeView].
 *
 * Displays a button for adding additional [OscilloscopeSignalRowViews][OscilloscopeSignalRowView],
 * a button for displaying a [KnobView] to be used for changing the [SignalHistoryTimeline] scale,
 * and a ruler for the guidelines of the leading [OscilloscopeSignalRowViews][OscilloscopeSignalRowView].
 */
class OscilloscopeScaleRowView(
	private val oscilloscopeView: OscilloscopeView,
	location: Point2D,
	rightInset: Int,
	private val service: OscilloscopeViewService,
	factory: OscilloscopeViewFactory
) : ActorViewContainer<Drawable>(location = location, useLocation = true) {

	companion object {
		const val ROW_HEIGHT = ICON_BUTTON_SIZE + 2 * ROW_INSET
	}

	private val addButton = DrawableButton<EditInputEventContext>(
		renderer = IconDrawableButtonRenderer(AddIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE))),
		action = { service.addRow(it.drawingView, oscilloscopeView) },
		location = Point2D(ROW_INSET, ROW_HEIGHT / 2 - ICON_BUTTON_SIZE / 2),
		styleType = StyleType.ANNOTATION
	)

	private val scaleButton = ScaleButton(
		location = Point2D(2 * ROW_INSET + ICON_BUTTON_SIZE, ROW_HEIGHT / 2 - ICON_BUTTON_SIZE / 2))

	val timelineView = factory.createSignalHistoryTimelineView(rightInset)

	init {
		add(addButton)
		add(scaleButton)
		add(timelineView)
		updateState()
	}

	fun updateState() {
		scaleButton.enabled = oscilloscopeView.applicationMode.isExecute()
		addButton.enabled = oscilloscopeView.applicationMode.isEdit()
			&& oscilloscopeView.editable
			&& oscilloscopeView.signalRowViews.size < MAX_ROW_NUMBER
		addButton.tooltipKey = if (addButton.enabled) "graph.action.oscilloscope.addRow.name" else "graph.action.oscilloscope.addRow.limit"
	}

	fun updateLocation() {
		location = Point2D(0, TITLE_HEIGHT + oscilloscopeView.rowHeight * oscilloscopeView.signalRowViews.size)
		updateBoundingBox()
		update()
	}

	fun bindDrawer() {
		timelineView.visible = oscilloscopeView.mode == SignalHistoriesType.Realtime
		timelineView.bind(
			oscilloscopeView.signalRowViews.firstOrNull()?.let { oscilloscopeView.model.getSignalHistory(it.name) },
			oscilloscopeView.timeline)
		updateGeometry()
	}

	fun unbindDrawer() {
		timelineView.bind(null, null)
	}

	private fun updateGeometry() {
		timelineView.setBounds(
			OscilloscopeView.DRAWER_X, 0.0,
			oscilloscopeView.drawerWidth, ROW_HEIGHT.toDouble())
	}

	/** Displays a [KnobView] to be used for changing the [SignalHistoryTimeline]'s scale.*/
	private inner class ScaleButton(
		location: Point2D
	) : ActorDrawableButton<EditInputEventContext>(
		renderer = IconDrawableButtonRenderer(KnobIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE))),
		location = location,
		tooltipKey = "graph.action.oscilloscope.scale.name",
		actorAction = {}
	) {

		override fun createActorInteractionHandler(): InputEventHandlerAdapter<ActorInteractionContext> = MouseMoveHandler()

		private inner class MouseMoveHandler : ActorHandler() {

			override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {

				// Hover highlighting
				if (super.mouseMoved(context) == null) {
					return null
				}

				return KnobLauncherImpl.launchAfterDelay(
					initialValue = oscilloscopeView.timelineScale.toLong(),
					location = boundingBox.center
						.add(oscilloscopeView.location)
						.add(this@OscilloscopeScaleRowView.location),
					unit = "x",
					mouseMovedCondition = { keepMouseMoved(it.location) },
					displayHandler = { isHovering = false },
					valueChangeHandler = { oscilloscopeView.timelineScale = it.toDouble() },
					signalHandler = context.signalHandler
				)
			}

			override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler {
				return KnobLauncherImpl.launchImmediately(
					view = context.view as DrawingView<*,*>,
					initialValue = oscilloscopeView.timelineScale.toLong(),
					location = boundingBox.center
						.add(oscilloscopeView.location)
						.add(this@OscilloscopeScaleRowView.location),
					unit = "x",
					mouseMovedCondition = { keepMouseMoved(it.location) },
					displayHandler = { isHovering = false },
					valueChangeHandler = { oscilloscopeView.timelineScale = it.toDouble() },
					signalHandler = context.signalHandler
				)
			}
		}
	}
}