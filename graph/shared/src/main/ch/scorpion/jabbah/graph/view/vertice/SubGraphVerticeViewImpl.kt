package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.ScriptMetaData
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
import ch.scorpion.jabbah.draw.graphics.Cursor
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ActorViewBag
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.DrawExecSymbolFunctions
import ch.scorpion.jabbah.graph.dsl.GraphDslInterpreter
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
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
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder,
	private val eventBus: EventBus = BaseModule.eventBus
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

	private var drawExecScriptInterpreter: Interpreter? = null

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

	override var controlViewVisibility: ControlViewVisibility = ControlViewVisibility.DEFAULT
		set(value) {
			if (value == field) {
				return
			}
			field = value
			invalidate()
			validate()
		}

	/**
	 * Corresponds with [label]. Used to decide whether label is overwritten and needs to
	 * be stored.
	 */
	private var _label: Translatable? = null

	override var label: Translatable? = null
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
				_label = field
				invalidate()
				updateLabelComponent(field)
				invalidate()
			}
		}

	private val effectiveLabelText: Translatable? get() = _label ?: defaultLabelText

	private val defaultLabelText: Translatable? get() = repository
		.getMetaGraph(model.graphUUID!!)
		.containerDrawing.drawables.filterIsInstance<LabelComponent>()
		// the last one is the first one added (i.e. the bottom-most in the stacking order)
		.lastOrNull()?.text

	private fun updateLabelComponent(label: Translatable?) {
		getLabelComponent()?.let { labelComponent ->
			if (label?.isEmpty == true) {
				resetLabel()
			} else {
				label?.let { labelComponent.text = it }
			}
		}
	}

	override var executionLabel: Translatable? = null
		set(value) {
			if (value != field) {
				field = value
				invalidate()
				getLabelComponent()?.let { it.text = value ?: TranslatableText("") }
				validate()
			}
		}

	var orientation: Direction
		get() = Direction.of(rotation)
		set(value) {
			rotation = value.rotation
		}

	override var customColor: PredefinedColor?
		get() = super.customColor
		set(value) {
			if (value != super.customColor) {
				super.customColor = value
				updateCustomColor(value)
			}
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
		drawImplBeforeBorder(context)

		drawableBag.drawables
			.filter { controlViewVisibility.drawFilter(it, context) }
			.forEach { it.draw(context) }

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute /*&& StringUtils.isNotEmpty(drawExecScript)*/) {
			drawExecScriptInterpreter?.let {
				DrawExecSymbolFunctions.bind(this, context)
				try {
					it.interpretCatching(ScriptMetaData(this.model.graphName.value, Translations.getString("graph.property.ContainerDrawing.execDrawScript.name")), rethrow = true)
				} catch (e: Throwable) {
					// Reset Interpreter in case of an error to avoid cascading errors
					// when the View is redrawn
					drawExecScriptInterpreter = null
				}
			}
		}

		drawImplAfterBorder(context)
	}

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

	fun drawWithDrawableDrawer(context: DrawContext, drawableDrawer: (Drawable) -> Unit) {
		draw(context) { c ->
			drawableBag.drawables
				.filter { controlViewVisibility.drawFilter(it, context) }
				.forEach { drawableDrawer.invoke(it) }
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
		if (_label != null) {
			writer.writeStorables("label", label!!.allTranslations())
		}
		if (customizedContainerDrawing != null) {
			writer.writeStorable("container", customizedContainerDrawing!!)
		}
		// Default is determined by ContainerDrawing, therefore store always
		writer.writeString("controlViewVisibility", controlViewVisibility.customName)
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
		if (reader.hasAttribute("controlViewVisibility")) {
			controlViewVisibility = ControlViewVisibility.withName(reader.readString("controlViewVisibility"))
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
			_label = reference.additionalInfo as Translatable?
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

		// Set the label loaded in resolve() not before here, because at the time of resolve(),
		// the DrawableBag is still empty
		if (_label != null) {
			label = _label
		}
	}

	/** ---- [ConnectableView] */

	override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?) {
		super.handleConnect(edgeView, port)
		if (port != null && !isReading) {
			updateBoxes()
		}
	}

	override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?, lockEndpoint: Boolean) {
		super.handleUnconnect(edgeView, port, lockEndpoint)
		if (port != null && !isReading) {
			updateBoxes()
		}
	}

	/** ---- [Component] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		drawableBag.rotation = newRotation
		drawableBag.drawables.forEach {
			if (it is Labeled) {
				it.label.ownerRotation = newRotation
			}
		}
	}

	/** ---- [GraphElementView] */

	override fun bind(graph: Graph, deep: Boolean) {
		super.bind(graph, deep)
		if (model.designError == null) {
			if (model.isDeepExecution(deep)) {
				val controlViewComponents = getControlViewComponents()
				if (controlViewComponents.isNotEmpty()) {
					val innerGraph = getGraph()
					controlViewComponents.forEach { it.bindControlView(this, innerGraph, repository, storableCreator) }
				}
			}
		}
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		super<AbstractVerticeView>.executionStarted(signalHandler)
		drawExecScriptInterpreter = createDrawScriptInterpreter(signalHandler)
		if (drawExecScriptInterpreter is GraphDslInterpreter) {
			(drawExecScriptInterpreter as GraphDslInterpreter).executionStarted()
		}
		executionLabel?.let { execLabel ->
			getLabelComponent()?.let { it.text = execLabel }
		}
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super<AbstractVerticeView>.executionStopped(signalHandler)
		getLabelComponent()?.let { labelComponent ->
			effectiveLabelText?.let {
				labelComponent.text = it
			}
		}
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		super.handleStateChanged(event)
		if (event.reason == Vertice.STATE_CHANGE_TYPE) {
			tooltip.reset()
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

	override val hasCustomizedContainerDrawing: Boolean get() = customizedContainerDrawing != null

	override fun addDrawable(drawable: Drawable) {
		mirrorIfNecessary(drawable)
		if (drawable is ControlViewComponent) {
			// Handle validation events from ControlView in order to update the UI
			// whenever state of ControlView changes
			DrawableOwner(this, drawable)
		} else if (drawable is LabelComponent) {
			drawable.label.ownerRotation = rotation
			drawable.label.rotationDisplayStrategy = RotationDisplayStrategy.ROTATE_HALF
		}
		drawableBag.add(drawable)
		_boundingBox.add(drawable.boundingBox)
		containsBox.add(drawable.boundingBox)
	}

	override fun createSubGraphView(signalHandler: SignalHandler?): GraphView {
		val libraryGraph = repository.getMetaGraph(subGraphVertice.graphUUID!!)
		val graphView = libraryGraph.graph.graphView.cloneForExistingModel(getGraph(), storableCreator)
		graphView.bind(signalHandler?.isDeepExecution ?: false)
		signalHandler?.let {
			graphView.executionStart(it)
		}
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

	override fun getLabelComponent(): LabelComponent? =
		drawableBag.drawables.filterIsInstance<LabelComponent>().map { it }.firstOrNull()

	private fun createDrawScriptInterpreter(signalHandler: SignalHandler): Interpreter? {
		return repository.getContainerLibraryElement(model.graphUUID!!).let { cle ->
			cle?.drawSymbolAST?.let { ast ->
				BaseModule.interpreterFactory(
					ast,
					Memory(GraphModelModule.subGraphVerticeRefActivationRecordFactory.create(model, signalHandler)))
			}
		}
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		drawableBag.getActorInteractionHandler(context)

	/** ---- [SubGraphVerticeViewImpl] */

	fun getControlViewComponents(): ImmutableList<ControlViewComponent> =
		drawableBag.drawables.filterIsInstance<ControlViewComponent>().map { it }.toImmutableList()

	private fun getGraph(): Graph =
		subGraphVertice.getGraph(repository, storableCreator)

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
		rotationChanged(rotation)

		updateCustomColor(customColor)

		updateBoxes()
	}

	private fun resetLabel() {
		repository.getMetaGraph(model.graphUUID!!).containerDrawing.drawables.filterIsInstance<LabelComponent>().lastOrNull()?.let {
			label = it.text
		}
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
			drawable.mirrorHorizontally(0.0)
		}
		if (isVerticallyMirrored) {
			drawable.mirrorVertically(0.0)
		}
	}

	private fun updateCustomColor(customColor: PredefinedColor?) {
		invalidate()
		drawableBag.drawables.forEach {
			if (it is Stylable) {
				it.customColor = customColor
			}
		}
		validate()
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

			override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
				val handler = super.mouseMoved(context)
				if (handler != null) {
					return handler
				}
				context.view.setCursor(Cursor.CLICK)
				return null
			}

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
