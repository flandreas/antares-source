package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.AddressableVertice
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy.ROTATE_HALF
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.ImmediateVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.*
import kotlin.math.max

abstract class AbstractAddressableView<T : AddressableVertice>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	eventBus: EventBus = BaseModule.eventBus,
	model: T
) : OrientableRectangularVerticeView<T>(styleProvider, model),
	ControlView<T>,
	ControlViewSource<T>
{

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

	val label = Label(
		font = font,
		text = buildLabelText(),
		rotationDisplayStrategy = ROTATE_HALF,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER
	)

	protected var contentsView = AddressableContentsView(model)

	private val inputEventHandler = AddressableInputEventHandler(
		eventBus,
	) { view, newDesktopView ->
		val verticeLink: VerticeLink = if (isActiveControlView) {
			displayContentVerticeLink!!
		} else {
			ImmediateVerticeLink(this.model.id)
		}
		OpenMemoryContentsRequest(
			view,
			displayContentVerticeView ?: this,
			label.text,
			verticeLink as ObjectLink<Addressable>,
			newDesktopView)
	}

	/** ---- UI properties */

	/**
	 * The text to be used for overwriting the default [RAMView] text, if any. If `null` no overwriting
	 * takes place. Can also be set to an empty [String] in order to hide the predefined label.
	 */
	var text: Translatable? = null
		set(value) {
			if (value != text) {
				field = value
				// Forward to model to be accessible for HDL generation
				if (value == null || value.isEmpty) {
					label.text = buildLabelText()
					model.name = null
				} else {
					label.text = value.getTranslation()
					model.name = label.text
				}
				postControlViewSourceChangeEvent()
			}
		}

	var addressWidth: BitWidth
		get() = model.addressWidth
		set(value) {
			invalidate()
			model.addressWidth = value
			if (showContents) {
				updateGeometry()
			}
			invalidate()
			validate()
			postControlViewSourceChangeEvent()
		}

	var dataWidth: BitWidth
		get() = model.dataWidth
		set(value) {
			invalidate()
			model.dataWidth = value
			if (showContents) {
				updateGeometry()
			}
			invalidate()
			validate()
			postControlViewSourceChangeEvent()
		}

	var showContents: Boolean = false
		set(value) {
			if (field != value) {
				field = value
				updateGeometry()
				validate()
				postControlViewSourceChangeEvent()
			}
		}

	var contentRowsCount: Int
		get() = contentsView.rowsCount
		set(value) {
			if (value != contentRowsCount) {
				contentsView.rowsCount = value
				updateGeometry()
				validate()
				postControlViewSourceChangeEvent()
			}
		}

	var contentColumnsCount: Int
		get() = contentsView.columnsCount
		set(value) {
			if (value != contentColumnsCount) {
				contentsView.columnsCount = value
				updateGeometry()
				validate()
				postControlViewSourceChangeEvent()
			}
		}

	/** ---- [AbstractVerticeView] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		inputEventHandler.getInputEventHandler()

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		inputEventHandler.getActorInteractionHandler(this)

	/** ---- [Component] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		label.ownerRotation = rotation
		updateGeometry()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (showContents) {
			writer.writeBoolean("showContents", showContents)
		}
		text?.let {
			if (it.isNotEmpty) {
				writer.writeStorables("text", it.allTranslations())
			}
		}
		writer.writeInt("contentRowsCount", contentRowsCount)
		writer.writeInt("contentColumnsCount", contentColumnsCount)
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
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "text") {
			text = reference.additionalInfo as Translatable?
		} else if (reference.name == "showContents") {
			showContents = reference.additionalInfo as Boolean
		}
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		if (event.signalHandler == null) {
			label.text = if (text == null) buildLabelText() else text!!.getTranslation()
		}
		if (model.isSelected) {
			contentsView.handleCurrentAddressChanged()
		}
		super.handleStateChanged(event)
	}

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color

		drawImplBeforeBorder(context)
		drawShape(context, getApplicableForegroundColor(context), getApplicableBackgroundColor(context), stroke)
		drawImplAfterBorder(context)

		context.g.color = oldColor
	}

	private fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(bounds)
			}
		}

		context.g.color = backgroundColor
		context.g.fill(bounds)

		context.g.color = foregroundColor
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
	}

	/** Determines whether drawing hte [AddressableContentsView] is required depending on the [CurrentSystemSpeedCategory].*/
	private fun requireDrawContents(context: DrawContext): Boolean {
		val graphApplicationContext = context.castedAppContext<GraphApplicationContext>()!!
		return showContents && (
			!graphApplicationContext.isExecute
				|| graphApplicationContext.isPausing
				|| graphApplicationContext.systemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.Observe)
	}

	/** ---- [ControlViewSource] */

	override val controlName: String get() = super.controlName

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	/**
	 * The link to the [Addressable] containing the content to be opened for displaying in another view.
	 * Only used during execution. Only set if this [AbstractAddressableView] is used as [ControlView]
	 * in a [SubGraphVerticeView], otherwise `null`.
	 */
	private var displayContentVerticeLink: VerticeLink? = null

	/**
	 * The [VerticeView] that contains this [AbstractVerticeView] and is therefore rendered as "origin" of this
	 * [AbstractAddressableView]. If used as a [ControlView], this is the [SubGraphVerticeView], otherwise this is
	 * a ROM or RAM. Only used during execution. Only set if this [AbstractAddressableView] is used as [ControlView]
	 * 	 * in a [SubGraphVerticeView], otherwise `null`.
	 */
	private var displayContentVerticeView: VerticeView<*>? = null

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		displayContentVerticeLink = if (link is DeepVerticeLink) {
			link.prepend(subGraphVerticeView.model.id)
		} else {
			link
		}
		this.model = link.getLinkedObject(startGraph) as T
		this.displayContentVerticeView = subGraphVerticeView
	}

	override fun writeModelProperties(writer: StoreWriter) {}

	override fun readModelProperties(reader: StoreReader) {}

	override fun sourcePropertiesChanged(source: ControlViewSource<T>) {
		if (source is AbstractAddressableView<*>) {
			copyControlViewProperties(source, this)
		}
	}

	protected open fun copyControlViewProperties(source: AbstractAddressableView<*>, dest: AbstractAddressableView<*>) {
		dest.addressWidth = source.addressWidth
		dest.dataWidth = source.dataWidth
		dest.text = source.text
		dest.showContents = source.showContents
		dest.contentRowsCount = source.contentRowsCount
		dest.contentColumnsCount = source.contentColumnsCount
		dest.customColor = source.customColor
	}

	/** ---- [AbstractAddressableView] */

	protected abstract fun updatePortViewPositions()

	protected fun updateGeometry() {
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

		val dataPV = getPortView(model.getDataPort())!!
		dataPV.setLocation(dataPV.unconnectedLength + width, 0.0)

		updatePortViewPositions()

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

	protected fun buildLabelText(): String =
		"$type ${StringUtils.removeWhitespace(addressWidth.size)} x ${dataWidth.width}"
}