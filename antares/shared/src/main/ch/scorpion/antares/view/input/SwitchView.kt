package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.ceil
import kotlin.math.max

/** Defines the visual, exchangeable appearance of a [SwitchView]. */
private interface SwitchViewFace {

	fun drawSelected(context: DrawContext)

	fun drawEdited(context: DrawContext)

	fun drawExecuted(context: DrawContext)
}

/**
 * A view representation of a [Switch] that supports persistent toggling between two states.
 */
class SwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Switch = Switch(),
	private val eventBus: EventBus = BaseModule.eventBus
) : DigitalComponentView<Switch>(styleProvider, model), ControlView<Switch>, ControlViewSource<Switch> {

	companion object {
		private const val TOGGLE_BASE_RESOURCE_KEY = "library.element.Toggle"
		private val TOGGLE_TYPE = Translations.getString("$TOGGLE_BASE_RESOURCE_KEY.name")
		private val TOGGLE_TYPE_DESC = Translations.getOptionalString("$TOGGLE_BASE_RESOURCE_KEY.desc")

		private const val SWITCH_BASE_RESOURCE_KEY = "library.element.Switch"
		private val SWITCH_TYPE = Translations.getString("$SWITCH_BASE_RESOURCE_KEY.name")
		private val SWITCH_TYPE_DESC = Translations.getOptionalString("$SWITCH_BASE_RESOURCE_KEY.desc")

		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.SwitchView.iconPath"
		private const val SIZE = 4 * SCALE
		private const val BORDER_WIDTH = 3
		private const val LABEL_DIST = SCALE
		private const val LABEL_INSET = 4.0
	}

	var labelPosition: VerticeLabelPosition = VerticeLabelPosition.EXTERNAL
		set(value) {
			invalidate()
			field = value
			setBounds(calculateBounds())
			updateLabelGeometries()
			invalidate()
			update()
			validate()
			postControlViewSourceChangeEvent(eventBus)
		}

	private val face = AnalogFace()

	/** Handles mouse interactions during execution*/
	private val actorInteractionHandler = InteractionHandler()

	/**
	 * The [Label] that displays the signal for [VerticeLabelPosition.EXTERNAL], or the name of this [SwitchView]
	 * for [VerticeLabelPosition.INTERNAL].
	 */
	private val internalLabel: Label = Label(
		font = font,
		text = "",
		location = Point2D(DigitalPortView.LENGTH - SIZE / 2.0, 0.0),
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER,
		rotationDisplayStrategy = Label.RotationDisplayStrategy.KEEP_HORIZONTAL)

	private val externalLabel = HorizontalLabel(
		owner = this,
		relLocation = Point2D(-(SIZE + DigitalPortView.LENGTH + LABEL_DIST), 0),
		orientation = Direction.WEST,
		font = font)

	init {
		isFocusable = true
		modelExchanged(null)
		setBounds(calculateBounds())
	}

	/**
	 * Calculates the bounds of this [SwitchView] depending on the [labelPosition] and the
	 * current externalLabel text
	 */
	private fun calculateBounds(): RectangularShape {
		val width = calculateWidth()
		return Rectangle2D(-DigitalPortView.LENGTH - width, -SIZE / 2, width, SIZE)
	}

	private fun updateLabelGeometries() {
		internalLabel.location = Point2D(bounds.centerX, bounds.centerY)
		if (labelPosition == VerticeLabelPosition.INTERNAL) {
			internalLabel.rotationDisplayStrategy = Label.RotationDisplayStrategy.ROTATE_HALF
		} else {
			internalLabel.rotationDisplayStrategy = Label.RotationDisplayStrategy.KEEP_HORIZONTAL
		}
	}

	override fun modelExchanged(oldModel: Switch?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST)
		portView.setLocation(-portView.length.toDouble(), 0.0)
		addPortView(portView)
		updateLabels()
	}

	/** ---- UI properties */

	var name: String?
		get() = model.name
		set(value) {
			if (value != model.name) {
				model.name = value
				updateLabels()
				validate()
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	/**
	 * Controls the interactive behaviour of this [SwitchView]. If set to `true`, the [Switch]
	 * stays in the new state when the user releases the mouse button. If set to `false`,
	 * the [Switch] returns to 0 state.
	 */
	var toggle: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (labelPosition != VerticeLabelPosition.EXTERNAL) {
			writer.writeString("labelPos", labelPosition.customName)
		}
		if (!toggle) {
			writer.writeBoolean("toggle", toggle)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("labelPos")) {
			labelPosition = VerticeLabelPosition.withName(reader.readString("labelPos"))
		}
		if (reader.hasAttribute("toggle")) {
			toggle = reader.readBoolean("toggle")
		}
	}

	/** ---- [ActorView] interface */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [Component] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw UnsupportedOperationException()
		}

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		internalLabel.ownerRotation = rotation
		updateLabels()
	}

	/** ---- [AbstractDrawable] */

	override val boundingBox: Rectangle2D
		get() {
			val bb = super.boundingBox
			if (StringUtils.isNotEmpty(externalLabel.text)) {
				val lbb = externalLabel.boundingBox.moveBy(location)
				bb.add(lbb)
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
		val oldColor = context.g.color
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			face.drawExecuted(context)
		} else {
			face.drawEdited(context)
		}

		drawBorder(context)

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawFocus(context)

			if (isDisabledFor(context) || model.inactive) {
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
			if (labelPosition == VerticeLabelPosition.INTERNAL) {
				internalLabel.draw(context)
			}
		}
		if (labelPosition == VerticeLabelPosition.EXTERNAL) {
			externalLabel.draw(context)
		}
	}

	private fun drawInnerRectangle(context: DrawContext) {
		context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
		context.g.drawRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
			width.toInt() - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH/*, DIAMETER, DIAMETER*/)
	}

	private fun drawOuterRectangle(context: DrawContext) {
		context.g.stroke = stroke
		context.g.drawRect(xInt, yInt, width.toInt(), SIZE)
	}

	/** ---- [ControlViewSource] */

	override val controlId: String
		get() {
			// Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
			// when ControlViews (event as part of a wrapping Component) are added to a Drawing
			return "switch:" + model.id
		}

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<Switch> {
		val clone = SwitchView(styleProvider, model)
		clone.isShowPortViews = false
		clone.location = Point2D.ZERO
		copyControlViewProperties(this, clone)
		return clone
	}


	/** ---- [ControlView] */

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: Switch) {
		this.model = model
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

	override fun sourcePropertiesChanged(source: ControlViewSource<Switch>) {
		if (source is SwitchView) {
			copyControlViewProperties(source, this)
		}
	}

	private fun copyControlViewProperties(source: SwitchView, dest: SwitchView) {
		dest.name = source.name
		dest.labelPosition = source.labelPosition
		dest.toggle = source.toggle
	}

	/** ---- [AbstractVerticeView] */

	override val type: String
		get() = if (toggle) {
			TOGGLE_TYPE
		} else {
			SWITCH_TYPE
		}

	override val typeDesc: String?
		get() = if (toggle) {
			TOGGLE_TYPE_DESC
		} else {
			SWITCH_TYPE_DESC
		}

	/** ---- [SwitchView] */

	private fun drawBorder(context: DrawContext) {
		context.g.color = transparent.applyTo(color.foregroundColor)
		context.g.stroke = stroke
		context.g.drawRect(xInt, yInt, widthInt, heightInt)
	}

	private fun drawBackground(context: DrawContext) {
		context.g.color = transparent.applyTo(propertiesBackgroundColor)
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
	}

	private fun drawSignalBackground(context: DrawContext) {
		context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().background.color.backgroundColor)
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
		context.g.color = transparent.applyTo(Bit.of(model.isOn).color.foregroundColor)
		context.g.fillRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
			widthInt - 2 * BORDER_WIDTH, heightInt - 2 * BORDER_WIDTH)
	}

	private fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.drawRect(xInt + BORDER_WIDTH - 1, yInt + BORDER_WIDTH - 1,
				widthInt - 2 * BORDER_WIDTH + 2, heightInt - 2 * BORDER_WIDTH + 2)
		}
	}

	private fun isDisabledFor(context: DrawContext): Boolean =
		model.disabled && context.castedAppContext<GraphApplicationContext>()?.isPausing == true

	private fun drawDisabled(context: DrawContext) {
		context.g.color = Look.disabledColor()
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
	}

	private fun updateLabels() {
		invalidate()
		if (labelPosition == VerticeLabelPosition.INTERNAL) {
			internalLabel.text = StringUtils.orEmpty(name)
		} else {
			externalLabel.text = StringUtils.orEmpty(name)
			externalLabel.rotationChanged()
		}
		setBounds(calculateBounds())
		updateLabelGeometries()
		invalidate()
		update()
	}

	/**
	 * Calculates the width of this [SwitchView] depending on the current externalLabel and
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

	/** A [SwitchViewFace] that draws the hexadecimal value of the current state bit. */
	@Suppress("unused")
	private inner class DigitalFace : SwitchViewFace {

		override fun drawSelected(context: DrawContext) {
			drawOuterRectangle(context)
			drawInnerRectangle(context)
		}

		override fun drawEdited(context: DrawContext) {
			drawSignalBackground(context)
			drawContent(context)
		}

		override fun drawExecuted(context: DrawContext) {
			drawSignalBackground(context)
			drawContent(context)
		}

		private fun drawContent(context: DrawContext) {
			drawSignalBackground(context)

			internalLabel.color = transparent.applyTo(if (model.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor)
			if (labelPosition == VerticeLabelPosition.INTERNAL) {
				internalLabel.text = StringUtils.orEmpty(model.name)
			} else {
				internalLabel.text = Bit.of(model.isOn).toHexString()
			}
			internalLabel.draw(context)
		}
	}

	/** A [SwitchViewFace] that draws an open or closed analog switch symbol. */
	private inner class AnalogFace : SwitchViewFace {

		override fun drawSelected(context: DrawContext) {
			drawOuterRectangle(context)
			drawContent(context)
		}

		override fun drawEdited(context: DrawContext) {
			drawBackground(context)
			context.g.color = transparent.applyTo(color.foregroundColor)
			drawContent(context)
		}

		override fun drawExecuted(context: DrawContext) {
			drawSignalBackground(context)
			context.g.color = transparent.applyTo(if (model.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor)
			drawContent(context)
		}

		private fun drawContent(context: DrawContext) {
			if (labelPosition == VerticeLabelPosition.INTERNAL) {
				internalLabel.text = StringUtils.orEmpty(model.name)
				internalLabel.draw(context)
				drawInnerRectangle(context)
			} else {
				drawSymbol(context)
			}
		}

		private fun drawSymbol(context: DrawContext) {
			context.g.translate(-DigitalPortView.LENGTH - SIZE / 2.0, 0.0)
			context.g.rotate(rotation.inverse().angle)
			context.g.translate(DigitalPortView.LENGTH + SIZE / 2.0, 0.0)

			drawCircles(context)
			context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
			if (toggle) {
				drawToggleSymbol(context)
			} else {
				drawPushButtonSymbol(context)
			}

			context.g.translate(-DigitalPortView.LENGTH - SIZE / 2.0, 0.0)
			context.g.rotate(rotation.angle)
			context.g.translate(DigitalPortView.LENGTH + SIZE / 2.0, 0.0)
		}

		private fun drawCircles(context: DrawContext) {
			context.g.fillCircle(-DigitalPortView.LENGTH - 3.0 * SCALE, 0.5 * SCALE, 2.0)
			context.g.fillCircle(-DigitalPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE, 2.0)
		}

		private fun drawPushButtonSymbol(context: DrawContext) {
			if (model.isOn) {
				context.g.drawLine(
					-DigitalPortView.LENGTH - 3.0 * SCALE, 0.5 * SCALE,
					-DigitalPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE)
				context.g.drawLine(
					-DigitalPortView.LENGTH - 2.0 * SCALE, 0.5 * SCALE,
					-DigitalPortView.LENGTH - 2.0 * SCALE, -0.0 * SCALE)
			} else {
				context.g.drawLine(
					-DigitalPortView.LENGTH - 3.0 * SCALE, -0.25 * SCALE,
					-DigitalPortView.LENGTH - 1.0 * SCALE, -0.25 * SCALE)
				context.g.drawLine(
					-DigitalPortView.LENGTH - 2.0 * SCALE, -0.25 * SCALE,
					-DigitalPortView.LENGTH - 2.0 * SCALE, -0.75 * SCALE)
			}
		}

		private fun drawToggleSymbol(context: DrawContext) {
			if (model.isOn) {
				context.g.drawLine(
					-DigitalPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE,
					-DigitalPortView.LENGTH - 3.0 * SCALE, 0.5 * SCALE)
			} else {
				context.g.drawLine(
					-DigitalPortView.LENGTH - 1.0 * SCALE, 0.5 * SCALE,
					-DigitalPortView.LENGTH - 2.0 * SCALE, -1.0 * SCALE)
			}
		}
	}

	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		private var keyDown = false

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			model.toggle(context.signalHandler)
			context.mouseEvent?.consume()
			requestFocus()
			return this
		}

		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler {
			return this
		}

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			if (!toggle) {
				if (model.isOn) {
					model.off(context.signalHandler)
					context.mouseEvent?.consume()
				}
			}
			return null
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			context.mouseEvent?.consume()
			return this
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!keyDown) {
				name?.let {
					if (it.length == 1 && it[0].toInt() == context.keyEvent?.key) {
						toggle(context)
						keyDown = true
						return null
					}
				}
				if (isFocusOwner) {
					when (context.keyEvent?.key) {
						'0'.toInt() -> switchOff(context)
						'1'.toInt() -> switchOn(context)
						'\n'.toInt() -> toggle(context)
					}
				}
				keyDown = true
			}
			return null
		}

		override fun keyReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!toggle) {
				if (keyDown) {
					name?.let {
						if (it.length == 1 && it[0].toInt() == context.keyEvent?.key) {
							switchOff(context)
							keyDown = false
							return null
						}
					}
					if (isFocusOwner) {
						when (context.keyEvent?.key) {
							'1'.toInt() -> switchOff(context)
							'\n'.toInt() -> switchOff(context)
						}
					}
				}
			}
			keyDown = false
			return null
		}

		private fun switchOn(context: ActorInteractionContext) {
			model.on(context.signalHandler)
			context.keyEvent?.consume()
		}

		private fun switchOff(context: ActorInteractionContext) {
			model.off(context.signalHandler)
			context.keyEvent?.consume()
		}

		private fun toggle(context: ActorInteractionContext) {
			model.toggle(context.signalHandler)
			context.keyEvent?.consume()
		}
	}
}

class SwitchViewSelectionModel(component: SwitchView) : AbstractSelectionModel<SwitchView>(component) {

	override fun draw(context: DrawContext) {
		val oldUseContextColors = context.useContextColors
		context.useContextColors = true
		context.color = Themes.get<AntaresTheme>().selection.color
		component.drawSelected(context)
		context.useContextColors = oldUseContextColors
	}

	override val boundingBox: RectangularShape get() = component.boundingBox

	override fun contains(x: Double, y: Double): Boolean {
		return component.contains(x, y)
	}

	override fun componentUpdated() {
		validate()
	}
}