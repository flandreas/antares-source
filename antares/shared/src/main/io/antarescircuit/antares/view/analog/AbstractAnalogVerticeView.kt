package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AbstractAnalogVertice
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementProxy
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.LinearColorGradient
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.edit.model.text.HorizontalLabel
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

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

	override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
		customColor?.color ?: super.getEditPortViewColor(styleProvider)

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