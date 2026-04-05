package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.animation.AnimationModule
import io.antarescircuit.jabbah.animation.Animator
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.DisplayDuration
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.draw.drawable.FlexibleTextView
import io.antarescircuit.jabbah.draw.drawable.TransparentAnimation
import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.edit.style.EditStyleType
import kotlin.math.abs

/**
 * Displays [ComponentMessage]s in a [DrawingView].
 */
class ComponentMessageDisplayer<T : Drawing<Component>>(
	private val drawingView: DrawingView<T>,
	private val displayGlobalMessages: Boolean,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val animator: Animator = AnimationModule.constantSpeedAnimator
) {

	companion object {

		private const val INSET = 10.0

		/** The inset in view coordinates to apply when making [Drawable]s visible.*/
		private const val MAKE_VISIBLE_INSET = 10.0

		private const val FADE_IN_DURATION_MS = 300

		private const val FADE_OUT_DURATION_MS = 600
	}

	init {
		eventBus.register(ComponentMessage::class, this::handle)
	}

	fun dispose() {
		eventBus.unregister(ComponentMessage::class, this::handle)
	}

	private fun handle(msg: ComponentMessage) {
		if (msg.source != null && !drawingView.drawing.contains(msg.source)) {
			return
		}
		if (msg.source == null && !displayGlobalMessages) {
			return
		}

		val text = if (msg.messageParam == null) {
			Translations.getString(msg.messageKey)
		} else {
			Translations.getString(msg.messageKey, msg.messageParam)
		}

		val messageView = FlexibleTextView(
			text = text,
			anchor = calculateAnchorPoint(msg),
			direction = Direction.SOUTH,
			styleType = determineStyleType(msg.type))

		if (msg.source != null) {
			val makeVisibleOffset = getMakeVisibleOffset(messageView)
			messageView.moveBy(makeVisibleOffset.x, makeVisibleOffset.y)
		}

		@Suppress("UNCHECKED_CAST")
		val container = if (msg.source == null) drawingView.overlayContainer else drawingView.ghostContainer as DrawableContainer<Drawable>

		container.add(messageView)
		container.validate()

		TransparentAnimation.fadeInOut(
			messageView,
			container,
			FADE_IN_DURATION_MS,
			DisplayDuration.calculateMilliseconds(text),
			FADE_OUT_DURATION_MS, animator)
	}

	private fun calculateAnchorPoint(msg: ComponentMessage): Point2D {
		return if (msg.source == null) {
			Point2D(drawingView.width / 2, drawingView.height / 2)
		} else {
			val bbox = msg.source.boundingBox
			Point2D(bbox.centerX, bbox.maxY + INSET)
		}
	}

	/**
	 * Calculates the minimal displacement to apply to the specified [FlexibleTextView] to make it entirely visible
	 * in the [View], given that it is drawn as [Unzoomable], meaning that its size is in view coordinates.
	 */
	private fun getMakeVisibleOffset(messageView: FlexibleTextView): Point2D {
		val bbox = messageView.boundingBox
		val locationView = drawingView.modelToView(messageView.location)
		val minXView = locationView.x - bbox.width / 2
		val maxXView = locationView.x + bbox.width / 2
		val minYView = locationView.y
		val maxYView = locationView.y + bbox.height

		val dx = when {
			minXView - MAKE_VISIBLE_INSET < 0 -> abs(minXView - MAKE_VISIBLE_INSET)
			maxXView + MAKE_VISIBLE_INSET > drawingView.width -> -(maxXView + MAKE_VISIBLE_INSET - drawingView.width)
			else -> 0.0
		}

		val dy = when {
			minYView - MAKE_VISIBLE_INSET < 0 -> abs(minYView - MAKE_VISIBLE_INSET)
			maxYView + MAKE_VISIBLE_INSET > drawingView.height -> -(maxYView + MAKE_VISIBLE_INSET - drawingView.height)
			else -> 0.0
		}

		return Point2D(dx / drawingView.zoomFactor, dy / drawingView.zoomFactor)
	}

	private fun determineStyleType(msgType: ComponentMessageType): StyleType {
		return when (msgType) {
			ComponentMessageType.Info -> EditStyleType.MESSAGE_INFO
			ComponentMessageType.Error -> EditStyleType.MESSAGE_ERROR
		}
	}
}
