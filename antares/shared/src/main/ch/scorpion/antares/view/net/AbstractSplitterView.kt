package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.AbstractSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

/**
 * Common base class for [SplitterView] and [ConcentratorView].
 */
abstract class AbstractSplitterView<T : AbstractSplitter>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AbstractSplitter
) : DigitalComponentView<AbstractSplitter>(styleProvider, model) {

	companion object {
		const val WIDTH = 2 * Look.GRID
		const val PORT_INSET = Look.SCALE
		const val PORT_DISTANCE = 2 * Look.SCALE
		private const val DIR_PATH_WIDTH = Look.SCALE
		private const val DIR_PATH_HEIGHT_HALF = Look.SCALE / 2

		private val DIR_PATH = System.createPath()
			.moveTo(0,0)
			.lineTo(-DIR_PATH_WIDTH, -DIR_PATH_HEIGHT_HALF)
			.lineTo(-DIR_PATH_WIDTH, DIR_PATH_HEIGHT_HALF)
			.close()

	}

	var handedness: Handedness = Handedness.RIGHT
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				modelExchanged(model)
				invalidate()
				update()
			}
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				invalidate()
				model.bitWidth = value
				modelExchanged(model)
				invalidate()
				update()
			}
		}

	var branchCount: BranchCount
		get() = model.branchCount
		set(value) {
			if (value != branchCount) {
				invalidate()
				model.branchCount = value
				modelExchanged(model)
				invalidate()
				update()
			}
		}

	var signalRepresentation: DigitalSignalRepresentation
		get() = model.signalRepresentation
		set(value) {
			model.signalRepresentation = value
		}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: AbstractSplitter?) {
		super.modelExchanged(oldModel)

		val height = 2 * PORT_INSET + PORT_DISTANCE * (model.narrowSidePorts.count() - 1)

		// Wide side
		val wideSidePortView = createWideSidePortView(height)
		addPortView(wideSidePortView)

		setBounds(createBodyBounds(height))

		val dy = if (handedness == Handedness.RIGHT) -PORT_DISTANCE else +PORT_DISTANCE
		var y = if (handedness == Handedness.RIGHT) height / 2 - PORT_INSET else -height / 2 + PORT_INSET

		for (narrowSidePort in model.narrowSidePorts) {
			val portView = createNarrowSidePortView(narrowSidePort, y)
			portView.showBitWidthAnnotation = false
			addPortView(portView)
			y += dy
		}
	}

	/** ---- [AbstractSplitterView] */

	protected abstract fun createWideSidePortView(height: Int): DigitalPortView

	protected abstract fun createNarrowSidePortView(port: DigitalPort, y: Int): DigitalPortView

	protected abstract fun createBodyBounds(height: Int): RectangularShape

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("handedness", handedness.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("handedness")) {
			handedness = Handedness.withName(reader.readString("handedness"))
		}
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, width.toInt(), height.toInt())
			}
		}
		super.drawImpl(context)
		if (context.useContextColors) {
			drawImpl(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
		} else {
			drawImpl(context, foregroundColor, propertiesBackgroundColor)
		}
	}

	private fun drawImpl(context: DrawContext, lineColor: Color, fillColor: Color?) {
		val oldColor = context.g.color
		context.g.stroke = stroke
		if (fillColor != null) {
			context.g.color = fillColor
		}
		context.g.fillRect(xInt, yInt, width.toInt(), height.toInt())
		context.g.color = lineColor
		context.g.drawRect(xInt, yInt, width.toInt(), height.toInt())

		drawDirectionAnnotation(context)

		context.g.color = oldColor
	}

	private fun drawDirectionAnnotation(context: DrawContext) {
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		context.g.translate(bounds.minX + 0.75 * bounds.width, 0.0)
		context.g.draw(DIR_PATH)
		context.g.translate(-(bounds.minX + 0.75 * bounds.width), 0.0)
	}
}