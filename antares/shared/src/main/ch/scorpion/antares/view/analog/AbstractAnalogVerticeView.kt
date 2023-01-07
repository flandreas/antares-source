package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AbstractAnalogVertice
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

abstract class AbstractAnalogVerticeView<T: AbstractAnalogVertice<*>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T
) : OrientableRectangularVerticeView<T>(styleProvider, model) {

	companion object {
		const val MAIN_PROPERTY_LABEL_DIST = Look.SCALE

	}

	private val mainPropertyLabel = HorizontalLabel(
		owner = this,
		relLocation = Point2D.ZERO,
		orientation = Direction.SOUTH,
		font = font)

	protected abstract val mainPropertyValue: String

	init {
		modelExchanged(null)
	}

	/** ---- [AbstractDrawable] */

	override val boundingBox: Rectangle2D
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = mainPropertyLabel.boundingBox.moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [AbstractComponent] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		mainPropertyLabel.rotationChanged()
	}

	/** ---- [AbstractVerticeView] */

	override fun draw(context: DrawContext) {
		super.draw(context)
		drawLabel(context)
	}

	private fun drawLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		mainPropertyLabel.draw(context)
	}

	/** ---- [AbstractAnalogVerticeView] */

	protected fun updateLabel() {
		invalidate()
		mainPropertyLabel.text = mainPropertyValue
		mainPropertyLabel.relLocation = Point2D(bounds.centerX, bounds.bottomCenter.y + MAIN_PROPERTY_LABEL_DIST)
		mainPropertyLabel.rotationChanged()
		invalidate()
		update()
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == AbstractAnalogVertice.MAIN_PROPERTY_STATE) {
			updateLabel()
		}
	}
}