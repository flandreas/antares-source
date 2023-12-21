package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AbstractAnalogVertice
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementProxy
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
	model: T,
	private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : OrientableRectangularVerticeView<T>(styleProvider, model), AnalogElement by analogElement {

	companion object {
		const val MAIN_PROPERTY_LABEL_DIST = 10
	}

	private val mainPropertyLabel = createLabel()

	protected open val mainPropertyValue: String? = null

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)
		analogElement.bind(model)
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

	protected open val labelLocation: Point2D get() = Point2D(bounds.centerX, bounds.bottomCenter.y + MAIN_PROPERTY_LABEL_DIST)

	protected open val labelOrientation: Direction get() = Direction.SOUTH

	protected open fun createLabel(): HorizontalLabel =
		HorizontalLabel(
			owner = this,
			relLocation = labelLocation,
			orientation = labelOrientation,
			font = font)

	protected fun updateLabel() {
		invalidate()
		mainPropertyLabel.text = mainPropertyValue ?: ""
		mainPropertyLabel.relLocation = labelLocation
		mainPropertyLabel.rotationChanged()
		invalidate()
		update()
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == AbstractAnalogVertice.MAIN_PROPERTY_STATE) {
			updateLabel()
		} else if (event.reason == AbstractAnalogVertice.REQUEST_RECALCULATE) {
			if (event.signalHandler != null && parent is AnalogGraphView) {
				(parent as AnalogGraphView).recalculate(event.signalHandler!!)
			}
		}
	}
}