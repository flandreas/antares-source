package io.antarescircuit.antares.view.input

import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Geometry
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Stroke
import kotlin.math.cos
import kotlin.math.sin

enum class JoystickDeflection(val customName: String) {

	RECTANGULAR("rectangular") {

		override fun drawDeflection(joystickView: JoystickView, context: DrawContext, color: Color) {
			val inset = OrientableRectangularVerticeView.w(JoystickView.SIZE) / 2 - JoystickView.MAX_DISPLACEMENT - JoystickView.KNOB_RADIUS
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
			val inset = OrientableRectangularVerticeView.w(JoystickView.SIZE) / 2 - JoystickView.MAX_DISPLACEMENT - JoystickView.KNOB_RADIUS
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

	fun calculateKeyKnobPosition(leftDown: Boolean, rightDown: Boolean, upDown: Boolean, downDown: Boolean): Point2D {
		var dx = 0
		var dy = 0
		if (!(leftDown && rightDown)) {
			if (leftDown) {
				dx = -1
			}
			if (rightDown) {
				dx = 1
			}
		}
		if (!(upDown && downDown)) {
			if (upDown) {
				dy = -1
			}
			if (downDown) {
				dy = 1
			}
		}
		return Point2D(dx * JoystickView.MAX_DISPLACEMENT, dy * JoystickView.MAX_DISPLACEMENT)
	}
}