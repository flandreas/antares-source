package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.Resistor
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.graphics.LinearColorGradient
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.ui.KnobLauncherImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class ResistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Resistor = Resistor()
) : AbstractAnalogVerticeView<Resistor>(styleProvider, model), AnalogBranchVerticeView<Resistor> {

	@Suppress("unused") // Reflective bean property
	var resistance: Double
		get() = model.resistance
		set(value) {
			model.resistance = value
		}

	@Suppress("unused") // Reflective bean property
	var variable: Boolean
		get() = model.variable
		set(value) {
			model.variable = value
		}

	private val actorInteractionHandler by lazy { ResistorViewInteractionHandler() }

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

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawResistor(
			this,
			this.variable,
			context,
			getColorGradient(context) ?: context.chooseForeground(foregroundColor),
			context.chooseBackground(backgroundColor),
			SymbolStyle.RESISTOR_STROKE)
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		if (variable) {
			actorInteractionHandler
		} else {
			super.getActorInteractionHandler(context)
		}

	/** ---- [AbstractRectangularVerticeView] */

	override fun getBoundingBoxImpl(): Rectangle2D =
		super.getBoundingBoxImpl().expandBy(h(2.0), 0.0, h(1.0), 0.0) as Rectangle2D

	/** ---- [AbstractAnalogVerticeView] */

	override val mainPropertyValue: String get() = "${model.resistance.toInt()} Ω"

	private fun getColorGradient(context: DrawContext): LinearColorGradient? {
		if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			val color1 = model.getPort<AnalogSignal>(1).net?.signal?.color?.foregroundColor ?: foregroundColor
			val color2 = model.getPort<AnalogSignal>(2).net?.signal?.color?.foregroundColor ?: foregroundColor
			return LinearColorGradient(
				bounds.centerLeft,
				transparent.applyTo(color2),
				bounds.centerRight,
				transparent.applyTo(color1))
		}
		return null
	}

	/** ---- [ResistorView] */

	private inner class ResistorViewInteractionHandler : AbstractVerticeView.Companion.CannotOpenActorClickHandler() {

		init {
			component = this@ResistorView
		}

		override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			return KnobLauncherImpl.launchAfterDelay(
				initialValue = resistance.toLong(),
				location = boundingBox.center,
				unit = "Ω",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.setState(it.toDouble(), context.signalHandler, (context.view as DrawingView<*>).drawing as GraphView) }
			)
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			return KnobLauncherImpl.launchImmediately(
				view = context.view as DrawingView<*>,
				initialValue = resistance.toLong(),
				location = boundingBox.center,
				unit = "Ω",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.setState(it.toDouble(), context.signalHandler, (context.view as DrawingView<*>).drawing as GraphView) }
			)
		}
	}
}