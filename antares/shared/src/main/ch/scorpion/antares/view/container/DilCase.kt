package ch.scorpion.antares.view.container

import ch.scorpion.antares.view.container.DilCase.Companion.SCALE
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Snappable
import ch.scorpion.jabbah.edit.SnappableX
import ch.scorpion.jabbah.edit.SnappableY
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy
import ch.scorpion.jabbah.graph.container.PortViewComponent
import ch.scorpion.jabbah.graph.container.PortViewContainer

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

		private val snapHighlight = EmptySnapHighlight()
	}

	private var _snappableX: Array<SnappableX>? = null
	private var _snappableY: Array<SnappableY>? = null

	/** ---- [RectangularComponent] */

	override val type: String get() = Translations.getString("antares.dilCase.name")

	override val shapeToDraw: Shape get() = (shape as DilShape).path

	override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
		super.setFrame(x, y, width, height)
		_snappableX = null
		_snappableY = null
	}

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX> get() {
		if (_snappableX == null) {
			createSnappable()
		}
		return _snappableX!!
	}

	override val snappableY: Array<SnappableY> get() {
		if (_snappableY == null) {
			createSnappable()
		}
		return _snappableY!!
	}

	override fun getSnapHighlightX(x: Double, y: Double): Unzoomable {
		snapHighlight.location = Point2D(x, y)
		return snapHighlight
	}

	override fun getSnapHighlightY(x: Double, y: Double): Unzoomable? {
		snapHighlight.location = Point2D(x, y)
		return snapHighlight
	}

	data class DilPositionX(override val x: Double, private val isBorder: Boolean) : SnappableX, PortViewContainer {
		override fun accept(other: SnappableX): Boolean =
			other is PortViewComponent<*>
				&& (isBorder && other.direction.isHorizontal() || !isBorder && other.direction.isVertical())
	}

	data class DilPositionY(override val y: Double, private val isBorder: Boolean) : SnappableY, PortViewContainer {
		override fun accept(other: SnappableY): Boolean =
			other is PortViewComponent<*>
				&& (isBorder && other.direction.isVertical() || !isBorder && other.direction.isHorizontal())
	}

	/** ---- [DilCase] */

	private fun createSnappable() {
		val xList = mutableListOf<SnappableX>(
			DilPositionX(minX, isBorder = true),
			DilPositionX(maxX, isBorder = true))
		var snapX = minX + DEF_PORT_INSET
		while (snapX < maxX - DEF_PORT_INSET) {
			xList.add(DilPositionX(snapX, isBorder = false))
			snapX += DEF_PORT_DIST
		}
		_snappableX = xList.toTypedArray()

		val yList = mutableListOf<SnappableY>(
			DilPositionY(minY, isBorder = true),
			DilPositionY(maxY, isBorder = true))
		var snapY = minY + DEF_PORT_INSET
		while (snapY < maxY - DEF_PORT_INSET) {
			yList.add(DilPositionY(snapY, isBorder = false))
			snapY += DEF_PORT_DIST
		}
		_snappableY = yList.toTypedArray()
	}

	private class EmptySnapHighlight : AbstractRectangularUnzoomable(0.0) {
		override val lineWidth: Double get() = 0.0
		override fun draw(context: DrawContext) { }
	}
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