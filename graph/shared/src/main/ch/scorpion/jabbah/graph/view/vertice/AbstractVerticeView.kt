package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.Transparent.Companion.FULLY_OPAQUE
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.PortView.Companion.PROP_SENSITIVE_AREA
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Abstract base implementation of the [VerticeView] interface.
 */
abstract class AbstractVerticeView<T : Vertice>(
    styleProvider: StyleProvider,
    baseResourceKey: String,
    model: T?
): AbstractGraphElementView<T>(styleProvider, GraphStyleType.VERTICE, model), VerticeView<T>, Transparent {

    companion object {
        private fun cannotOpenMsg(c: Component) {
            BaseModule.eventBus.post(ComponentMessage(source = c, messageKey="graph.vertice.cannotOpen.msg"))
        }
    }

    /** Holds the graphical representations of all the model's [Port]s.*/
    private val portViews: MutableList<PortView<*>> = mutableListOf()

    /**
     * Determines whether this [VerticeView] stores its [PortView]s in terms of [Storable], or
     * whether they are statically created while constructing this [AbstractVerticeView] (or its subclass).
     *
     * This implementation returns `false` by default. Might be overwritten by subclasses.
     * @return `true` if this [AbstractVerticeView] stores its [PortView]s.
     */
    private val arePortViewsStored: Boolean get() = false

    /** Displays information while the model of this [VerticeView] is executed.*/
    private val exectionInfoLabel = Label(
            text = "",
            font = font,
            horizontalAlignment = Label.HorizontalAlignment.CENTER,
            verticalAlignment = Label.VerticalAlignment.BOTTOM)

    /** ---- [VerticeView] interface */

    override val shortDescription: String? = Translations.getOptionalString("$baseResourceKey.desc")

    override var isShowPortViews: Boolean = true
        set(value) {
            if (value == field) {
                return
            }
            invalidate()
            field = value
            invalidate()
            update()
        }

    override val portViewCount: Int get() = portViews.size

    override fun addPortView(portView: PortView<*>) {
        portViews.add(portView)
        portView.owner = this
    }

    override fun removePortView(portView: PortView<*>) {
        portViews.remove(portView)
        portView.owner = null
    }

    override fun getPortViews(): ImmutableList<PortView<*>> {
        return portViews.toImmutableList()
    }

    override fun getPortViewAt(x: Double, y: Double): PortView<*>? {
        val p = rotateBack(x, y)
        return portViews.firstOrNull { it.containsConnectionPoint(p.x - location.x, p.y - location.y) }
    }

    override fun <G : Any> getPortView(port: Port<G>): PortView<G>? {
        return portViews.filter { it.port == port }.map { it as PortView<G> }.firstOrNull()
    }

    override fun getPort(portId: Int): Port<*> {
        return model!!.getPort<Any>(portId)
    }

    override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?) {
        getPortView(port!!)?.let {
            invalidate()
            it.handleConnect(edgeView)
            invalidate()
        }
    }

    override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?) {
        getPortView(port!!)?.let {
            invalidate()
            it.handleUnconnect(edgeView)
            invalidate()
        }
    }

    /** ---- [ConnectableView] */

    override fun getPortConnectionPoint(port: Port<*>?): Point2D {
        return rotate(getPortView(port!!)!!.connectionPoint.add(location))
    }

    override fun getPortConnectionLayoutDirections(edgeView: EdgeView<*>, port: Port<*>?, refPoint: Point2D?): Set<Direction> {
        if (port != null) {
            return setOf(rotate(getPortView(port)!!.direction))
        }
        return emptySet()
    }

    /** ---- [Transparent] interface */

    override var transparency: Int = FULLY_OPAQUE
        set(value) {
            field = value
            invalidate()
        }

    protected fun getColorWithTransparency(color: Color): Color {
        return Color(color.red, color.green, color.blue, transparency)
    }

    /** ---- [Snappable] interface */

    override val snappableX: Array<SnappableX>
        get() {
            val list = mutableListOf<SnappableX>()
            model!!.getPorts()
                    .filter { !it.isConnected }
                    .forEach { list.add(getPortView(it)!!) }
            return list.toTypedArray()
        }

    override val snappableY: Array<SnappableY>
        get() {
            val list = mutableListOf<SnappableY>()
            model!!.getPorts()
                    .filter { !it.isConnected }
                    .forEach { list.add(getPortView(it)!!) }
            return list.toTypedArray()
        }

    private fun getUnconnectedPortConnectionPoint(port: Port<*>): Point2D {
        return rotate(getPortView(port)!!.unconnectedConnectionPoint.add(location))
    }

    /** ---- [Drawable] */

    private val plainBoundingBox get() = rotate(getBoundingBoxImpl())

    override val boundingBox: Rectangle2D
        get() {
            val bbox = plainBoundingBox
            if (isExecutionInfoDrawn()) {
                bbox.add(exectionInfoLabel.boundingBox)
            }
            return bbox
        }

    override fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            val iterator = portViews.iterator()
            while (iterator.hasNext()) {
                if (!iterator.next().accept(visitor)) {
                    break
                }
            }
        }
        return visitor.visitLeave(this)
    }

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        val portView = getPortViewAt(x, y)
        if (portView != null) {
            return portView.getToolTipText(x, y, width)
        }
        return buildToolTipText(type, shortDescription, width)
    }

    override fun draw(context: DrawContext) {
        draw(context, {drawImpl(it)})
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return object : InputEventHandlerAdapter<InputEventContext>() {
            override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
                if (context.mouseEvent?.clickCount == 2) {
                    cannotOpenMsg(this@AbstractVerticeView)
                }
                return null
            }
        }
    }

    /** ---- [Component] interface */

    override val type: String? = Translations.getString("$baseResourceKey.name")

    override val rotatable: Boolean get() = true

    override var rotation: Rotation
        get() = super.rotation
        set(value) {
            super.rotation = value
            portViews.forEach { it.ownerRotation = value }
        }

    /** ---- [AbstractGraphElementView] */

    override fun modelExchanged(oldModel: T?) {
        super.modelExchanged(oldModel)
        if (!arePortViewsStored) {
            clearPortViews()
        }
    }

    /** ---- [ActorView] interface */

    override fun getExecutionToolTipText(x: Double, y: Double, width: Int?): String? {
        return getPortViewAt(x, y)?.getExecutionToolTipText(x, y, width) ?: buildToolTipText(type, shortDescription, width)
    }

    override fun getActorInteractionHandler(): ActorInteractionHandler? {
        return object : ActorInteractionHandlerAdapter() {
            override fun mouseClicked(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
                if (event.clickCount == 2) {
                    cannotOpenMsg(this@AbstractVerticeView)
                }
            }
        }
    }

    /** ---- [AbstractVerticeView] */

    /** Returns the unrotated bounding box in absolute view coordinates.*/
    protected abstract fun getBoundingBoxImpl(): Rectangle2D

    protected fun clearPortViews() {
        portViews.clear()
    }

    /**
     * Convenience method for getting the [PortView] of the first [InputPort].
     * @throws NoSuchElementException if not exists
     */
    protected fun getInput(): PortView<*> {
        return getPortView(model!!.getInput<Any>())!!
    }

    /**
     * Convenience method for getting the [PortView] of the first [OutputPort].
     * @throws NoSuchElementException if not exists
     */
    protected fun getOutput(): PortView<*> {
        return getPortView(model!!.getOutput<Any>())!!
    }

    /** Returns the [PortView]s that point to the specified [Direction].*/
    protected fun getPortViewsOfDirection(direction: Direction): ImmutableList<PortView<*>> {
        return portViews.filter { it.direction == direction }.toCollection(mutableListOf()).toImmutableList()
    }

    /** Adds all [PortView]s to the overall bounding box and the box used for 'contains' calculation.*/
    protected fun addPortViewsTo(boundingBox: Rectangle2D, containsBox: Rectangle2D?) {
        containsBox?.setFrame(boundingBox)
        for (pv in portViews) {
            addPortViewTo(pv, boundingBox, containsBox)
        }
    }

    /**
     * Adds the bounding box of the specified [PortView] to the overall bounding box and the box
     * used for 'contains' calculation.
     */
    protected fun addPortViewTo(portView: PortView<*>, boundingBox: Rectangle2D, containsBox: Rectangle2D?) {
        val outset = BaseModule.properties.getInt(PROP_SENSITIVE_AREA)
        val bb = portView.boundingBox

        bb.setFrame(location.x + bb.x, location.y + bb.y, bb.width, bb.height)
        boundingBox.add(bb)

        containsBox?.let {
            it.add(bb)
            it.add(
                location.x + portView.connectionPoint.x + outset * portView.direction.dx,
                location.y + portView.connectionPoint.y + outset * portView.direction.dy)
        }
    }

    /**
     * Calls [drawImpl(DrawContext,Boolean)] and uses the property [isShowPortViews] to determine
     * whether the [PortView]s are to be drawn.
     */
    protected open fun drawImpl(context: DrawContext) {
        drawImpl(context, isShowPortViews)
    }

    /**
     * Basic drawing method that only draws all [PortView]s of this [VerticeView].
     *
     * When this method gets called, the [DrawContext] is translated to the location and rotated to the rotation
     * angle of this [AbstractVerticeView].
     *
     * Subclasses will extends this method in order to draw their individual look. Note that this method is typically
     * called by [AbstractVerticeView.draw], which prepares a setup for location and rotation independent drawing of
     * this method.
     */
    protected open fun drawImpl(context: DrawContext, drawPortViews: Boolean) {
        if (drawPortViews) {
            portViews.forEach { it.draw(context) }
        }
    }

    /**
     * Drawing wrapper method that prepares a setup for location and rotation independent drawing of custom drawing code.
     *
     * This method translates the [Graphics2D] context to the location of this [VerticeView] and also
     * rotates it to the current [Rotation].
     * @param context the {@link DrawContext} to be used for drawing
     * @param drawer the code that effectively draws content within the prepared translation and rotation context.
     */
    fun draw(context: DrawContext, drawer: (DrawContext) -> Unit) {
        val oldColor = context.g.color

        context.g.translate(location.x, location.y)
        context.g.rotate(rotation.angle)

        drawer.invoke(context)

        context.g.rotate(-rotation.angle)
        context.g.translate(-location.x, -location.y)

        // Draw propagation delay above bounding box if in waiting state
        if (isExecutionInfoDrawn()) {
            configureExecutionInfoLabel()
            exectionInfoLabel.draw(context)
        }

        // DEBUG BEGIN
//        context.g.color = Color.RED
//        context.g.stroke = Stroke(0.5f)
//        context.g.draw(boundingBox)
//        context.g.fillOval((location.x - 2).toInt(), (location.y - 2).toInt(), 4, 4)
        // DEBUG END

        context.g.color = oldColor
    }

    private fun isExecutionInfoDrawn(): Boolean {
        return model != null && model!!.waiting
    }

    /**
     * Configures and updates the [Label] that displays the execution info text above the bounding box.
     * Since [AbstractVerticeView] doesn't update itself when the [Vertice]'s execution state has changed,
     * this method must always be called before using [exectionInfoLabel].
     */
    private fun configureExecutionInfoLabel() {
        val bbox = plainBoundingBox
        val text = "${propagationDelay.toString()} ns"
        val style = Themes.get<GraphTheme>().annotation
        exectionInfoLabel.font = style.font
        exectionInfoLabel.text = text
        exectionInfoLabel.color = getColorWithTransparency(style.color.textColor)
        exectionInfoLabel.location = Point2D(bbox.centerX.toInt(), bbox.minY.toInt() - 3)
    }

    protected fun rotate(rect: Rectangle2D): Rectangle2D {
        return rotation.rotateRectangleAround(location, rect)
    }

    /** Rotates the specified point around the location of this [VerticeView] by its [Rotation]..*/
    private fun rotate(p: Point2D): Point2D {
        return rotate(p.x, p.y)
    }

    /** Rotates the specified (x,y) point around the location of this [VerticeView] by its [Rotation].*/
    private fun rotate(x: Double, y: Double): Point2D {
        return rotation.rotatePointAround(location, x, y)
    }

    /** Rotates the specified (x,y) point around the location of this [VerticeView] by the inverse of its [Rotation].*/
    private fun rotateBack(x: Double, y: Double): Point2D {
        return rotation.inverse().rotatePointAround(location, x, y)
    }

    /** Rotates the specified [Direction] by the [Rotation] of this [VerticeView].*/
    private fun rotate(direction: Direction): Direction {
        return rotation.rotateDirection(direction)
    }
}