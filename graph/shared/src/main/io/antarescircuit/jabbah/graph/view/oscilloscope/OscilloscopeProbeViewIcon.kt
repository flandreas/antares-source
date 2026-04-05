package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.Graphics2D
import io.antarescircuit.jabbah.draw.graphics.Icon
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.RotationDisplayStrategy

/**
 * The graphical figure used in probe views.
 */
class OscilloscopeProbeViewIcon(
	name: String,
	var color: CompositeColor,
	ownerRotation: Rotation = Rotation.R0
) : Icon {

	companion object {

		private const val F = 7.0
		const val SIZE = 5 * F

		private val PATH = System.createPath()
			.moveTo(0.0, SIZE)
			.lineTo(1 * F, 2 * F)
			.curveTo(2.0 * F, -3 * F, 8 * F, 3.0 * F, 3 * F, 4 * F)
			.close()
	}

	private val label = Label(
		text = name,
		font = Themes.get<DrawTheme>().annotation.font,
		location = Point2D(20, 17),
		rotationDisplayStrategy = RotationDisplayStrategy.KEEP_HORIZONTAL
	)

	var filled = true

	var enabled = true

	var name: String
		get() = label.text
		set(value) { label.text = value }

	var ownerRotation: Rotation = ownerRotation
		set(value) {
			if (field != value) {
				field = value
				label.ownerRotation = field
			}
		}

	/** ---- [Icon] */

	override val dim: Dimension2D = Dimension2D(SIZE, SIZE)

	override fun draw(context: DrawContext, location: Point2D, color: CompositeColor) {
		// Parameter 'color' not used. Use this.color instead.
		context.translated(location) {
			if (filled) {
				setColor(it.g, it.choose(this.color).backgroundColor)
				it.g.fill(PATH)
			}
			setColor(it.g, it.choose(this.color).foregroundColor)
			it.g.draw(PATH)
			setColor(it.g, it.choose(this.color).textColor)
			label.draw(it)
		}
	}

	private fun setColor(g: Graphics2D, color: Color) {
		g.color = if (enabled) {
			color
		} else {
			color.withAlpha(128)
		}
	}
}
