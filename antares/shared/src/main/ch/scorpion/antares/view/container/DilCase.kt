package ch.scorpion.antares.view.container

import ch.scorpion.antares.view.container.DilCase.Companion.SCALE
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.AbstractRectangularShape
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.geom.Shape
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy

class DilCase(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
) : RectangularComponent(
	StyleType.FIGURE,
	styleProvider,
	DilShape(0.0, 0.0, DEF_WIDTH.toDouble(), DEF_HEIGHT.toDouble()),
	labelRotation = Rotation.R90,
	labelRotationDisplayStrategy = RotationDisplayStrategy.ROTATE_HALF
) {

	companion object {
		const val SCALE = 7
		private const val DEF_PORT_INSET = 2 * SCALE
		private const val DEF_PORT_DIST = 4 * SCALE
		private const val DEF_PORT_COUNT = 5
		private const val DEF_WIDTH = 10 * SCALE
		private const val DEF_HEIGHT = 2 * DEF_PORT_INSET + (DEF_PORT_COUNT - 1) * DEF_PORT_DIST
	}

	override val type: String
		get() = Translations.getString("antares.dilCase.name")

	override val shapeToDraw: Shape get() = (shape as DilShape).path
}

class DilShape(x: Double, y: Double, width: Double, height: Double) : AbstractRectangularShape(x, y, width, height) {

	companion object {
		private const val MIN_WIDTH = 8 * SCALE
		private const val NOTCH_SIZE_HALF =  1 * SCALE.toDouble()
	}

	var path: Path = createPath(x, y)
		private set

	override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
		if (width >= MIN_WIDTH) {
			super.setFrame(x, y, width, height)
			path = createPath(x, y)
		}
	}

	private fun createPath(x: Double, y: Double): Path {
		return System.createPath()
			.moveTo(x, y)
			.lineTo(x, y + height)
			.lineTo(x + width, y + height)
			.lineTo(x + width, y)
			.lineTo(x + width / 2.0 + NOTCH_SIZE_HALF, y)
			.quadTo(x + width / 2.0 + NOTCH_SIZE_HALF, y + NOTCH_SIZE_HALF, x + width / 2.0, y + NOTCH_SIZE_HALF)
			.quadTo(x + width / 2.0 - NOTCH_SIZE_HALF, y + NOTCH_SIZE_HALF, x + width / 2.0 - NOTCH_SIZE_HALF, y)
			.close()
	}
}