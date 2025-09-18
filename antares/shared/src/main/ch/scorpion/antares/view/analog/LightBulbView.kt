package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.LightBulb
import ch.scorpion.antares.model.output.LightEmitterModel
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorExpression
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class LightBulbView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LightBulb = LightBulb(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR
) : AbstractAnalogVerticeView<LightBulb>(styleProvider, model),
	LightEmitter,
	ControlViewSource<LightBulb>,
	ControlView<LightBulb>
{

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.analog.LightBulbView.iconPath"

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

	/** ---- UI properties */

	@Suppress("unused") // Reflective bean property
	var resistance: Double
		get() = model.resistance
		set(value) {
			model.resistance = value
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
				context.g.fillOval(xInt, yInt, widthInt, heightInt)
			}
		}

		drawBulb(context)
	}

	/** ---- [Storable] */

	override fun read(reader: StoreReader) {
		super.read(reader)
		lightColor = LightColor.read("lightColor", reader)
		if (reader.hasAttribute("minCurrent")) {
			minCurrent = reader.readDouble("minCurrent")
		}
		if (reader.hasAttribute("maxCurrent")) {
			maxCurrent = reader.readDouble("maxCurrent")
		}
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		lightColor.write("lightColor", writer)
		writer.writeDouble("minCurrent", minCurrent)
		writer.writeDouble("maxCurrent", maxCurrent)
	}

	/** ---- [LightEmitter]  */

	override var lightColor: LightColor = lightColor

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "lightBulb:${model.id}"

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<LightBulb> {
		val clone = LightBulbView(styleProvider, model, lightColor)
		clone.isShowPortViews = false
		clone.location = Point2D.ZERO
		copyControlViewProperties(this, clone)
		return clone
	}

	/** --- [ControlView] */

	override var isActiveControlView: Boolean = false

	override fun writeModelProperties(writer: StoreWriter) { }

	override fun readModelProperties(reader: StoreReader) { }

	override fun sourcePropertiesChanged(source: ControlViewSource<LightBulb>) {
		if (source is LightBulbView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as LightBulb
	}

	private fun copyControlViewProperties(source: LightBulbView, dest: LightBulbView) {
		dest.lightColor = source.lightColor
		dest.orientation = source.orientation
	}

	/** ---- [AbstractGraphElementView] */

	override fun bind(graphView: GraphView, deep: Boolean) {
		super.bind(graphView, deep)
		graphParamsChanged(graphView.graph!!)
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == LightEmitterModel.REASON_GRAPH_PARAM_CHANGED && event.argument is Graph) {
			graphParamsChanged(event.argument as Graph)
		}
	}

	private fun graphParamsChanged(graph: Graph) {
		(lightColor as? LightColorExpression)?.let { it.evaluateIn(graph)?.let { lc -> lightColor = lc } }
	}

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

	@Suppress("MemberVisibilityCanBePrivate") // For testing
	val executionLightFactor: Float get() = ((abs((model.getPort<AnalogSignal>() as AnalogPort).current) - minCurrent).coerceAtLeast(0.0) / maxCurrent)
		.coerceIn(0.0..1.0).toFloat()

	private val executionBulbColor: Color get() = lightColor.gradient.at(executionLightFactor)
}