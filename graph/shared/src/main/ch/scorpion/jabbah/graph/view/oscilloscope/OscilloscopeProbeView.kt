package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.text.StyledTextBuilder
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.drawable.IconButton
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * The probe view that is contained in a row of a [OscilloscopeView]. It contains interaction logic
 * for picking the [OscilloscopeProbeViewIcon] and dragging it as [OscilloscopeProbeVerticeView]
 * into the [GraphView].
 *
 * @param color the color of the [OscilloscopeProbeViewIcon]
 * @param location the location relative to the [DrawableContainer] containing this [OscilloscopeProbeView]
 * @param origLocSource returns the location relative to which the new [OscilloscopeProbeVerticeView] is inserted
 */
class OscilloscopeProbeView(
	location: Point2D,
	name: String,
	private val color: CompositeColor,
	private val origLocSource: () -> Point2D,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : IconButton<EditInputEventContext>(
	icon = OscilloscopeProbeViewIcon(name, color),
	action = {},
	location = location,
) {

	companion object {
		private val LOG by logger(OscilloscopeProbeView::class)

		// x,y displacement used at start of dragging to make it recognizable by the user
		private const val DRAG_DISPLACEMENT = 5.0
	}

	var name: String
		get() = probeIcon.name
		set(value) {
			probeIcon.name = value
			verticeView?.let { it.name = value }
		}

	/**
	 * The [OscilloscopeProbeVerticeView] to be dragged into the [GraphView].
	 * Exists during dragging, and when being contained in the [GraphView].
	 * Can be set by [OscilloscopeView] while reading from persistent store.
	 */
	var verticeView: OscilloscopeProbeVerticeView<Any>? = null
		set(value) {
			if (field !== value) {
				field = value
				verticeViewPresent = false
				probeIcon.filled = false
			}
		}

	private val probeIcon get() = icon as OscilloscopeProbeViewIcon

	/** Set to `false` if [verticeView] has been dragged into the [GraphView].*/
	private var verticeViewPresent = true

	/** ---- [Drawable] */

	override val lineWidth: Double get() = 0.0

	override fun getTooltip(x: Double, y: Double): Tooltip? =
		if (verticeViewPresent) createTooltip(x, y) else null

	override fun createEditInteractionHandler(): InputEventHandler<InputEventContext> =
		Handler() as InputEventHandler<InputEventContext>

	/** ---- [OscilloscopeProbeView] */

	private fun createTooltip(x: Double, y: Double): Tooltip =
		Tooltip(StyledTextBuilder().append(Translations.getString("graph.action.oscilloscope.dragProbe.name")).build(), toAbsoluteLocation(Point2D(x, y)))

	fun handleProbeViewRemovedFromDrawing() {
		invalidate()
		verticeView = null
		verticeViewPresent = true
		probeIcon.filled = true
		validate()
	}

	/**
	 * Handles hovering on this [OscilloscopeProbeView] and delegates to the [InputEventHandler] of
	 * its [OscilloscopeProbeVerticeView] to control dragging into the [GraphView].
	 */
	private inner class Handler : EditInteractionHandler() {

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (!verticeViewPresent) {
				// don't hover/highlight
				return null
			}
			return super.mouseMoved(context)
		}

		override fun mousePressed(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			LOG.trace("OscilloscopeProbeView pressed ${context.x},${context.y}")

			if (!verticeViewPresent) {
				return null
			}

			startDraggingOfCreatedVerticeView(context).also {
				verticeView = it
				return it.getInputEventHandler(context).mouseMoved(context)
			}
		}

		private fun startDraggingOfCreatedVerticeView(context: EditInputEventContext): OscilloscopeProbeVerticeView<Any> {
			invalidate()

			probeIcon.filled = false
			isHovering = false
			verticeViewPresent = false

			val vv = OscilloscopeProbeVerticeView<Any>(name = name, color = color, styleProvider = styleProvider).let {
				it.location = origLocSource.invoke().add(location)
					.add(Point2D(0.0, height)) // origin is at the tip of the bubble, i.e. the BOTTOM of the icon rectangle
					.add(Point2D(DRAG_DISPLACEMENT, DRAG_DISPLACEMENT))
				it.visible = true
				context.editor.drawing.add(it)
				it
			}

			context.mouseEvent?.consume()
			validate()

			return vv
		}
	}
}