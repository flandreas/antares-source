package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.InputEvent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.Translatable
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.actor.*
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.PortViewReuser
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [SubGraphVerticeView] interface.
 */
class SubGraphVerticeViewImpl(
	graphElement: SubGraphVerticeRef = SubGraphVerticeRef(),
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway
) : AbstractVerticeView<SubGraphVerticeRef>(
	styleProvider,
	graphElement
), SubGraphVerticeView<SubGraphVerticeRef> {

	companion object {
		val LOG by logger(SubGraphVerticeViewImpl::class)
	}

	/** Contains the [Drawable]s that make up the look of this [SubGraphVerticeView].*/
	private val drawableBag = SubSymbolBag()

	private val editInteractionHandler = EditInteractionHandler()

	/** The [ContainerDrawing] that has been customized by the user for this [SubGraphVerticeView], if any.*/
	private var customizedContainerDrawing: ContainerDrawing? = null

	private val containsBox = Rectangle2D()

	private val _boundingBox = Rectangle2D()

	override var location: Point2D = Point2D.ZERO
		set(value) {
			invalidate()
			field = value
			drawableBag.location = value
			updateBoxes()
			update()
			invalidate()
		}

	init {
		modelExchanged(null)
	}

	/** ---- UI properties */

	// Used by reflection as bean property
	@Suppress("MemberVisibilityCanBePrivate")
	var isHorizontallyMirrored: Boolean = false
		set(value) {
			if (value == field) {
				return
			}
			field = value
			mirrorHorizontally(location.x)
		}

	// Used by reflection as bean property
	@Suppress("MemberVisibilityCanBePrivate")
	var isVerticallyMirrored: Boolean = false
		set(value) {
			if (value == field) {
				return
			}
			field = value
			mirrorVertically(location.y)
		}

	/**
	 * The text to be used to overwrite the first [LabelComponent], if any. If `null` no overwriting
	 * takes place. Can also be set to an empty [String] in order to hide the predefined label.
	 */
	var label: Translatable? = null
		get() {
			val labelComponent = getLabelComponent() ?: return null
			if (field != null) {
				return field
			}
			return labelComponent.text
		}
		set(value) {
			val labelComponent = getLabelComponent()
			if (labelComponent != null) {
				field = value
				invalidate()
				field?.let { labelComponent.text = it }
				invalidate()
			}
		}

	var orientation: Direction
		get() = Direction.of(rotation)
		set(value) {
			rotation = value.rotation
		}

	/** ---- [Transparent] */

	override var transparency: Int
		get() = super.transparency
		set(value) {
			super.transparency = value
			drawableBag.drawables.filterIsInstance<Transparent>().forEach { it.transparency = value }
		}

	/** ---- [Stylable] */

	override var styleProvider: StyleProvider
		get() = super.styleProvider
		set(value) {
			if (super.styleProvider != value) {
				super.styleProvider = value
				drawableBag.drawables.filterIsInstance<Stylable>().forEach { it.styleProvider = value }
			}
		}

	/** ---- [Drawable] */

	override fun getBoundingBoxImpl(): Rectangle2D {
		return _boundingBox
	}

	override fun contains(x: Double, y: Double): Boolean {
		return rotate(containsBox).contains(x, y)
	}

	override fun drawImpl(context: DrawContext) {
		drawableBag.drawables.forEach { it.draw(context) }
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute && StringUtils.isNotEmpty(drawExecScript)) {
			scriptGateway.exec(wrappedDrawExecScript, this, context)
		}
		super.drawImpl(context)
	}

	private val wrappedDrawExecScript: Script
		get() =
			Script(
				code = drawExecScript!!,
				origin = Translations.getString("graph.property.ContainerDrawing", StringUtils.orElse(model.name, "?")),
				context = Translations.getString("graph.property.ContainerDrawing.execDrawScript.name"))

	fun drawWithDrawableDrawer(context: DrawContext, drawableDrawer: (Drawable) -> Unit) {
		draw(context) { c ->
			drawableBag.drawables.forEach { drawableDrawer.invoke(it) }
			super.drawImpl(c)
		}
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("x", location.x)
		writer.writeDouble("y", location.y)
		if (isHorizontallyMirrored) {
			writer.writeBoolean("mirrorH", isHorizontallyMirrored)
		}
		if (isVerticallyMirrored) {
			writer.writeBoolean("mirrorV", isVerticallyMirrored)
		}
		if (label != null && label!!.isNotEmpty && label != getLabelComponent()?.text) {
			writer.writeStorables("label", label!!.allTranslations())
		}
		if (customizedContainerDrawing != null) {
			writer.writeStorable("container", customizedContainerDrawing!!)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)

		location = Point2D(reader.readDouble("x"), reader.readDouble("y"))
		if (reader.hasAttribute("mirrorH")) {
			reader.requestResolution(this, Reference(
				name = "mirrorH",
				referenceId = 0,
				additionalInfo = reader.readBoolean("mirrorH"),
				resolveAfter = listOf(reader.readInt(STORABLE_MODEL_ID))
			))
		}
		if (reader.hasAttribute("mirrorV")) {
			reader.requestResolution(this, Reference(
				name = "mirrorV",
				referenceId = 0,
				additionalInfo = reader.readBoolean("mirrorV"),
				resolveAfter = listOf(reader.readInt(STORABLE_MODEL_ID))
			))
		}
		var tempLabel: Translatable? = null
		if (reader.hasAttribute("label")) {
			// Backward compatibility
			tempLabel = TranslatableText(reader.readString("label"))
		}
		if (reader.hasElement("label")) {
			tempLabel = TranslatableText(reader.readStorables("label"))
		}
		if (tempLabel != null) {
			// The label depends on the container drawing, so resolve the label after the model has been read
			reader.requestResolution(this, Reference(
				name = "label",
				additionalInfo = tempLabel,
				resolveAfter = listOf(reader.readInt(STORABLE_MODEL_ID))
			))
		}

		if (reader.hasElement("container")) {
			customizedContainerDrawing = reader.readStorable("container") as ContainerDrawing
			val globalId = reader.getGlobalId(customizedContainerDrawing!!)
			reader.requestResolution(this, Reference(
				name = "container",
				referenceId = globalId,
				resolveAfter = listOf(reader.readInt(STORABLE_MODEL_ID), globalId)
			))
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)

		if (STORABLE_MODEL_ID == reference.name) {
			if (customizedContainerDrawing == null) {
				val graph = repository.getOptionalMetaGraph(model.graphUUID!!)
				if (graph != null) {
					fillFromContainerDrawing(graph.containerDrawing)
				}
			}
			if (model.designError != null) {
				fillDesignErrorRepresentation()
			}
		}
		if ("label" == reference.name) {
			label = reference.additionalInfo as Translatable?
		}
		if ("mirrorH" == reference.name) {
			isHorizontallyMirrored = reference.additionalInfo as Boolean
		}
		if ("mirrorV" == reference.name) {
			isVerticallyMirrored = reference.additionalInfo as Boolean
		}
	}

	override fun resolutionDone() {
		// Make sure that SubGraphVerticeView is filled from ContainerDrawing AFTER the PortViewComponents
		// in the ContainerDrawings have resolved their model SubCircuitPort. We cannot make more sure than doing
		// it after all resolutions have been done.
		if (customizedContainerDrawing != null && model.designError == null) {
			repository.getMetaGraph(model.graphUUID!!)
			fillFromContainerDrawing(customizedContainerDrawing!!)
		}
	}

	/** ---- [Component] */

	override var rotation: Rotation
		get() = super.rotation
		set(value) {
			super.rotation = value
			drawableBag.rotation = value
		}

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		drawableBag.drawables.forEach {
			when (it) {
				is LabelComponent -> it.label.ownerRotation = newRotation
				is BrokenReferenceView -> it.label.ownerRotation = newRotation
			}
		}
	}

	/** ---- [GraphElementView] */

	override fun bind(graph: Graph) {
		super.bind(graph)
		if (model.designError == null) {
			val innerGraph = getGraph()
			getControlViewComponents().forEach { it.bindControlView(this, innerGraph, repository, storableCreator) }
		}
	}

	/** ---- [AbstractVerticeView] */

	override fun addPortView(portView: PortView<*>) {
		mirrorIfNecessary(portView)
		super.addPortView(portView)
		addPortViewTo(portView, _boundingBox, containsBox)
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return editInteractionHandler
	}

	/** ---- [SubGraphVerticeView] */

	override val subGraphVertice: SubGraphVertice get() = model

	override var drawExecScript: String? = null

	override val hasCustomizedContainerDrawing: Boolean get() = customizedContainerDrawing != null

	override fun addDrawable(drawable: Drawable) {
		mirrorIfNecessary(drawable)
		if (drawable is ControlViewComponent) {
			// Handle validation events from ControlView in order to update the UI
			// whenever state of ControlView changes
			DrawableOwner(this, drawable)
		} else if (drawable is LabelComponent) {
			drawable.label.ownerRotation = rotation
		}
		drawableBag.add(drawable)
		_boundingBox.add(drawable.boundingBox)
		containsBox.add(drawable.boundingBox)
	}

	override fun createSubGraphView(): GraphView {
		val libraryGraph = repository.getMetaGraph(subGraphVertice.graphUUID!!)
		val graphView = libraryGraph.graph.graphView.cloneForExistingModel(getGraph(), storableCreator)
		graphView.bind()
		return graphView
	}

	override fun getEditableContainerDrawing(): ContainerDrawing {
		if (customizedContainerDrawing != null) {
			return customizedContainerDrawing!!
		}
		return getLibraryContainerDrawing()
	}

	override fun setEditedContainerDrawing(containerDrawing: ContainerDrawing?) {
		invalidate()
		customizedContainerDrawing = containerDrawing
		fillFromContainerDrawing(customizedContainerDrawing ?: getLibraryContainerDrawing())
		invalidate()
		validate()
		update()
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		drawableBag.getActorInteractionHandler(context)

	override val canMirror: Boolean get() = drawableBag.drawables.all { it.canMirror }

	override fun mirrorHorizontally(x: Double) {
		if (!canMirror) {
			LOG.error("Unsupported request to mirror horizontally")
			throw UnsupportedOperationException("cannot mirror horizontally")
		}
		invalidate()
		drawableBag.drawables.forEach { it.mirrorHorizontally(x - location.x) }
		getPortViews().forEach { it.mirrorHorizontally(x - location.x) }
		updateBoxes()
		invalidate()
	}

	override fun mirrorVertically(y: Double) {
		if (!canMirror) {
			LOG.error("Unsupported request to mirror vertically")
			throw UnsupportedOperationException("cannot mirror vertically")
		}
		invalidate()
		drawableBag.drawables.forEach { it.mirrorVertically(y - location.y) }
		getPortViews().forEach { it.mirrorVertically(y - location.y) }
		updateBoxes()
		invalidate()
	}

	/** ---- [SubGraphVerticeViewImpl] */

	fun getControlViewComponents(): ImmutableList<ControlViewComponent> =
		drawableBag.drawables.filterIsInstance<ControlViewComponent>().map { it }.toImmutableList()

	private fun getGraph(): Graph =
		subGraphVertice.getGraph(repository, storableCreator)

	private fun getLabelComponent(): LabelComponent? =
		drawableBag.drawables.filterIsInstance<LabelComponent>().map { it }.firstOrNull()

	// Visible for testing
	fun fillFromContainerDrawing(containerDrawing: ContainerDrawing) {
		val reuser = PortViewReuser(this)
		drawableBag.clear()
		clearPortViews()

		containerDrawing.fillSubGraphVerticeView(this)
		reuser.reuse()

		label?.let {
			getLabelComponent()?.text = it
		}

		updateBoxes()
	}

	private fun fillDesignErrorRepresentation() {
		drawableBag.clear()
		drawableBag.add(drawable = BrokenReferenceView(rotation, styleProvider))
		updateBoxes()
	}

	private fun getLibraryContainerDrawing(): ContainerDrawing {
		val libraryGraph = repository.getMetaGraph(subGraphVertice.graphUUID!!)
		return StorableCloner.clonePreservingIdentities(libraryGraph.containerDrawing, storableCreator)
	}

	private fun updateBoxes() {
		_boundingBox.setFrame(0.0, 0.0, 0.0, 0.0)
		containsBox.setFrame(_boundingBox)
		addPortViewsTo(_boundingBox, containsBox)

		drawableBag.drawables.forEach {
			val r = Rectangle2D(it.boundingBox)
			r.setFrame(location.x + r.x, location.y + r.y, r.width, r.height)
			_boundingBox.add(r)
			containsBox.add(r)
		}

		DropShadow.expand(_boundingBox, rotation)
	}

	private fun mirrorIfNecessary(drawable: Drawable) {
		if (isHorizontallyMirrored) {
			drawable.mirrorHorizontally(location.x)
		}
		if (isVerticallyMirrored) {
			drawable.mirrorVertically(location.y)
		}
	}

	private fun requestOpenSubGraph(event: InputEvent) {
		eventBus.post(OpenSubGraphRequest(this, newView = event.isAltDown, quickMode = event.isMetaDown))
	}

	private inner class EditInteractionHandler : InputEventHandlerAdapter<InputEventContext>() {
		override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
			if (context.mouseEvent?.button == Button.BUTTON1 && context.mouseEvent?.clickCount == 2) {
				requestOpenSubGraph(context.mouseEvent!!)
				return null
			}
			return super.mouseClicked(context)
		}
	}

	private inner class SubSymbolBag : ActorViewBag<Drawable>(useLocation = true) {

		override fun createHandler(): Handler = SubSymbolBagHandler()

		private inner class SubSymbolBagHandler : Handler() {
			override fun mouseClicked(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
				val handler = super.mouseClicked(context)
				if (context.mouseEvent?.isConsumed() != true && context.mouseEvent?.clickCount == 2) {
					requestOpenSubGraph(context.mouseEvent!!)
					return null
				}
				return handler
			}
		}
	}
}
