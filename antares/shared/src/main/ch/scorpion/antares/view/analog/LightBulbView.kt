package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.LightBulb
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class LightBulbView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LightBulb = LightBulb(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR
) : OrientableRectangularVerticeView<LightBulb>(styleProvider, model), LightEmitter, AnalogBranchVerticeView<LightBulb> {

	companion object {
		private val SIZE = wInt(4)
		private val DX = cos(PI / 4) * SIZE / 2
		private val DY = sin(PI / 4) * SIZE / 2

		private val DEFAULT_LIGHT_COLOR = LightColor.WHITE

		/** The current (A) at which the [LightBulbView] starts glowing. */
		private const val DEF_MIN_GLOW_CURRENT = 0.0

		/** The current (A) at which the [LightBulbView] reaches its maximum brightness. */
		private const val DEF_MAX_GLOW_CURRENT = 0.1
	}

	@Suppress("MemberVisibilityCanBePrivate") // Bean Reflection
	var minCurrent: Double = DEF_MIN_GLOW_CURRENT
		set(value) {
			require(value in 0.0..maxCurrent) { Translations.getString("library.element.LightBulb.minCurrent.error") }
			field = value
		}

	@Suppress("MemberVisibilityCanBePrivate") // Bean Reflection
	var maxCurrent: Double = DEF_MAX_GLOW_CURRENT
		set(value) {
			require(value > minCurrent) { Translations.getString("library.element.LightBulb.maxCurrent.error") }
			field = value
		}

	init {
		modelExchanged(null)
	}

	/** ---- [AbstractVerticeView] */

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

		drawBulb(context)
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("minCurrent")) {
			minCurrent = reader.readDouble("minCurrent")
		}
		if (reader.hasAttribute("maxCurrent")) {
			maxCurrent = reader.readDouble("maxCurrent")
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("minCurrent", minCurrent)
		writer.writeDouble("maxCurrent", maxCurrent)
	}

	/** ---- [LightEmitter]  */

	override var lightColor: LightColor = lightColor

	/** ---- [LightBulbView] */

	private fun drawBulb(context: DrawContext) {
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawBulb(context, transparent.applyTo(executionBulbColor))
		} else {
			drawBulb(context, transparent.applyTo(backgroundColor))
		}
	}

	private fun drawBulb(context: DrawContext, background: Color) {
		context.g.color = context.chooseBackground(background)
		context.g.fillOval(xInt, yInt, widthInt, heightInt)

		context.g.color = context.chooseForeground(foregroundColor)
		context.g.stroke = stroke
		context.g.drawOval(xInt, yInt, widthInt, heightInt)

		if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.drawLine(x + (SIZE / 2 - DX), -DY, x + SIZE / 2 + DX, DY)
			context.g.drawLine(x + (SIZE / 2 - DX), DY, x + SIZE / 2 + DX, -DY)
		}
	}

	private val executionBulbColor: Color get() =
		lightColor.gradient.at(
			(((getPortView(model.getPort()) as AnalogPortView).current - minCurrent).coerceAtLeast(0.0) / maxCurrent)
			.coerceIn(0.0..1.0).toFloat())
}