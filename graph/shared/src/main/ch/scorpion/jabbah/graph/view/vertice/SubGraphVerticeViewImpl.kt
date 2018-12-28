package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.collection.ConcatIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.InputEvent
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
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
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.TextProperty
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
    graphElement: SubGraphVerticeRef? = null,
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
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
    private val drawables = mutableListOf<Drawable>()

    /** Handles mouse double clicks during editing.*/
    private val editInteractionHandler = DoubleClickHandler()

    /** Handles mouse double clicks during execution*/
    private val executionInteractionHandler = DoubleClickExecutionHandler()

    /** The [ContainerDrawing] that has been customized by the user for this [SubGraphVerticeView], if any.*/
    private var customizedContainerDrawing: ContainerDrawing? = null

    private val containsBox = Rectangle2D()

    private val _boundingBox = Rectangle2D()

    override var location: Point2D = Point2D.ZERO
        set(value) {
            invalidate()
            field = value
            updateBoxes()
            update()
            invalidate()
        }

    init {
        if (graphElement != null) {
            modelExchanged(null)
        }
    }

    /** ---- UI properties */

    var isHorizontallyMirrored: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            field = value
            mirrorHorizontally(location.x)
        }

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
    var label: String? = null
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
                labelComponent.text = field?: ""
                invalidate()
            }
        }

	var orientation: Direction
		get() = Direction.of(rotation)
		set(value) {
			rotation = value.rotation
		}

	var descriptionProperty: TextProperty
		get() = model!!.descriptionProperty
		set(value) { model!!.descriptionProperty = value }

    /** ---- [Transparent] */

    override var transparency: Int
        get() = super.transparency
        set(value) {
            super.transparency = value
            drawables.filter { it is Transparent }.map { it as Transparent }.forEach { it.transparency = value }
        }

    /** ---- [Drawable] */

    override fun getBoundingBoxImpl(): Rectangle2D {
        return _boundingBox
    }

	override fun contains(x: Double, y: Double): Boolean {
        return rotate(containsBox).contains(x, y)
    }

    override fun drawImpl(context: DrawContext) {
        drawables.forEach { it.draw(context) }
	    if (context.castedAppContext<GraphApplicationContext>()!!.isExecute && StringUtils.isNotEmpty(drawExecScript)) {
		    scriptGateway.exec(Script(code = drawExecScript!!, origin = "Container ${model?.getGraphIfPresent()?.name}", context = "drawExecScript"), this, context)
	    }
        super.drawImpl(context)
    }

    fun drawWithDrawableDrawer(context: DrawContext, drawableDrawer: (Drawable) -> Unit) {
        draw(context) {
	        drawables.forEach { drawableDrawer.invoke(it) }
	        super.drawImpl(it)
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
        if (label != null) {
            writer.writeString("label", label!!)
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
        if (reader.hasAttribute("label")) {
            label = reader.readString("label")
        }
        if (reader.hasElement("container")) {
            customizedContainerDrawing = reader.readStorable("container") as ContainerDrawing
            // TEST BEGIN
            customizedContainerDrawing!!.areSubGraphPortsConsistent()
            // TEST END
            reader.requestResolution(this, Reference(
                name = "container",
                referenceId = customizedContainerDrawing!!.storableId,
                resolveAfter = listOf(reader.readInt(STORABLE_MODEL_ID), customizedContainerDrawing!!.storableId)
            ))
        }
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        super.resolve(reference, referenceResolver)

        if (STORABLE_MODEL_ID == reference.name) {
            if (customizedContainerDrawing == null) {
                // TODO Support for requesting only the ContainerDrawing from the LibraryImpl
                val graph = repository.getOptionalMetaGraph(model!!.graphUUID!!)
                if (graph != null) {
                    fillFromContainerDrawing(graph.containerDrawing)
                    model!!.shortDescription = graph.graph.model!!.shortDescription
                }
            }
	        if (model!!.designError != null) {
		        fillDesignErrorRepresentation()
	        }
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
        if (customizedContainerDrawing != null && model!!.designError == null) {
            val graph = repository.getMetaGraph(model!!.graphUUID!!)
            fillFromContainerDrawing(customizedContainerDrawing!!)
            model!!.shortDescription = graph.graph.model!!.shortDescription
        }
    }

    override fun getStorableChildren(): Iterator<Storable> {
        if (customizedContainerDrawing == null) {
            return super.getStorableChildren()
        }
        return ConcatIterator(super.getStorableChildren(), listOf(customizedContainerDrawing!!).iterator())
    }

    /** ---- [Component] */

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.REPLACE
        set(value) { super.preferredSelectionDrawingStrategy = value }

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		drawables.forEach {
			when (it) {
				is LabelComponent -> it.label.ownerRotation = newRotation
				is BrokenReferenceView -> it.label.ownerRotation = newRotation
			}
		}
	}

    /** ---- [GraphElementView] */

    override fun bind(graph: Graph) {
        super.bind(graph)
        if (model!!.designError == null) {
	        val innerGraph = getGraph()
	        getControlViewComponents().forEach { it.bindToGraph(innerGraph, repository, storableCreator) }
        }
    }

    /** ---- [AbstractVerticeView] */

    override val type: String? get() = model?.name

    override val shortDescription: String? get() = model?.shortDescription

    override fun addPortView(portView: PortView<*>) {
        mirrorIfNecessary(portView)
        super.addPortView(portView)
        addPortViewTo(portView, _boundingBox, containsBox)
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return editInteractionHandler
    }

    /** ---- [SubGraphVerticeView] */

    override val subGraphVertice: SubGraphVertice? get() = model

	override var drawExecScript: String? = null

    override fun addDrawable(drawable: Drawable) {
        mirrorIfNecessary(drawable)
        if (drawable is ControlViewComponent) {
            // Handle validation events from ControlView in order to update the UI
            // whenever state of ControlView changes
            DrawableOwner(this, drawable)
        } else if (drawable is LabelComponent) {
	        drawable.label.ownerRotation = rotation
        }
        drawables.add(0, drawable)
        _boundingBox.add(drawable.boundingBox)
        containsBox.add(drawable.boundingBox)
    }

    override fun createSubGraphView(): GraphView<GraphElementView<SubGraphVerticeRef>> {
        val libraryGraph = repository.getMetaGraph(subGraphVertice!!.graphUUID!!)
        val graphView = libraryGraph.graph.graphView.cloneForExistingModel(getGraph(), storableCreator)
        graphView.bind()
        return graphView as GraphView<GraphElementView<SubGraphVerticeRef>>
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

    override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
        return executionInteractionHandler
    }

    override val canMirror: Boolean get() = drawables.all { it.canMirror }

    override fun mirrorHorizontally(x: Double) {
        if (!canMirror) {
            LOG.error("Unsupported request to mirror horizontally")
            throw UnsupportedOperationException("cannot mirror horizontally")
        }
        invalidate()
        drawables.forEach { it.mirrorHorizontally(x - location.x) }
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
        drawables.forEach { it.mirrorVertically(y - location.y) }
        getPortViews().forEach { it.mirrorVertically(y- location.y) }
        updateBoxes()
        invalidate()
    }

    /** ---- [SubGraphVerticeViewImpl] */

    fun getControlViewComponents(): ImmutableList<ControlViewComponent> {
        return drawables.filter { it is ControlViewComponent }.map { it as ControlViewComponent }.toImmutableList()
    }

    private fun getGraph(): Graph {
        return subGraphVertice!!.getGraph(repository, storableCreator)
    }

    private fun getLabelComponent(): LabelComponent? {
        return drawables.filter { it is LabelComponent }.map { it as LabelComponent }.firstOrNull()
    }

    private fun fillFromContainerDrawing(containerDrawing: ContainerDrawing) {
        val reuser = PortViewReuser(this)
        drawables.clear()
        clearPortViews()

        containerDrawing.fillSubGraphVerticeView(this)
        reuser.reuse()

        if (StringUtils.isNotEmpty(label)) {
            getLabelComponent()?.text = label!!
        }

        updateBoxes()
    }

	private fun fillDesignErrorRepresentation() {
		drawables.clear()
		drawables.add(BrokenReferenceView(rotation, styleProvider))
		updateBoxes()
	}

    private fun getLibraryContainerDrawing(): ContainerDrawing {
        val libraryGraph = repository.getMetaGraph(subGraphVertice!!.graphUUID!!)
        return storableCloner.clonePreservingIdentities(libraryGraph.containerDrawing, storableCreator) as ContainerDrawing
    }

    private fun updateBoxes() {
        _boundingBox.setFrame(0.0, 0.0, 0.0, 0.0)
        containsBox.setFrame(_boundingBox)
        addPortViewsTo(_boundingBox, containsBox)

        drawables.forEach {
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
        eventBus.post(OpenSubGraphRequest(this, quickMode = event.isAltDown))
    }

    private fun getActorViewAt(x: Double, y: Double): ActorView? {
	    val p = rotateBack(x, y).subtract(location)
        return drawables.filter {  it is ActorView && it.contains(p) }.map { it as ActorView }.firstOrNull()
    }

    private inner class DoubleClickHandler : InputEventHandlerAdapter<InputEventContext>() {
        override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
            if (context.mouseEvent!!.button == Button.BUTTON1 && context.mouseEvent!!.clickCount == 2) {
                requestOpenSubGraph(context.mouseEvent!!)
                return null
            }
            return super.mouseClicked(context)
        }
    }

    private inner class DoubleClickExecutionHandler: ClickableActorInteractionHandlerAdapter() {
        override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
	        val actorView = getActorViewAt(context.x, context.y)
	        return if (actorView?.getActorInteractionHandler(context) != null) {
		        actorView.getActorInteractionHandler(context)!!.mousePressed(context.withXY(context.x - location.x, context.y - location.y))
	        } else {
		        super.mousePressed(context)
	        }
        }
        override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
	        val actorView = getActorViewAt(context.x, context.y)
	        return if (actorView?.getActorInteractionHandler(context) != null) {
		        actorView.getActorInteractionHandler(context)!!.mouseReleased(context.withXY(context.x - location.x, context.y - location.y))
	        } else {
		        super.mouseReleased(context)
	        }
        }

        override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
	        if (context.mouseEvent!!.clickCount == 2) {
		        requestOpenSubGraph(context.mouseEvent!!)
		        return null
	        }
	        val actorView = getActorViewAt(context.x, context.y)
	        if (actorView?.getActorInteractionHandler(context) != null) {
		        return actorView.getActorInteractionHandler(context)!!.mouseClicked(context.withXY(context.x - location.x, context.y - location.y))
	        }
	        return null
        }
    }
}
