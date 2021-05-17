package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.ROM
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.*
import kotlin.math.max


/**
 * A view of a [ROM].
 * TODO Extract code common with [RAMView] to common base class.
 */
class ROMView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val eventBus: EventBus = BaseModule.eventBus,
	model: ROM = ROM()
) : DigitalComponentView<ROM>(styleProvider, model) {

	companion object {

		/** The width of a ROMView box if the contents are not displayed.*/
		const val MIN_WIDTH = 24 * Look.GRID

		/** The height of a ROMView box if the contents are not displayed.*/
		const val MIN_HEIGHT = 12 * Look.GRID

		/** The horizontal inset between the outer box and the contents box.*/
		const val HORIZONTAL_CONTENTS_INSET = 20

		/** The vertical inset between the outer box and the contents box.*/
		const val VERTICAL_CONTENTS_INSET = 40

		const val LABEL_INSET = 20
	}

	private val inputEventHandler = DoubleClickHandler()
	private val actorInteractionHandler = DoubleClickActorHandler()

	private val label = Label(
		font = font,
		text = buildLabelText(),
		rotationDisplayStrategy = Label.RotationDisplayStrategy.ROTATE_HALF,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER
	)

	private var contentsView = AddressableContentsView(model)

	override fun modelExchanged(oldModel: ROM?) {
		super.modelExchanged(oldModel)

		val addressPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getAddressInput(),
			direction = Direction.WEST)
		addressPV.setLocation(addressPV.length, 0)
		addPortView(addressPV)

		val csPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getChipSelectInput(),
			direction = Direction.SOUTH)
		csPV.setLocation(csPV.length + MIN_WIDTH / 2, MIN_HEIGHT / 2)
		addPortView(csPV)

		val dataPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getDataOutput(),
			direction = Direction.EAST)
		dataPV.setLocation(dataPV.length + MIN_WIDTH, 0)
		addPortView(dataPV)

		label.text = buildLabelText()
		contentsView = AddressableContentsView(
			addressable = model,
			rowsCount = contentRowsCount,
			columnsCount = contentColumnsCount,
			showDisassembler = showDisassembler,
			highlightCurrentCellWhenNotSelected = highlightCurrentCellWhenNotSelected)
	}

	init {
		modelExchanged(null)
		updateGeometry()
	}

	/** ---- UI properties */

	/**
	 * The text to be used for overwriting the default [ROMView] text, if any. If `null` no overwriting
	 * takes place. Can also be set to an empty [String] in order to hide the predefined label.
	 */
	var text: Translatable? = null
		set(value) {
			if (value != text) {
				field = value
				label.text = if (value == null || value.isEmpty) buildLabelText() else value.getTranslation()
			}
		}

	var addressWidth: BitWidth
		get() = model.addressWidth
		set(value) {
			invalidate()
			model.setAddressWidth(value)
			invalidate()
			validate()
		}

	var dataWidth: BitWidth
		get() = model.dataWidth
		set(value) {
			invalidate()
			model.setDataWidth(value)
			invalidate()
			validate()
		}

	var showContents: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				updateGeometry()
				validate()
			}
		}

	var contentRowsCount: Int
		get() = contentsView.rowsCount
		set(value) {
			if (value != contentRowsCount) {
				contentsView.rowsCount = value
				updateGeometry()
				validate()
			}
		}

	var contentColumnsCount: Int
		get() = contentsView.columnsCount
		set(value) {
			if (value != contentColumnsCount) {
				contentsView.columnsCount = value
				updateGeometry()
				validate()
			}
		}

	var disassemblerConfig: ScriptProperty
		get() = ScriptProperty(model.disassemblerConfig)
		set(value) {
			model.disassemblerConfig = value.script!!
		}

	var showDisassembler: Boolean
		get() = contentsView.showDisassembler
		set(value) {
			if (value != showDisassembler) {
				contentsView.showDisassembler = value
				updateGeometry()
				validate()
			}
		}

	var highlightCurrentCellWhenNotSelected: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				contentsView.highlightCurrentCellWhenNotSelected = field
				validate()
			}
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (showContents) {
			writer.writeBoolean("showContents", showContents)
		}
		text?.let { writer.writeStorables("text", it.allTranslations()) }
		writer.writeInt("contentRowsCount", contentRowsCount)
		writer.writeInt("contentColumnsCount", contentColumnsCount)
		writer.writeBoolean("showDisassembler", showDisassembler)
		if (highlightCurrentCellWhenNotSelected) {
			writer.writeBoolean("highlightCurrentCell", highlightCurrentCellWhenNotSelected)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("showContents")) {
			reader.requestResolution(this, Reference(
				name = "showContents",
				additionalInfo = reader.readBoolean("showContents"),
				resolveAfter = listOf(reader.readInt("modelId"))))
		}

		var tempText: Translatable? = null
		if (reader.hasAttribute("text")) {
			// Backward compatibility
			tempText = TranslatableText(reader.readString("text"))
		} else if (reader.hasElement("text")) {
			tempText = TranslatableText(reader.readStorables("text"))
		}
		// The default text depends on model data, so resolve the text after the model has been read
		reader.requestResolution(this, Reference(
			name = "text",
			additionalInfo = tempText,
			resolveAfter = listOf(reader.readInt("modelId"))))

		if (reader.hasAttribute("contentRowsCount")) {
			contentRowsCount = reader.readInt("contentRowsCount")
		}
		if (reader.hasAttribute("contentColumnsCount")) {
			contentColumnsCount = reader.readInt("contentColumnsCount")
		}
		if (reader.hasAttribute("showDisassembler")) {
			showDisassembler = reader.readBoolean("showDisassembler")
		}
		if (reader.hasAttribute("highlightCurrentCell")) {
			highlightCurrentCellWhenNotSelected = reader.readBoolean("highlightCurrentCell")
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "text") {
			text = reference.additionalInfo as Translatable?
		} else if (reference.name == "showContents") {
			showContents = reference.additionalInfo as Boolean
		}
	}

	/** ---- [Component] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		label.ownerRotation = rotation
		updateGeometry()
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		label.text = if (text == null) buildLabelText() else text!!.getTranslation()
		contentsView.handleCurrentAddressChanged()
		super.handleStateChanged(event)
	}

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color
		val oldStroke = context.g.stroke

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(bounds)
			}
		}

		context.g.color = context.choose(color).backgroundColor
		context.g.fill(bounds)

		context.g.color = context.choose(color).foregroundColor
		context.g.stroke = stroke
		context.g.draw(bounds)

		label.draw(context)

		if (requireDrawContents(context)) {
			context.stylable = this
			context.g.translate(contentsView.x, contentsView.y)
			context.g.rotate(rotation.inverse().angle)
			context.g.translate(-contentsView.x, -contentsView.y)
			contentsView.draw(context)
			context.g.translate(contentsView.x, contentsView.y)
			context.g.rotate(-rotation.inverse().angle)
			context.g.translate(-contentsView.x, -contentsView.y)
			context.stylable = null
		}

		context.g.color = oldColor
		context.g.stroke = oldStroke

		super.drawImpl(context)
	}

	/** Determines whether drawing hte [AddressableContentsView] is required depending on the [CurrentSystemSpeedCategory].*/
	private fun requireDrawContents(context: DrawContext): Boolean {
		return showContents && (
			!context.castedAppContext<GraphApplicationContext>()!!.isExecute
				|| context.castedAppContext<GraphApplicationContext>()!!.isPausing
				|| ExecutionModule.currentSystemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.Observe)
	}

	/** ---- [AbstractVerticeView] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return inputEventHandler
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [ROMView] */

	private fun updateGeometry() {
		invalidate()
		contentsView.updateGeometry()

		val addressPV = getPortView(model.getAddressInput())!!
		val x = addressPV.unconnectedLength

		val totalHeight = Look.scaleToDoubleGrid(calculateHeight())
		val totalWidth = Look.scaleToDoubleGrid(calculateWidth())
		setBounds(x, -totalHeight / 2, totalWidth, totalHeight)

		if (showContents) {
			contentsView.location = calculateContentsLocation()
		}
		label.location = Point2D(x + width / 2.0, y + LABEL_INSET)

		addressPV.setLocation(addressPV.unconnectedLength, 0)

		val dataPV = getPortView(model.getDataOutput())!!
		dataPV.setLocation(dataPV.unconnectedLength + width, 0.0)

		getPortView(model.getChipSelectInput())!!.setLocation(x + width / 2, height / 2)

		invalidate()

		update()
	}

	private fun calculateContentsLocation(): Point2D {
		return when (rotation) {
			Rotation.R0 -> Point2D(x + width / 2 - contentsView.width / 2, -contentsView.height / 2)
			Rotation.R90 -> Point2D(x + width / 2 + contentsView.height / 2, y + height / 2 - contentsView.width / 2)
			Rotation.R180 -> Point2D(x + width / 2 + contentsView.width / 2, contentsView.height / 2)
			Rotation.R270 -> Point2D(x + width / 2 - contentsView.height / 2, contentsView.width / 2)
		}
	}

	private fun calculateWidth(): Int {
		return if (showContents) {
			max(MIN_WIDTH, when (rotation) {
				Rotation.R0, Rotation.R180 -> (contentsView.width + 2 * HORIZONTAL_CONTENTS_INSET).toInt()
				Rotation.R90, Rotation.R270 -> (contentsView.height + 2 * HORIZONTAL_CONTENTS_INSET).toInt()
			})
		} else {
			MIN_WIDTH
		}
	}

	private fun calculateHeight(): Int {
		return if (showContents) {
			max(MIN_HEIGHT, when (rotation) {
				Rotation.R0, Rotation.R180 -> (contentsView.height + 2 * VERTICAL_CONTENTS_INSET).toInt()
				Rotation.R90, Rotation.R270 -> (contentsView.width + 2 * VERTICAL_CONTENTS_INSET).toInt()
			})
		} else {
			MIN_HEIGHT
		}
	}

	private fun buildLabelText(): String {
		return "ROM ${addressWidth.size}x${dataWidth.width}"
	}

	private fun requestOpenMemoryContents(readonly: Boolean, newDesktopView: Boolean) {
		eventBus.post(OpenMemoryContentsRequest(this, label.text, model, readonly, newDesktopView))
	}

	private inner class DoubleClickHandler : InputEventHandlerAdapter<InputEventContext>() {
		override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
			if (context.mouseEvent?.clickCount == 2) {
				requestOpenMemoryContents(context.readonly, context.mouseEvent?.isAltDown == true)
				return null
			}
			return super.mouseClicked(context)
		}
	}

	private inner class DoubleClickActorHandler : ClickableActorInteractionHandlerAdapter() {
		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.clickCount == 2) {
				requestOpenMemoryContents(false, context.mouseEvent?.isAltDown == true)
			}
			return null
		}
	}
}