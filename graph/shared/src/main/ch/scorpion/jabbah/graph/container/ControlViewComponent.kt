package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator

/**
 * A [Component] that wraps a [ControlView] in order to allow deferred reference to a [SubGraphVerticeView]'s model.
 */
class ControlViewComponent(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    var controlView: ControlView<Vertice>? = null,
    baseLink: DeepVerticeLink = DeepVerticeLink.EMPTY
) : AbstractComponent(styleProvider), ActorView {

	/**
     * The ID of the model displayed by [controlView]. This ID is made persistent and is used to resolve the link
     * to the model when the underlying [Graph] gets bound.
     */
	private var controlModelLink: DeepVerticeLink = if (controlView != null) baseLink.append(controlView!!.model!!.id) else DeepVerticeLink.EMPTY

    private var drawableOwner: DrawableOwner? = null

    init {
        if (controlView != null) {
            drawableOwner = DrawableOwner(this, controlView!!)
        }
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeStorable("controlView", controlView!!)
	    writer.writeString("controlModelId", controlModelLink.toStoreFormat())
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (drawableOwner != null) {
            drawableOwner!!.dispose()
        }
        controlView = reader.readStorable("controlView") as ControlView<Vertice>
        drawableOwner = DrawableOwner(this, controlView!!)
	    controlModelLink = DeepVerticeLink.fromStoreFormat(reader.readString("controlModelId"))
        super.read(reader)
        controlView!!.isShowPortViews = false
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return listOf(controlView!!).iterator()
    }

    /** ---- [Drawable] */

    override val boundingBox: RectangularShape get() = controlView!!.boundingBox

    override fun draw(context: DrawContext) {
        controlView!!.draw(context)
    }

    override fun contains(x: Double, y: Double): Boolean = controlView!!.contains(x, y)

    /** ---- [Locatable] */

    override var location: Point2D
        get() = controlView!!.location
        set(value) {controlView!!.location = value}

    /** ---- [Component] interface */

    override val type: String? get() = controlView!!.type

    override val selectableComponent: Component get() = controlView!!

    override val propertyOwner: Any get() = controlView!!

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = controlView!!.preferredSelectionDrawingStrategy
        set(value) {
            super.preferredSelectionDrawingStrategy = value
        }

    /** ---- [ActorView] */

    override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? = controlView!!.getActorInteractionHandler(context)

    override fun getExecutionTooltip(x: Double, y: Double): Tooltip? = controlView!!.getExecutionTooltip(x, y)

    /** ---- [ControlViewComponent] */

    fun bindToGraph(graph: Graph, repository: MetaGraphRepository, storableCreator: StorableCreator) {
	    val vertice = controlModelLink.getLinkedVertice(graph, repository, storableCreator)
	    controlView!!.bindToModel(vertice)
    }
}