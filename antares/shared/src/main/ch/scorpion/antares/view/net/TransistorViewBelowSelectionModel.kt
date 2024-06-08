package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.geom.Ellipse2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.select.AbstractBelowSelectionModel
import ch.scorpion.jabbah.edit.style.EditStyleType

class TransistorViewBelowSelectionModel(
	transistorView: AbstractTransistorView<*>,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	styleType: StyleType = EditStyleType.SELECTION
) : AbstractBelowSelectionModel<AbstractTransistorView<*>>(transistorView, styleProvider, styleType) {

	private var bounds = Ellipse2D()

	override val boundingBox: RectangularShape get() = bounds

	override fun draw(context: DrawContext) {
		context.g.color = color.foregroundColor
		context.g.fill(bounds)
	}

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	override fun componentUpdated() {
		invalidate()
		val pos = component.location
		bounds = Ellipse2D(
			pos.x + 2.0 * Look.SCALE - outset,
			pos.y -5.0 * Look.SCALE - outset,
			AbstractTransistorView.WIDTH + 2.0 * outset,
			AbstractTransistorView.HEIGHT + 2.0 * outset)
		invalidate()
		validate()
	}
}