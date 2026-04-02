package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.analog.AnalogSwitchView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.ceil
import kotlin.math.max

/**
 * An [AbstractSwitchView] implementation that draws a rectangular push button area.
 * Can be used as view of digital [SwitchView]s as well as [ControlView] of [AnalogSwitchView].
 */
abstract class AbstractPushButtonSwitchView<T: AbstractSwitch<T>>(
	override var styleProvider: StyleProvider,
	model: T
) : AbstractSwitchView<T>(styleProvider, model, internalLabelText = " "),
	Labeled,
	ControlView<T>
{
	companion object {
		private const val SIZE = 4 * SCALE
		private const val BORDER_WIDTH = 3
		private const val LABEL_INSET = 4.0
		//private const val LABEL_DIST = SCALE
	}

	private val face = AnalogFace()

	// Individually controlled by ControlView
	var labelPosition: VerticeLabelPosition = VerticeLabelPosition.EXTERNAL
		set(value) {
			invalidate()
			field = value
			setBounds(calculateBounds())
			//updateLabelGeometries()
			updateGeometry()
			invalidate()
			update()
			validate()
		}

	init {
		initExternalLabel(Direction.WEST)
		isFocusable = true
		modelExchanged(null)
		setBounds(calculateBounds())
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(-(SIZE + AbstractAntaresPortView.LENGTH + LABEL_DIST), 0)

	override fun setInternalLabelLocation(location: Point2D) {
		// Ignore location and apply custom positioning
		updateInternalLabelLocation()
	}

	private fun updateInternalLabelLocation() {
		internalLabel?.location = bounds.center
	}

	override fun updateInternalLabel() {
		if (labelPosition == VerticeLabelPosition.INTERNAL) {
			internalLabel?.text = StringUtils.orEmpty(name)
			setBounds(calculateBounds())
			updateGeometry()
		}
	}

	override fun updateGeometry() {
		super.updateGeometry()
		updateInternalLabelLocation()
	}

	/** ---- [Labeled] interface */

	override val label: Label get() = when (labelPosition) {
		VerticeLabelPosition.HIDE -> internalLabel!!
		VerticeLabelPosition.INTERNAL -> internalLabel!!
		VerticeLabelPosition.EXTERNAL -> externalLabel.label
	}

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	override val controlName: String
		get() = ControlViewSource.getControlName(type, id, model.name)

	override val mirrorWidth: Double get() = -(2 * AbstractAntaresPortView.LENGTH + width)

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as T
	}

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
	}

	override fun readModelProperties(reader: StoreReader) {
		if (reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<T>) {
		if (source is AbstractPushButtonSwitchView<*>) {
			copyControlViewProperties(source, this)
		}
	}

	protected open fun copyControlViewProperties(source: AbstractPushButtonSwitchView<*>, dest: AbstractPushButtonSwitchView<*>) {
		dest.name = source.name
	}

	/** ---- [AbstractDrawable] */

	override val boundingBox: RectangularShape
		get() {
			var bb = super.boundingBox
			if (StringUtils.isNotEmpty(externalLabel.text)) {
				val lbb = Rectangle2D(externalLabel.boundingBox).moveBy(location)
				bb = Rectangle2D(bb).add(lbb)
			}
			return bb
		}

	override fun draw(context: DrawContext) {
		super.draw(context)
		if (labelPosition != VerticeLabelPosition.INTERNAL) {
			context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		} else {
			context.g.color = if (model.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor
		}
		if (labelPosition == VerticeLabelPosition.EXTERNAL) {
			externalLabel.draw(context)
		}
	}

	override fun drawImpl(context: DrawContext) {
		val appContext = context.castedAppContext<GraphApplicationContext>()!!

		val oldColor = context.g.color
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}

		if (appContext.isExecute) {
			face.drawExecuted(context)
		} else {
			face.drawEdited(context)
		}

		drawBorder(context)

		if (appContext.isExecute) {
			drawFocus(context)

			if (model.shouldDrawDisabled(appContext)) {
				drawDisabled(context)
			}
		}
		context.g.color = oldColor
	}

	override fun drawSelected(context: DrawContext) {
		context.g.color = context.color!!.foregroundColor
		draw(context) {
			super.drawImpl(it)
			face.drawSelected(context)
			drawInternalLabel(context)
		}
		drawExternalLabel(context)
	}

	override fun drawInternalLabel(context: DrawContext, text: String?) {
		if (labelPosition == VerticeLabelPosition.INTERNAL) {
			internalLabel!!.text = StringUtils.orEmpty(model.name)
			super.drawInternalLabel(context, text)
		}
	}

	override fun drawExternalLabel(context: DrawContext) {
		if (labelPosition == VerticeLabelPosition.EXTERNAL) {
			super.drawExternalLabel(context)
		}
	}

	private fun drawBorder(context: DrawContext) {
		context.g.color = context.chooseForeground(transparent.applyTo(foregroundColor))
		context.g.stroke = stroke
		context.g.drawRect(xInt, yInt, widthInt, heightInt)
	}

	private fun drawDisabled(context: DrawContext) {
		context.g.color = Look.disabledColor()
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
	}

	/** ---- [Component] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw UnsupportedOperationException()
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (labelPosition != VerticeLabelPosition.EXTERNAL) {
			writer.writeString("labelPos", labelPosition.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("labelPos")) {
			labelPosition = VerticeLabelPosition.withName(reader.readString("labelPos"))
		}
	}

	/** ---- [AbstractSwitchView] */

	override fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.drawRect(xInt + BORDER_WIDTH - 1, yInt + BORDER_WIDTH - 1,
				widthInt - 2 * BORDER_WIDTH + 2, heightInt - 2 * BORDER_WIDTH + 2)
		}
	}

	/** ---- [AbstractPushButtonSwitchView] */

	/**
	 * Calculates the bounds of this [SwitchView] depending on the [labelPosition] and the
	 * current externalLabel text
	 */
	private fun calculateBounds(): RectangularShape {
		val width = calculateWidth()
		return Rectangle2D(-AbstractAntaresPortView.LENGTH - width, -SIZE / 2, width, SIZE)
	}

	/**
	 * Calculates the width of this [AbstractPushButtonSwitchView] depending on the current externalLabel and
	 * the [VerticeLabelPosition]. If [labelPosition] is [VerticeLabelPosition.INTERNAL],
	 * the width is calculated as the smallest integer multiple of [SIZE] that contains the
	 * externalLabel when drawn with the current font.
	 */
	private fun calculateWidth(): Int {
		if (labelPosition != VerticeLabelPosition.INTERNAL || StringUtils.isEmpty(model.name)) {
			return SIZE
		}
		val tri = TextRenderInfoFactory.measureSingleLineText(model.name!!, font)
		val requiredSpace = tri.textBounds.width + 2 * LABEL_INSET
		return (SIZE * max(1.0, ceil(requiredSpace / SIZE))).toInt()
	}

	/**
	 * A button face that draws an open or closed analog switch symbol, or the switch's label
	 * if [labelPosition] is [VerticeLabelPosition.INTERNAL].
	 */
	private inner class AnalogFace {

		fun drawSelected(context: DrawContext) {
			drawOuterRectangle(context)
			drawContent(context)
		}

		fun drawEdited(context: DrawContext) {
			drawBackground(context)
			context.g.color = context.chooseForeground(transparent.applyTo(foregroundColor))
			drawContent(context)
		}

		fun drawExecuted(context: DrawContext) {
			drawSignalBackground(context)
			context.g.color = transparent.applyTo(if (model.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor)
			drawContent(context)
		}

		private fun drawInnerRectangle(context: DrawContext) {
			context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
			context.g.drawRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
				width.toInt() - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
		}

		private fun drawOuterRectangle(context: DrawContext) {
			context.g.stroke = stroke
			context.g.drawRect(xInt, yInt, width.toInt(), SIZE)
		}

		private fun drawBackground(context: DrawContext) {
			context.g.color = context.chooseBackground(transparent.applyTo(propertiesBackgroundColor))
			context.g.fillRect(xInt, yInt, widthInt, heightInt)
		}

		private fun drawSignalBackground(context: DrawContext) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().background.color.backgroundColor)
			context.g.fillRect(xInt, yInt, widthInt, heightInt)
			context.g.color = transparent.applyTo(Bit.of(model.isOn).color.foregroundColor)
			context.g.fillRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
				widthInt - 2 * BORDER_WIDTH, heightInt - 2 * BORDER_WIDTH)
		}


		private fun drawContent(context: DrawContext) {
			if (labelPosition == VerticeLabelPosition.INTERNAL) {
				drawInternalLabel(context)
				drawInnerRectangle(context)
			} else {
				drawSymbol(context)
			}
		}

		private fun drawSymbol(context: DrawContext) {
			context.g.translate(-AbstractAntaresPortView.LENGTH - SIZE / 2.0, 0.0)
			context.g.rotate(rotation.inverse().angle)
			context.g.translate(AbstractAntaresPortView.LENGTH + SIZE / 2.0, 0.0)

			drawCircles(context)
			context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
			if (toggle) {
				drawToggleSymbol(context)
			} else {
				drawPushButtonSymbol(context)
			}

			context.g.translate(-AbstractAntaresPortView.LENGTH - SIZE / 2.0, 0.0)
			context.g.rotate(rotation.angle)
			context.g.translate(AbstractAntaresPortView.LENGTH + SIZE / 2.0, 0.0)
		}

		private fun drawCircles(context: DrawContext) {
			context.g.fillCircle(-AbstractAntaresPortView.LENGTH - 3.0 * SCALE, 0.5 * SCALE, 2.0)
			context.g.fillCircle(-AbstractAntaresPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE, 2.0)
		}

		private fun drawPushButtonSymbol(context: DrawContext) {
			if (model.isOn) {
				context.g.drawLine(
					-AbstractAntaresPortView.LENGTH - 3.0 * SCALE, 0.5 * SCALE,
					-AbstractAntaresPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE
				)
				context.g.drawLine(
					-AbstractAntaresPortView.LENGTH - 2.0 * SCALE, 0.5 * SCALE,
					-AbstractAntaresPortView.LENGTH - 2.0 * SCALE, -0.0 * SCALE
				)
			} else {
				context.g.drawLine(
					-AbstractAntaresPortView.LENGTH - 3.0 * SCALE, -0.25 * SCALE,
					-AbstractAntaresPortView.LENGTH - 1.0 * SCALE, -0.25 * SCALE
				)
				context.g.drawLine(
					-AbstractAntaresPortView.LENGTH - 2.0 * SCALE, -0.25 * SCALE,
					-AbstractAntaresPortView.LENGTH - 2.0 * SCALE, -0.75 * SCALE
				)
			}
		}

		private fun drawToggleSymbol(context: DrawContext) {
			if (model.isOn) {
				context.g.drawLine(
					-AbstractAntaresPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE,
					-AbstractAntaresPortView.LENGTH - 3.0 * SCALE, 0.5 * SCALE
				)
			} else {
				context.g.drawLine(
					-AbstractAntaresPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE,
					-AbstractAntaresPortView.LENGTH - 2.0 * SCALE, -1.0 * SCALE
				)
			}
		}
	}
}