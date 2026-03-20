package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.AbstractBranchCountSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.DigitalSignalRepresenter
import ch.scorpion.jabbah.graph.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.antares.view.PortViewSpacing
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Point2D
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
abstract class AbstractBranchCountSplitterView<T : AbstractBranchCountSplitter>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	handedness: Handedness = Handedness.RIGHT
) : OrientableRectangularVerticeView<T>(styleProvider, model), DigitalSignalRepresenter {

	companion object {
		const val WIDTH = 2 * Look.GRID
		const val PORT_INSET = Look.SCALE
		const val DIR_PATH_WIDTH = Look.SCALE
		const val DIR_PATH_HEIGHT_HALF = Look.SCALE / 2

		private val DIR_PATH = System.createPath()
			.moveTo(0,0)
			.lineTo(-DIR_PATH_WIDTH, -DIR_PATH_HEIGHT_HALF)
			.lineTo(-DIR_PATH_WIDTH, DIR_PATH_HEIGHT_HALF)
			.close()
	}

	var handedness: Handedness = handedness
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
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

	override var signalRepresentation: DigitalSignalRepresentation
		get() = model.signalRepresentation
		set(value) {
			model.signalRepresentation = value
		}

	var portViewSpacing: PortViewSpacing = PortViewSpacing.Narrow
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
				invalidate()
				update()
			}
		}


	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)

		val height = 2 * PORT_INSET + portViewSpacing.value * (model.narrowSidePorts.count() - 1)

		// Wide side
		val wideSidePortView = createWideSidePortView(height)
		addPortView(wideSidePortView)

		setBounds(createBodyBounds(height))

		val dy = if (handedness == Handedness.RIGHT) -portViewSpacing.value else +portViewSpacing.value
		var y = if (handedness == Handedness.RIGHT) height / 2 - PORT_INSET else -height / 2 + PORT_INSET

		for (narrowSidePort in model.narrowSidePorts) {
			val portView = createNarrowSidePortView(narrowSidePort, y)
			portView.showBitWidthAnnotation = false
			addPortView(portView)
			y += dy
		}
	}

	private fun updateGeometry() {
		val height = 2 * PORT_INSET + portViewSpacing.value * (model.narrowSidePorts.count() - 1)
		setBounds(createBodyBounds(height))

		getPortView(model.getPort(1))!!.location = Point2D(wideSidePortViewX, 0)

		val dy = if (handedness == Handedness.RIGHT) -portViewSpacing.value else +portViewSpacing.value
		var y = if (handedness == Handedness.RIGHT) height / 2 - PORT_INSET else -height / 2 + PORT_INSET

		for (narrowSidePort in model.narrowSidePorts) {
			val portView = getPortView(narrowSidePort)
			portView!!.location = Point2D(narrowSidePortViewX, y)
			y += dy
		}
	}

	/** ---- [AbstractBranchCountSplitterView] */

	protected abstract val wideSidePortViewX: Int
	protected abstract val narrowSidePortViewX: Int

	protected abstract fun createWideSidePortView(height: Int): DigitalPortView

	protected abstract fun createNarrowSidePortView(port: DigitalPort, y: Int): DigitalPortView

	protected abstract fun createBodyBounds(height: Int): RectangularShape

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("handedness", handedness.customName)
		if (portViewSpacing != PortViewSpacing.Narrow) {
			writer.writeString("portViewSpacing", portViewSpacing.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("handedness")) {
			handedness = Handedness.withName(reader.readString("handedness"))
		}
		if (reader.hasAttribute("portViewSpacing")) {
			portViewSpacing = PortViewSpacing.withName(reader.readString("portViewSpacing"))
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
			context.g.color = transparent.applyTo(fillColor)
		}
		context.g.fillRect(xInt, yInt, width.toInt(), height.toInt())
		context.g.color = transparent.applyTo(lineColor)
		context.g.drawRect(xInt, yInt, width.toInt(), height.toInt())

		drawDirectionAnnotation(context)

		context.g.color = oldColor
	}

	protected open fun drawDirectionAnnotation(context: DrawContext) {
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		context.translated(bounds.minX + 0.75 * bounds.width, 0.0) {
			it.g.draw(DIR_PATH)
		}
	}
}