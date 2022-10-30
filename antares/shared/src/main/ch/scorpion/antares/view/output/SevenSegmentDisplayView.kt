package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.SevenSegmentDisplay
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A view of a [SevenSegmentDisplay].
 */
class SevenSegmentDisplayView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: SevenSegmentDisplay = SevenSegmentDisplay(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSegmentDisplayView<SevenSegmentDisplay>(styleProvider, model, lightColor, eventBus),
	ControlView<SevenSegmentDisplay> {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.SevenSegmentDisplayView.iconPath"
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
					x = geom.scaledFactor * i,
					y = 0))
			}
			for (i in 5..8) {
				addPortView(DigitalPortView(
					styleProvider = styleProvider,
					port = model.getInput(i),
					direction = Direction.SOUTH,
					portLabelPosition = PortLabelPosition.EXTERNAL,
					x = geom.scaledFactor * (i - 4),
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
		val clone = SevenSegmentDisplayView(styleProvider, model, lightColor)
		clone.isShowPortViews = false
		clone.location = Point2D(0, 0)
		return clone
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		drawFullHorizontalSegment(context, model.inputValueOf("a"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)

		drawB(context)
		drawC(context)

		drawFullHorizontalSegment(context, model.inputValueOf("d"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			7 * geom.scaledFactor + geom.segHalfWidth)

		drawE(context)
		drawF(context)

		drawFullHorizontalSegment(context, model.inputValueOf("g"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)

		if (model.inactive && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = Look.inactiveColor
			context.g.fillRect(0, 0, geom.width, geom.height)
		}
	}
}