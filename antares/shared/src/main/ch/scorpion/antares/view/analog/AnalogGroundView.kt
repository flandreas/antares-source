package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogGround
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogGroundView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogGround = AnalogGround()
) : AbstractAnalogVerticeView<AnalogGround>(styleProvider, model) {

	companion object {
		private val SIZE = wInt(4)

		private val TRIANGLE = System.createPath()
			.moveTo(w(-1.5), h(3.5))
			.lineTo(w(1.5), h(3.5))
			.lineTo(w(0), h(5.5))
			.close()
	}

	init {
		modelExchanged(null)
	}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: AnalogGround?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(), 0, LENGTH, Direction.NORTH))
		setBounds(-SIZE / 2, LENGTH, SIZE, SIZE)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(TRIANGLE)
			}
		}

		context.g.color = context.chooseForeground(foregroundColor)
		(getPortView(model.getPort()) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
		context.g.drawLine(0.0, LENGTH.toDouble(), 0.0, h(3.5))

		context.g.color = context.chooseBackground(backgroundColor)
		context.g.fill(TRIANGLE)
		context.g.color = context.chooseForeground(foregroundColor)
		context.g.stroke = stroke
		context.g.draw(TRIANGLE)
	}
}