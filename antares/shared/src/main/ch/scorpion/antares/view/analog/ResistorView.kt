package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Resistor
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
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

class ResistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Resistor = Resistor()
) : OrientableRectangularVerticeView<Resistor>(styleProvider, model) {

	companion object {
		const val LABEL_DIST = Look.SCALE
	}

	@Suppress("unused") // Reflective bean property
	var resistance: Double
		get() = model.resistance
		set(value) {
			model.resistance = value
		}

	private val resistanceLabel = HorizontalLabel(
		owner = this,
		relLocation = Point2D.ZERO,
		orientation = Direction.SOUTH,
		font = font)

	init {
		modelExchanged(null)
	}

	/** ---- [AbstractDrawable] */

	override val boundingBox: Rectangle2D
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = resistanceLabel.boundingBox.moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [AbstractComponent] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		resistanceLabel.rotationChanged()
	}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: Resistor?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), -LENGTH, 0, Direction.EAST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), -LENGTH - SymbolStyle.RESISTOR_WIDTH.toInt(), 0, Direction.WEST))
		setBounds(
			-LENGTH.toDouble() - SymbolStyle.RESISTOR_WIDTH, -SymbolStyle.RESISTER_HEIGHT_HALF,
			SymbolStyle.RESISTOR_WIDTH, 2 * SymbolStyle.RESISTER_HEIGHT_HALF)
		updateLabel()
	}

	override fun draw(context: DrawContext) {
		super.draw(context)
		drawLabel(context)
	}

	private fun drawLabel(context: DrawContext) {
		context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		resistanceLabel.draw(context)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawResistor(
			this,
			context,
			context.chooseForeground(foregroundColor),
			context.chooseBackground(backgroundColor),
			SymbolStyle.RESISTOR_STROKE)
	}

	private fun updateLabel() {
		invalidate()
		resistanceLabel.text = "${model.resistance.toInt()} Ω"
		resistanceLabel.relLocation = Point2D(bounds.centerX, bounds.bottomCenter.y + LABEL_DIST)
		resistanceLabel.rotationChanged()
		invalidate()
		update()
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == Resistor.STATE_RESISTANCE) {
			updateLabel()
		}
	}
}