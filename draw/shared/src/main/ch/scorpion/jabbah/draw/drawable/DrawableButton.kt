package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.style.*

fun interface ButtonAction<C: InputEventContext> {
	fun execute(context: C)
}

/**
 * An base implementation of a button as a [Drawable] providing a hovering effect.
 *
 * @property specialColor implementations of [DrawableButtonRenderer] use the [color] property
 * for rendering this [DrawableButton], which resolved in using [Style.color]. Since [Style] only
 * supports [PredefinedColor] as custom colors, [specialColor] can be provided if a freely defined
 * color is needed.
 */
open class DrawableButton<C: InputEventContext>(
	location: Point2D,
	var tooltipKey: String? = null,
	stylable: Stylable,
	private val action: ButtonAction<C>,
	val renderer: DrawableButtonRenderer,
	private val specialColor: CompositeColor? = null,
	round: Boolean = false
) : AbstractRectangle(createShape(location, renderer.dimension, round)), Stylable by stylable {

	constructor(
		location: Point2D,
		tooltipKey: String? = null,
		styleType: StyleType,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider,
		action: ButtonAction<C>,
		renderer: DrawableButtonRenderer
	) : this(location, tooltipKey, StylableImpl(styleType = styleType, styleProvider = styleProvider), action, renderer)

	companion object {
		private val LOG by logger(DrawableButton::class)
		private const val CORNER_ARC = 6.0

		private fun createShape(location: Point2D, dimension: Dimension2D, round: Boolean): MutableRectangularShape {
			return if (round) {
				RoundRectangle2D(location, dimension, CORNER_ARC)
			} else {
				Rectangle2D(location, dimension)
			}
		}
	}

	val buttonColor: CompositeColor get() = specialColor ?: color

	var enabled: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				invalidate()
				validate()
			}
		}

	var isHovering = false
		protected set(value) {
			if (field != value) {
				invalidate()
				field = value
				validate()
			}
		}

	private val handler = createEditInteractionHandler()

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = style.stroke.width.toDouble()

	/** ---- [AbstractDrawable] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		handler as InputEventHandler<T>

	override fun getTooltip(x: Double, y: Double, editable: Boolean): Tooltip? =
		tooltipKey?.let {
			Tooltip(Translations.getString(it), Rectangle2D.pointLike(toAbsoluteLocation(Point2D(x, y))))
		}

	override fun update() {
		isHovering = false
		super.update()
	}

	override fun draw(context: DrawContext) {
		renderer.draw(this, context)
	}

	/** ---- AbstractDrawableButton */

	protected open fun createEditInteractionHandler(): InputEventHandler<C> = EditInteractionHandler()

	protected fun keepMouseMoved(mouseLocation: Point2D): Boolean {
		if (contains(mouseLocation)) {
			if (!isHovering && enabled) {
				LOG.trace("start hover mode")
				isHovering = true
			}
			return true
		}

		if (isHovering) {
			LOG.trace("stop hover mode")
			isHovering = false
		}
		return false
	}

	protected open inner class EditInteractionHandler : InputEventHandlerAdapter<C>() {

		override fun mouseMoved(context: C): InputEventHandler<C>? {
			return if (keepMouseMoved(context.location)) this else null
		}

		override fun mousePressed(context: C): InputEventHandler<C>? {
			// avoid involvement of surrounding SelectionTool
			return this
		}

		override fun mouseClicked(context: C): InputEventHandler<C>? {
			if (enabled) {
				context.mouseEvent?.consumeEvent()
				action.execute(context)
			}
			return null
		}
	}
}