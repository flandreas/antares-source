package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AbstractAnalogVertice
import io.antarescircuit.antares.model.analog.AnalogSwitch
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementProxy
import io.antarescircuit.antares.view.input.AbstractSwitchView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogSwitch = AnalogSwitch(),
	private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : AbstractSwitchView<AnalogSwitch>(styleProvider, model),
	AnalogElement by analogElement,
	ControlViewSource<AnalogSwitch>
{

	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.AnalogSwitchView.iconPath"
	}

	init {
		initExternalLabel(Direction.NORTH)
		isFocusable = true
		modelExchanged(null)
		setBounds(LENGTH, -REAL_SWITCH_HEIGHT_ABOVE, REAL_SWITCH_WIDTH, REAL_SWITCH_HEIGHT_ABOVE + REAL_SWITCH_HEIGHT_BELOW)
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(LENGTH + REAL_SWITCH_WIDTH / 2, -REAL_SWITCH_HEIGHT_ABOVE - LABEL_DIST)

	override fun modelExchanged(oldModel: AnalogSwitch?) {
		super.modelExchanged(oldModel)
		analogElement.bind(model)

		addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + REAL_SWITCH_WIDTH, 0, Direction.EAST))
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		drawTwoPortRealSwitchShape(context)
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == AbstractAnalogVertice.REQUEST_REANALYZE) {
			if (event.signalHandler != null && parent is AnalogGraphView) {
				(parent as AnalogGraphView).recalculate(event.signalHandler!!, true)
			}
		}
	}

	override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
		customColor?.color ?: super<AbstractSwitchView>.getEditPortViewColor(styleProvider)

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "analogSwitch:${model.id}"

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<AnalogSwitch> =
		AnalogPushButtonSwitchView(styleProvider, model)
}