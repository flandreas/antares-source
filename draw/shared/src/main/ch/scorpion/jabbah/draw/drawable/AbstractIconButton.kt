package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.style.*

/**
 * An implementation of a simple button as a [Drawable] that uses an [Icon] for rendering.
 * Provides a hovering behaviour by altering the icon's color while the mouse is located
 * of the icon.
 *
 * Uses the properties of [StyleType.ANNOTATION] for drawing.
 */
abstract class AbstractIconButton(
	protected val icon: Icon,
	location: Point2D = Point2D.ZERO,
	var tooltipKey: String? = null
) : AbstractRectangle(location.x, location.y, icon.dim.width, icon.dim.height) {

	companion object {
		private val LOG by logger(AbstractIconButton::class)
		private val STROKE = Themes.get<DrawTheme>().annotation.stroke
	}

	var enabled: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				invalidate()
				validate()
			}
		}

	protected var isHovering = false
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				validate()
			}
		}

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = STROKE.width.toDouble()

	/** ---- [Drawable] interface */

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors

		if (isHovering) {
			context.useContextColors = true
			context.color = Themes.get<DrawTheme>().hover
		} else if (!enabled) {
			context.useContextColors = true
			context.color = Themes.get<DrawTheme>().annotation.color.withAlpha(128)
		} else {
			context.useContextColors = false
		}

		context.g.stroke = STROKE
		icon.draw(context, Point2D(x, y))

		context.useContextColors = oldUseContextColors
	}

	/** ---- [AbstractDrawable] */

	override fun getTooltip(x: Double, y: Double): Tooltip? =
		tooltipKey?.let { Tooltip(Translations.getString(it), toAbsoluteLocation(Point2D(x, y))) }

	override fun update() {
		isHovering = false
		super.update()
	}

	/** ---- [AbstractIconButton] */

	protected fun keepMouseMoved(mouseLocation: Point2D): Boolean {
		if (contains(mouseLocation)) {
			if (!isHovering && enabled) {
				LOG.debug("start hover mode")
				isHovering = true
			}
			return true
		}

		if (isHovering) {
			LOG.debug("stop hover mode")
			isHovering = false
		}
		return false
	}
}