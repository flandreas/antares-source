package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AbstractAnalogVertice
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementProxy
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.LinearColorGradient
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.GraphApplicationContext
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

	override val boundingBox: RectangularShape
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = Rectangle2D(mainPropertyLabel.boundingBox).moveBy(location)
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
				(parent as AnalogGraphView).recalculate(event.signalHandler!!, false)
			}
		} else if (event.reason == AbstractAnalogVertice.REQUEST_REANALYZE) {
			if (event.signalHandler != null && parent is AnalogGraphView) {
				(parent as AnalogGraphView).recalculate(event.signalHandler!!, true)
			}
		}
	}

	protected fun getColorGradient(context: DrawContext, portId1: Int = 1, portId2: Int = 2): LinearColorGradient? {
		if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			val color1 = model.getPort<AnalogSignal>(portId1).net?.signal?.color?.foregroundColor ?: foregroundColor
			val color2 = model.getPort<AnalogSignal>(portId2).net?.signal?.color?.foregroundColor ?: foregroundColor
			return LinearColorGradient(
				bounds.centerLeft,
				transparent.applyTo(color2),
				bounds.centerRight,
				transparent.applyTo(color1))
		}
		return null
	}
}