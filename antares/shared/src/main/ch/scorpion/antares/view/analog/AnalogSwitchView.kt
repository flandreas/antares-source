package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSwitch
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementProxy
import ch.scorpion.antares.view.input.AbstractSwitchView
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogSwitch = AnalogSwitch(),
	private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : AbstractSwitchView<AnalogSwitch>(styleProvider, model),
	AnalogElement by analogElement
{

	companion object {
		private const val SIZE = 6 * Look.SCALE
	}

	private val label = HorizontalLabel(
		owner = this,
		relLocation = Point2D(LENGTH + SIZE / 2, AbstractAnalogVerticeView.MAIN_PROPERTY_LABEL_DIST),
		orientation = Direction.SOUTH,
		font = font)

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(LENGTH, -SIZE / 2, SIZE, SIZE)
	}

	override fun modelExchanged(oldModel: AnalogSwitch?) {
		super.modelExchanged(oldModel)
		analogElement.bind(model)

		addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
		updateLabels()
	}

	/** ---- [AbstractSwitchView] */

	override val circleRadius: Double get() = DEF_CIRCLE_RADIUS

	override fun updateLabels() {
		invalidate()
		label.text = name ?: ""
		label.rotationChanged()
		invalidate()
		update()
	}

	/** ---- [AbstractVerticeView] */

	override val boundingBox: Rectangle2D
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = label.boundingBox.moveBy(location)
			bb.add(lbb)
			return bb
		}

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		label.rotationChanged()
	}

	override fun draw(context: DrawContext) {
		super.draw(context)
		drawLabel(context)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		drawTwoPortRealSwitchShape(context)
	}

	/** ---- [AnalogSwitchView] */

	private fun drawLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		label.draw(context)
	}
}