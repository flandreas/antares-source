package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.LightBulb
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class LightBulbView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LightBulb = LightBulb()
) : OrientableRectangularVerticeView<LightBulb>(styleProvider, model) {

	companion object {
		private val SIZE = wInt(4)
		private val DX = cos(PI / 4) * SIZE / 2
		private val DY = sin(PI / 4) * SIZE / 2
	}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: LightBulb?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
		setBounds(LENGTH, -SIZE / 2, SIZE, SIZE)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}

		context.g.color = context.chooseBackground(backgroundColor)
		context.g.fillOval(xInt, yInt, widthInt, heightInt)

		context.g.color = context.chooseForeground(foregroundColor)
		context.g.stroke = stroke
		context.g.drawOval(xInt, yInt, widthInt, heightInt)
		context.g.drawLine(x + (SIZE / 2 - DX), -DY, x + SIZE / 2 + DX, DY)
		context.g.drawLine(x + (SIZE / 2 - DX), DY, x + SIZE / 2 + DX, -DY)
	}
}