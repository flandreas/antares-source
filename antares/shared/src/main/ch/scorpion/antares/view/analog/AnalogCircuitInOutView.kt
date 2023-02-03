package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogCircuitInOut
import ch.scorpion.antares.view.inout.AbstractCircuitInOutView
import ch.scorpion.antares.view.inout.ArrowPath
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.port.PortView

class AnalogCircuitInOutView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogCircuitInOut = AnalogCircuitInOut(),
	eventBus: EventBus = BaseModule.eventBus,
	orientation: Direction = Direction.EAST
) : AbstractCircuitInOutView<AnalogCircuitInOut>(styleProvider, model, eventBus, orientation) {

	init {
		isFocusable = true
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: AnalogCircuitInOut?) {
		super.modelExchanged(oldModel)
		updateView()
	}

	/** ---- [GraphPortView] */

	override val iconPath: String get() = BaseModule.properties.getString(DigitalCircuitInOutView.PROP_INOUT_ICON_PATH)

	/** ---- [AbstractCircuitInOutView] */

	override fun updateViewImpl() {
		arrowPath = ArrowPath.Companion.Builder(
			orientation,
			Dimension2D(label.bounds.width, label.bounds.height)
		).build(inout = true)
	}

	override fun createPortView(template: PortView<*>?): PortView<*> {
		val portView = AnalogPortView(
			styleProvider,
			model.getPort(),
			direction = orientation.opposite(),
			length = template?.length,
			customUnconnectedLength = template?.customUnconnectedLength
		)
		portView.setLocation(portView.unconnectedLength * orientation.dx, portView.unconnectedLength * orientation.dy)
		return portView
	}

	override fun updateOutputLabel() {
		label.text = StringUtils.orEmpty(name)
		label.location = orientation.multiply(LABEL_DIST.toDouble())
		label.alignment = Alignment.forOrientation(orientation.opposite())
		updateBoundingBox()
	}

	override fun drawSimulated(context: DrawContext) {
		// TODO
	}
}