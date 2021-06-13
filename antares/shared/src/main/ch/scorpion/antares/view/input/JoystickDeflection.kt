package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Geometry
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import kotlin.math.cos
import kotlin.math.sin

enum class JoystickDeflection(val customName: String) {

	RECTANGULAR("rectangular") {

		override fun drawDeflection(joystickView: JoystickView, context: DrawContext, color: Color) {
			val inset = DigitalComponentView.w(JoystickView.SIZE) / 2 - JoystickView.MAX_DISPLACEMENT - JoystickView.KNOB_RADIUS
			context.g.color = color
			context.g.stroke = STROKE
			context.g.drawRect(
				joystickView.x + inset, joystickView.y + inset,
				joystickView.width - 2 * inset, joystickView.height - 2 * inset)
		}

		override fun calculateContinuousKnobPosition(joystickView: JoystickView, mouseLocation: Point2D): Point2D {
			val center = joystickView.bounds.center.add(joystickView.location)
			return Point2D(
				mouseLocation.x.coerceIn(center.x - JoystickView.MAX_DISPLACEMENT, center.x + JoystickView.MAX_DISPLACEMENT),
				mouseLocation.y.coerceIn(center.y - JoystickView.MAX_DISPLACEMENT, center.y + JoystickView.MAX_DISPLACEMENT)
			).subtract(center)
		}
	},

	CIRCULAR("circular") {
		override fun drawDeflection(joystickView: JoystickView, context: DrawContext, color: Color) {
			val inset = DigitalComponentView.w(JoystickView.SIZE) / 2 - JoystickView.MAX_DISPLACEMENT - JoystickView.KNOB_RADIUS
			context.g.color = color
			context.g.stroke = STROKE
			context.g.drawOval(
				joystickView.x + inset, joystickView.y + inset,
				joystickView.width - 2 * inset, joystickView.height - 2 * inset)
		}

		override fun calculateContinuousKnobPosition(joystickView: JoystickView, mouseLocation: Point2D): Point2D {
			val center = joystickView.bounds.center.add(joystickView.location)

			val angle = -Geometry.angle(Point2D.ZERO, mouseLocation.subtract(center))
			val r = mouseLocation.subtract(center).distance(Point2D.ZERO).coerceAtMost(JoystickView.MAX_DISPLACEMENT)

			return Point2D(r * cos(angle), r * sin(angle))
		}
	};

	companion object {
		private val STROKE = Stroke(width = 0.5f)
		fun withName(customName: String): JoystickDeflection =
			values().firstOrNull { it.customName == customName } ?:
				throw IllegalArgumentException("Unknown JoystickDeflection '$customName'")
	}

	override fun toString(): String {
		return when (this) {
			RECTANGULAR -> Translations.getString("element.joystickDeflection.rectangular")
			CIRCULAR -> Translations.getString("element.joystickDeflection.circular")
		}
	}

	abstract fun drawDeflection(joystickView: JoystickView, context: DrawContext, color: Color)

	abstract fun calculateContinuousKnobPosition(joystickView: JoystickView, mouseLocation: Point2D): Point2D
}