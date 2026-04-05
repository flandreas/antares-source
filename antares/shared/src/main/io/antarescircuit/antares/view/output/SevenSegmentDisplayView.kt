package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.output.SevenSegmentDisplay
import io.antarescircuit.antares.model.output.SevenSegmentDisplayScheme
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A view of a [SevenSegmentDisplay].
 */
class SevenSegmentDisplayView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: SevenSegmentDisplay = SevenSegmentDisplay(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	size: Size = DEFAULT_SIZE,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSegmentDisplayView<SevenSegmentDisplay>(styleProvider, model, lightColor, size, false, eventBus),
	ControlView<SevenSegmentDisplay> {

	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.output.SevenSegmentDisplayView.iconPath"
	}

	override fun modelExchanged(oldModel: SevenSegmentDisplay?) {
		super.modelExchanged(oldModel)

		if (model.portScheme == SevenSegmentDisplayScheme.COMBINED) {
			createCombinedPortViews()
		} else {
			for (i in 1..4) {
				addPortView(DigitalPortView(
					styleProvider = styleProvider,
					port = model.getInput(i),
					direction = Direction.NORTH,
					portLabelPosition = PortLabelPosition.EXTERNAL,
					x = (geom.snappedScaledFactor * i).toInt(),
					y = 0))
			}
			for (i in 5..8) {
				addPortView(DigitalPortView(
					styleProvider = styleProvider,
					port = model.getInput(i),
					direction = Direction.SOUTH,
					portLabelPosition = PortLabelPosition.EXTERNAL,
					x = (geom.snappedScaledFactor * (i - 4)).toInt(),
					y = geom.height))
			}
		}
	}

	override fun handleSizeChanged() {
		if (size != Size.LARGE) {
			// Medium and small display can only have combined connection scheme (no enough space for more ports)
			portScheme = SevenSegmentDisplayScheme.COMBINED
		}
	}

	/** ---- UI properties */

	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var portScheme: SevenSegmentDisplayScheme
		get() = model.portScheme
		set(value) {
			if (value != portScheme) {
				invalidate()
				model.portScheme = value
				modelExchanged(model)
				invalidate()
				validate()
			}
		}

	/** ---- [ControlView] */

	override val controlId: String get() = "7seg:" + model.id

	override fun sourcePropertiesChanged(source: ControlViewSource<SevenSegmentDisplay>) {
		if (source is SevenSegmentDisplayView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun writeModelProperties(writer: StoreWriter) {
		super.writeModelProperties(writer)
		writer.writeString("portScheme", portScheme.customName)
	}

	override fun readModelProperties(reader: StoreReader) {
		super.readModelProperties(reader)
		if (reader.hasAttribute("portScheme")) {
			portScheme = SevenSegmentDisplayScheme.withName(reader.readString("portScheme"))
		}
	}

	/** ---- [ControlViewSource] */

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<SevenSegmentDisplay> {
		val clone = SevenSegmentDisplayView(styleProvider, model, lightColor, size)
		clone.isShowPortViews = false
		clone.location = Point2D(0, 0)
		return clone
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		drawFullHorizontalSegment(context, model.inputValueOf("a"),
			0.5f * geom.snappedScaledFactor + geom.segHalfWidth,
			geom.snappedScaledFactor + geom.segHalfWidth)

		drawB(context)
		drawC(context)

		drawFullHorizontalSegment(context, model.inputValueOf("d"),
			0.5f * geom.snappedScaledFactor + geom.segHalfWidth,
			7 * geom.snappedScaledFactor + geom.segHalfWidth)

		drawE(context)
		drawF(context)

		drawFullHorizontalSegment(context, model.inputValueOf("g"),
			0.5f * geom.snappedScaledFactor + geom.segHalfWidth,
			4 * geom.snappedScaledFactor + geom.segHalfWidth)

		if (model.inactive && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = Look.inactiveColor
			context.g.fillRect(0, 0, geom.width, geom.height)
		}
	}
}