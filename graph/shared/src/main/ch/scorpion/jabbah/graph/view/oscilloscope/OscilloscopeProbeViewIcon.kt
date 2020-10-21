package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.Label

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
		rotationDisplayStrategy = Label.RotationDisplayStrategy.KEEP_HORIZONTAL
	)

	var filled = true

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

	override fun draw(context: DrawContext, location: Point2D) {
		context.g.translate(location.x, location.y)

		if (filled) {
			context.g.color = context.choose(color).backgroundColor
			context.g.fill(PATH)
		}
		context.g.color = context.choose(color).foregroundColor
		context.g.draw(PATH)
		label.draw(context)

		context.g.translate(-location.x, -location.y)
	}
}
