package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.Resistor
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.RESISTER_HEIGHT_HALF
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.RESISTOR_WIDTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.ui.knob.KnobLauncherImpl
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView.Companion

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
	var resistance: MagnitudeValue
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

	override val mainPropertyValue: String get() = resistance.toString()

	/** ---- [ResistorView] */

	private inner class ResistorViewInteractionHandler : Companion.CannotOpenActorClickHandler() {

		init {
			component = this@ResistorView
		}

		override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> =
			KnobLauncherImpl.launchAfterDelay(
				initialValue = model.resistance,
				location = boundingBox.center,
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.setState(it, context.signalHandler, (context.view as DrawingView<*,*>).drawing as AnalogGraphView) },
				signalHandler = context.signalHandler
			)

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler =
			KnobLauncherImpl.launchImmediately(
				view = context.view as DrawingView<*,*>,
				initialValue = model.resistance,
				location = boundingBox.center,
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.setState(it, context.signalHandler, (context.view as DrawingView<*,*>).drawing as AnalogGraphView) },
				signalHandler = context.signalHandler
			)
	}
}