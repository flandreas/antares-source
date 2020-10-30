package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.IconButton
import ch.scorpion.jabbah.draw.graphics.AddIcon
import ch.scorpion.jabbah.draw.graphics.KnobIcon
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.execution.actor.AbstractActorIconButton
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorViewContainer
import ch.scorpion.jabbah.graph.ui.KnobLauncher
import ch.scorpion.jabbah.graph.ui.KnobView
import ch.scorpion.jabbah.graph.view.app.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.DRAWER_W
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.ICON_BUTTON_SIZE
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.MAX_ROW_NUMBER
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.ROW_INSET
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeView.Companion.TITLE_HEIGHT

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
	private val service: OscilloscopeViewService,
	private val factory: OscilloscopeViewFactory
) : ActorViewContainer<Drawable>(location = location, useLocation = true) {

	private val addButton = IconButton<EditInputEventContext>(
		icon = AddIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
		action = { service.addRow(it.drawingView(), oscilloscopeView) },
		location = Point2D(ROW_INSET, factory.rowHeight / 2 - ICON_BUTTON_SIZE / 2))


	private val scaleButton = ScaleButton(
		location = Point2D(2 * ROW_INSET + ICON_BUTTON_SIZE, factory.rowHeight / 2 - ICON_BUTTON_SIZE / 2))

	private val timelineView = factory.createSignalHistoryTimelineView()

	init {
		add(addButton)
		add(scaleButton)

		timelineView.setBounds(OscilloscopeView.DRAWER_X, 0.0, DRAWER_W, factory.rowHeight.toDouble())
		add(timelineView)
	}

	fun updateState() {
		scaleButton.enabled = oscilloscopeView.applicationMode.isExecute()
		addButton.enabled = oscilloscopeView.applicationMode.isEdit() && oscilloscopeView.signalRowViews.size < MAX_ROW_NUMBER
		addButton.tooltipKey = if (addButton.enabled) "graph.action.oscilloscope.addRow.name" else "graph.action.oscilloscope.addRow.limit"
	}

	fun updateLocation() {
		location = Point2D(0, TITLE_HEIGHT + factory.rowHeight * oscilloscopeView.signalRowViews.size)
	}

	fun bindDrawer() {
		timelineView.bind(
			oscilloscopeView.signalRowViews.firstOrNull()?.let { oscilloscopeView.model.getSignalHistory(it.name) },
			oscilloscopeView.timeline)
	}

	fun unbindDrawer() {
		timelineView.bind(null, null)
	}

	/** Displays a [KnobView] to be used for changing the [SignalHistoryTimeline]'s scale.*/
	private inner class ScaleButton(
		location: Point2D
	) : AbstractActorIconButton(
		icon = KnobIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
		location = location,
		tooltipKey = "graph.action.oscilloscope.scale.name",
	) {

		override fun createActorInteractionHandler(): InputEventHandlerAdapter<ActorInteractionContext> = MouseMoveHandler()

		override fun handleClicked(context: ActorInteractionContext) { }

		private inner class MouseMoveHandler : Handler() {
			override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {

				// Hover highlighting
				if (super.mouseMoved(context) == null) {
					return null
				}

				return KnobLauncher.launchAfterDelay(
					initialValue = oscilloscopeView.timelineScale.toLong(),
					location = boundingBox.center
						.add(oscilloscopeView.location)
						.add(this@OscilloscopeScaleRowView.location),
					unit = "x",
					mouseMovedCondition = { keepMouseMoved(it.location) },
					displayHandler = { isHovering = false },
					valueChangeHandler = { oscilloscopeView.timelineScale = it.toDouble() }
				)
			}
		}
	}
}