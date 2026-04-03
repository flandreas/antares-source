package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Resistor
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.RESISTER_HEIGHT_HALF
import ch.scorpion.antares.view.symbolstyle.SymbolStyle.Companion.RESISTOR_WIDTH
import ch.scorpion.jabbah.base.Thousands
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.ui.knob.KnobLauncherImpl
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class ResistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Resistor = Resistor()
) : AbstractAnalogVerticeView<Resistor>(
	styleProvider,
	model,
	Direction.NORTH,
	Rectangle2D(
		-LENGTH.toDouble() - RESISTOR_WIDTH, -RESISTER_HEIGHT_HALF,
		RESISTOR_WIDTH, 2 * RESISTER_HEIGHT_HALF
	)
) {

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

	override val relativeExternalLabelLocation: Point2D get() = Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: Resistor?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), -LENGTH, 0, Direction.EAST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), -LENGTH - RESISTOR_WIDTH.toInt(), 0, Direction.WEST))
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		val applicableForegroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
			getColorGradient(context) ?: styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
		} else {
			context.chooseForeground(foregroundColor)
		}

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawResistor(
			this,
			this.variable,
			context,
			applicableForegroundColor,
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

	override val mainPropertyValue: String get() = "${Thousands.convert(model.resistance.toLong(), " ")}Ω"

	/** ---- [ResistorView] */

	private inner class ResistorViewInteractionHandler : AbstractVerticeView.Companion.CannotOpenActorClickHandler() {

		init {
			component = this@ResistorView
		}

		override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> =
			KnobLauncherImpl.launchAfterDelay(
				initialValue = resistance.toLong(),
				location = boundingBox.center,
				unit = "Ω",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.setState(it.toDouble(), context.signalHandler, (context.view as DrawingView<*>).drawing as AnalogGraphView) },
				signalHandler = context.signalHandler
			)

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler =
			KnobLauncherImpl.launchImmediately(
				view = context.view as DrawingView<*>,
				initialValue = resistance.toLong(),
				location = boundingBox.center,
				unit = "Ω",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.setState(it.toDouble(), context.signalHandler, (context.view as DrawingView<*>).drawing as AnalogGraphView) },
				signalHandler = context.signalHandler
			)
	}
}