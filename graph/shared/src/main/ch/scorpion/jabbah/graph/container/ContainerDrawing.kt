package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.collection.ConcatIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeImpl
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.graph.model.Graph


/**
 * A [Drawing] that contains the graphical representation of a [SubGraphVertice]' outside view.
 */
class ContainerDrawing(
    private val storableCreator: StorableCreator = IOModule.storableCreator,
    private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
    private val eventBus: EventBus = BaseModule.eventBus,
    private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway,
    private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
    private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : DrawingImpl<Component>() {

    private val LOG by logger(ContainerDrawing::class)

    var model: SubGraphVertice = SubGraphVerticeImpl()
        private set

    /** ---- [DrawableContainer] interface */

    override fun add(drawable: Component, index: Int): DrawableContainer<Component> {
        super.add(drawable, index)
        if (readingFromStore) {
            return this
        }
        if (drawable is PortViewComponent<*>) {
            model.addPort(drawable.portView!!.port)
        }
        return this
    }

    override fun remove(drawable: Component): DrawableContainer<Component> {
        super.remove(drawable)
        if (drawable is PortViewComponent<*>) {
            model.removePort(drawable.portView!!.port)
        }
        return this
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeStorable("model", model)
    }

    override fun read(reader: StoreReader) {
        model = reader.readStorable("model") as SubGraphVertice
        super.read(reader)
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return ConcatIterator(super.getStorableChildren(), listOf(model).iterator())
    }

    /** ---- [ContainerDrawing] */

    /**
     * Complete this [DrawableContainer] with information from the corresponding [Graph].
     *
     * This method is called by [GraphStorable] after reading from persistent store. It completes
     * the model of this [DrawableContainer] with information from [Graph] objects, such as description
     * of [GraphPort]s. These informations are needed by [DrawableContainer] when filling
     * [SubGraphVerticeView]s for that [Graph].
     *
     * Of course, this method is not cool. It conflicts with the design goal that ]SubGraphVerticeView]
     * can be constructed soley with information from [DrawableContainer], without using the [Graph].
     * However, this goal may be out of reach anyway, and making information such as port description
     * reduntand in the [Graph] and the [DrawableContainer] (especially if they support I18N in the futuer!)
     * isn't cool either.
     */
    fun completeFromGraph(graph: Graph) {
        model.getPorts().forEach {
            it.description = graph.getGraphPort<Any>(it.name!!)!!.portDescription
        }
    }

    /**
     * Checks if the [SubGraphPort]s of all [PortViewComponent]s is the same instance as
     * the [SubGraphPort]s of the model [SubGraphVerticeViewImpl].
     */
    fun areSubGraphPortsConsistent(): Boolean {
        for (c in getPortViewComponents()) {
            val outer = c.portView!!.port
            val inner = model.getPort<Any>(outer.name!!)
            if (outer !== inner) {
                LOG.warn("ContainerDrawing: inconsistent SubGraphPort instances for port $outer")
                return false
            }
        }
        return true
    }

    /**
     * Initializes this [ContainerDrawing] with a default [RectangleComponent] and a default [OriginIndicator].
     * This is only needed when a new, fresh instance is created. It is not needed if this [ContainerDrawing] is
     * read from persistent storage, as the [RectangleComponent] and the [OriginIndicator] are then read
     * from storage.
     */
    fun initialize() {
        add(RectangleComponent(140.0, 140.0, 70.0, 140.0))
        add(OriginIndicator(x = 140.0, y = 140.0))
    }

    /**
     * Returns the [PortViewComponent] with the specified [Port] name.
     * @param portName the name of [Port] of the requested [PortViewComponent].
     */
    fun getPortViewComponent(portName: String): PortViewComponent<*>? {
        return getPortViewComponents().firstOrNull { it.portView!!.port.name == portName }
    }

    fun getControlViewComponent(controlId: String): ControlViewComponent? {
        return getControlViewComponents().firstOrNull{it.controlView!!.controlId == controlId}
    }

    fun createSubGraphVerticeView(): SubGraphVerticeView<SubGraphVerticeRef> {
        val model = SubGraphVerticeRef.fromSubGraphVertice(createSubGraphVertice(), storableCloner, libraryHolder, scriptGateway)
        val view = SubGraphVerticeViewImpl(model, styleProvider, storableCloner, storableCreator, libraryHolder, eventBus)
        fillSubGraphVerticeView(view)
        return view
    }

    /** Creates a copy of the [SubGraphVertice] model of this {@link ContainerDrawing}*/
    fun createSubGraphVertice(): SubGraphVertice {
        val subGraphVertice = storableCloner.cloneUsingCreator(this.model, storableCreator) as SubGraphVertice
        // The port descriptions are NOT copied when cloning, because they are not part of the persistent state
        // the Container's SubGraphVertice (see documentation of completeFromGraph()). Therefore, copy them now.
        subGraphVertice.getPorts().forEach {
            it.description = this.model.getPort<Any>(it.name!!).description
        }
        return subGraphVertice
    }

    /**
     * Fills the specified [SubGraphVerticeView] with all visible [Drawable]s of this
     * [ContainerDrawing], thus providing the look that has been designed by the library designer.
     */
    fun fillSubGraphVerticeView(view: SubGraphVerticeView<SubGraphVerticeRef>) {
        LOG.debug("ContainerDrawing: filling SubGraphVerticeViewRef name:${model.name} storableId:${model.storableId}, uuid:${model.graphUUID}")

        val clonedDrawing = storableCloner.cloneUsingCreator(this, storableCreator) as ContainerDrawing
        val origin = clonedDrawing.getOriginIndicator().location

        for (comp in clonedDrawing.getDrawables()) {
            comp.location = Point2D(comp.location.x - origin.x, comp.location.y - origin.y)
            if (comp is PortViewComponent<*>) {
                val portView = comp.portView as PortView<Any>
                view.addPortView(portView)
                try {
                    portView.port = view.model!!.getPort(portView.port.name!!)
                } catch (e: NoSuchElementException) {
                    LOG.error("SubGraphPort '${portView.port.name}' not found when filling SubGraphVerticeView for '${view.subGraphVertice!!.graphUUID}'")
                    throw e
                }
            } else if (comp !is OriginIndicator) {
                view.addDrawable(comp)
            }
        }
    }

    private fun getPortViewComponents(): ImmutableList<PortViewComponent<*>> {
        return getDrawables { it is PortViewComponent<*> }.map { it as PortViewComponent<*> }.toImmutableList()
    }

    private fun getControlViewComponents(): ImmutableList<ControlViewComponent> {
        return getDrawables { it is ControlViewComponent }.map { it as ControlViewComponent }.toImmutableList()
    }

    private fun getOriginIndicator(): OriginIndicator {
        return getDrawables { it is OriginIndicator }.first() as OriginIndicator
    }
}