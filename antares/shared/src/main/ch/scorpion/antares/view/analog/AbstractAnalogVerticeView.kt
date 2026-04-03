package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AbstractAnalogVertice
import ch.scorpion.antares.model.analog.AnalogSignal
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
import ch.scorpion.jabbah.graph.view.LabeledRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

/**
 * Besides tapping into the simulation process specific to analog circuits,
 * [AbstractAnalogVerticeView] enhances [LabeledRectangularVerticeView] for displaying the name
 * with an additional [HorizontalLabel] for displaying the value of the main physical property,
 * such as electric resistance.
 */
abstract class AbstractAnalogVerticeView<T: AbstractAnalogVertice<*>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	externalLabelDirection: Direction,
	bounds: Rectangle2D,
	private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : LabeledRectangularVerticeView<T>(styleProvider, model, bounds), AnalogElement by analogElement {

	companion object {
		const val MAIN_PROPERTY_LABEL_DIST = 10
	}

	private val mainPropertyLabel = createMainPropertyLabel()

	protected open val mainPropertyValue: String? = null

	init {
		initExternalLabel(externalLabelDirection)
		modelExchanged(null)
		updateMainPropertyLabel()
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
		mainPropertyLabel.update()
	}

	/** ---- [AbstractVerticeView] */

	override fun draw(context: DrawContext) {
		super.draw(context)
		drawMainPropertyLabel(context)
	}

	private fun drawMainPropertyLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		mainPropertyLabel.draw(context)
	}

	/** ---- [AbstractAnalogVerticeView] */

	protected open val mainPropertylabelLocation: Point2D get() =
		Point2D(bounds.centerX, bounds.bottomCenter.y + MAIN_PROPERTY_LABEL_DIST)

	protected open val mainPropertylabelOrientation: Direction get() = Direction.SOUTH

	protected open fun createMainPropertyLabel(): HorizontalLabel =
		HorizontalLabel(
			owner = this,
			relLocation = mainPropertylabelLocation,
			orientation = mainPropertylabelOrientation,
			font = font)

	protected fun updateMainPropertyLabel() {
		invalidate()
		mainPropertyLabel.text = mainPropertyValue ?: ""
		mainPropertyLabel.relLocation = mainPropertylabelLocation
		mainPropertyLabel.update()
		invalidate()
		update()
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == AbstractAnalogVertice.MAIN_PROPERTY_STATE) {
			updateMainPropertyLabel()
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
			// TODO Optimize: Can this be cached?
			return LinearColorGradient(
				bounds.centerLeft,
				transparent.applyTo(color2),
				bounds.centerRight,
				transparent.applyTo(color1))
		}
		return null
	}
}