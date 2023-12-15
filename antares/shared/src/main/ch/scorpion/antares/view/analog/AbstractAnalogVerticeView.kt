package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AbstractAnalogVertice
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
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
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

abstract class AbstractAnalogVerticeView<T: AbstractAnalogVertice<*>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T
) : OrientableRectangularVerticeView<T>(styleProvider, model), AnalogElement {

	companion object {
		const val MAIN_PROPERTY_LABEL_DIST = Look.SCALE
	}

	private val mainPropertyLabel = createLabel()

	protected open val mainPropertyValue: String? = null

	init {
		modelExchanged(null)
	}

	/** ---- [AnalogElement] */

	override val isNonLinear: Boolean get() = model.isNonLinear

	override val voltageSourceCount: Int get() = model.voltageSourceCount

	override val postCount: Int get() = model.postCount

	override fun allocateNodes() {
		model.allocateNodes()
	}

	override fun setNode(postId: Int, nodeId: Int) {
		model.setNode(postId, nodeId)
	}

	override fun setVoltageSource(index: Int, sourceId: Int) {
		model.setVoltageSource(index, sourceId)
	}

	override fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*>? = model.getPost(elem, postId)

	override fun setNodeVoltage(postId: Int, voltage: Double) {
		model.setNodeVoltage(postId, voltage)
		calculateCurrent()
	}

	override fun getNodeVoltage(postId: Int): Double = model.getNodeVoltage(postId)

	override fun setCurrent(index: Int, current: Double) {
		// Empty so far
	}

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		model.stamp(analysis)
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
			relLocation = Point2D.ZERO,
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
		}
	}
}