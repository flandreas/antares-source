package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

/**
 * A [Component] that wraps a [ControlView] in order to allow deferred reference to a [SubGraphVerticeView]'s model.
 */
class ControlViewComponent(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    controlView: ControlView<Vertice>? = null
) : AbstractComponent(styleProvider), ActorView {

    var controlView: ControlView<Vertice>? = controlView

    /**
     * The ID of the model displayed by [controlView]. This ID is made persistent and is used to resolve the link
     * to the model when the underlying [Graph] gets bound.
     */
    private var controlModelId: Int = if (controlView != null) controlView!!.model!!.id else 0

    private var drawableOwner: DrawableOwner? = null

    init {
        if (controlView != null) {
            drawableOwner = DrawableOwner(this, controlView)
        }
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeStorable("controlView", controlView!!);
        writer.writeInt("controlModelId", controlModelId);
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (drawableOwner != null) {
            drawableOwner!!.dispose()
        }
        controlView = reader.readStorable("controlView") as ControlView<Vertice>
        drawableOwner = DrawableOwner(this, controlView!!)
        controlModelId = reader.readInt("controlModelId")
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

    override fun getActorInteractionHandler(): ActorInteractionHandler? = controlView!!.getActorInteractionHandler()

    override fun getExecutionToolTipText(x: Double, y: Double, width: Int?): String?
        = controlView!!.getExecutionToolTipText(x, y, width)

    /** ---- [ControlViewComponent] */

    fun bindToGraph(graph: Graph) {
        controlView!!.bindToModel(graph.withId(controlModelId) as Vertice)
    }
}