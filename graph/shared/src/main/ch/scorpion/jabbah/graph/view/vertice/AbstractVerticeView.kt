package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ConnectableView
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.AutoConnectCommand
import ch.scorpion.jabbah.graph.view.editor.AutoConnector
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.port.PortView.Companion.PROP_SENSITIVE_AREA
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCloner

/**
 * Abstract base implementation of the [VerticeView] interface.
 */
abstract class AbstractVerticeView<T : Vertice>(
	styleProvider: StyleProvider,
	model: T
) : AbstractGraphElementView<T>(styleProvider, GraphStyleType.VERTICE, model), VerticeView<T>, Describable, Transparent {

	companion object {

		private fun cannotOpenMsg(c: Component) {
			BaseModule.eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = c, messageKey = "graph.vertice.cannotOpen.msg"))
		}

		private object CannotOpenClickHandler : InputEventHandlerAdapter<InputEventContext>() {
			var component: Component? = null
			override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
				if (context.mouseEvent?.button == Button.BUTTON1 && context.mouseEvent?.clickCount == 2) {
					cannotOpenMsg(component!!)
				}
				return null
			}
		}

		protected open class CannotOpenActorClickHandler : InputEventHandlerAdapter<ActorInteractionContext>() {
			var component: Component? = null
			override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
				if (context.mouseEvent?.clickCount == 2) {
					cannotOpenMsg(component!!)
				}
				return null
			}
		}

		private val CANNOT_OPEN_ACTOR_CLICK_HANDLER = CannotOpenActorClickHandler()
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
	private val executionInfoLabel = Label(
		text = "",
		font = font,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.BOTTOM)

	/** ---- [Storable] interface */

	override fun doClone(): Component {
		return StorableCloner.clone(VerticeViewStorable(this)).verticeView!!
	}

	/** ---- [Describable] */

	override var description: Description
		get() = model.description
		set(value) { model.description = value }

	/** ---- [VerticeView] interface */

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

	override fun getPortViewAtConnectionPoint(x: Double, y: Double): PortView<*>? {
		val p = rotateBack(x, y).subtract(location)
		portViews
			.minByOrNull { it.connectionPoint.distance(p) }
			?.let {
				if (it.containsConnectionPoint(p)) {
					return it
				}
			}
		return null
	}

	override fun getPortViewAt(x: Double, y: Double): PortView<*>? {
		val p = rotateBack(x, y).subtract(location)
		return portViews.firstOrNull { it.contains(p) || it.containsConnectionPoint(p) }
	}

	override fun <G : Any> getPortView(port: Port<G>): PortView<G>? {
		return portViews.filter { it.port == port }.map { it as PortView<G> }.firstOrNull()
	}

	override fun getPort(portId: Int): Port<*>? {
		if (model.designError != null) {
			return null
		}
		return model.getPort<Any>(portId)
	}

	override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?) {
		if (port != null) {
			getPortView(port)?.let {
				invalidate()
				it.handleConnect(edgeView)
				invalidate()
			}
		}
	}

	override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?) {
		if (port != null) {
			getPortView(port)?.let {
				invalidate()
				it.handleUnconnect(edgeView)
				invalidate()
			}
		}
	}

	override fun handleEdgeViewWidthChanged(edgeView: EdgeView<*>) {
		getNetPortViews(edgeView.model).forEach { it.edgeViewWidth = edgeView.width }
	}

	override fun drawDataFlow(inputName: String, outputName: String, context: DrawContext) {
		val inputPortView = getPortView(model.getInput<T>(inputName))!!
		val outputPortView = getPortView(model.getOutput<T>(outputName))!!

		inputPortView.prepareConnectionDrawContext(context)

		context.g.drawLine(inputPortView.locationX, inputPortView.locationY, outputPortView.locationX, outputPortView.locationY)
	}

	/** ---- [ConnectableView] */

	override val isConnectable: Boolean get() = model.designError == null

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

	protected val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) {
			transparent.transparency = value
			portViews.forEach { it.transparency = value }
		}

	/** ---- [Movable] interface */

	override fun dragged(editor: Editor) {
		super<AbstractGraphElementView>.dragged(editor)
		AutoConnector.handleDragged(editor, this)
	}

	override fun dragFinished(editor: Editor) {
		super<AbstractGraphElementView>.dragFinished(editor)
		AutoConnector.handleDragFinished(editor)
	}

	override fun getMoveCommand(editor: Editor, offset: Point2D): Command {
		val commands = AutoConnector.createAutoConnectCommands(editor, this)
		return if (commands.isEmpty()) {
			super<AbstractGraphElementView>.getMoveCommand(editor, offset)
		} else {
			AutoConnectCommand(editor, this.id, offset, commands)
		}
	}

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX>
		get() {
			val list = mutableListOf<SnappableX>()
			list.add(SnappableXCoordinate(location.x))
			getPortViews()
				.filter { !it.port.isConnected }
				.forEach { list.add(it) }
			return list.toTypedArray()
		}

	override val snappableY: Array<SnappableY>
		get() {
			val list = mutableListOf<SnappableY>()
			list.add(SnappableYCoordinate(location.y))
			getPortViews()
				.filter { !it.port.isConnected }
				.forEach { list.add(it) }
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
			if (isExecutionInfoDrawn(requiredBySystemSpeed = true, isPausing = true)) {
				bbox.add(executionInfoLabel.boundingBox)
			}
			return bbox
		}

	override fun getTooltip(x: Double, y: Double): Tooltip? {
		val portView = getPortViewAt(x, y)
		if (portView != null) {
			return portView.getTooltip(x, y)
		}
		val text = buildToolTipText(type, typeDesc, description.value)
		return if (StringUtils.isNotEmpty(text)) Tooltip(text!!, plainBoundingBox.centerX, plainBoundingBox.maxY) else null
	}

	override fun draw(context: DrawContext) {
		draw(context) { drawImpl(it) }
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		CannotOpenClickHandler.component = this
		return CannotOpenClickHandler
	}

	/** ---- [Component] interface */

	override val type: String get() = model.type

	override val typeDesc: String? get() = model.typeDesc

	override val useRotation: Boolean get() = true

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

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? {
		val portTooltip = getPortViewAtConnectionPoint(x, y)?.getExecutionTooltip(x, y)
		if (portTooltip != null) {
			return portTooltip
		}
		val text = buildToolTipText(type, typeDesc, executionTooltipSubtext)
		return if (StringUtils.isNotEmpty(text)) Tooltip(text!!, plainBoundingBox.centerX, plainBoundingBox.maxY) else null
	}

	protected open val executionTooltipSubtext: String? get() = description.value

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		CANNOT_OPEN_ACTOR_CLICK_HANDLER.component = this
		return CANNOT_OPEN_ACTOR_CLICK_HANDLER
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
		return getPortView(model.getInput())!!
	}

	/**
	 * Convenience method for getting the [PortView] of the first [OutputPort].
	 * @throws NoSuchElementException if not exists
	 */
	protected fun getOutput(): PortView<*> {
		return getPortView(model.getOutput())!!
	}

	/** Returns the [PortView]s that point to the specified [Direction].*/
	protected fun getPortViewsOfDirection(direction: Direction): ImmutableList<PortView<*>> {
		return portViews.filter { it.direction == direction }.toCollection(mutableListOf()).toImmutableList()
	}

	/** Adds all [PortView]s to the overall bounding box and the box used for 'contains' calculation.*/
	protected fun addPortViewsTo(boundingBox: Rectangle2D, containsBox: Rectangle2D?) {
		containsBox?.setFrame(boundingBox)
		if (isShowPortViews) {
			for (pv in portViews) {
				addPortViewTo(pv, boundingBox, containsBox)
			}
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

	protected fun drawImplBeforeBorder(context: DrawContext) {
		drawImpl(context, isShowPortViews, beforeBorder = true)
	}

	protected fun drawImplAfterBorder(context: DrawContext) {
		drawImpl(context, isShowPortViews, beforeBorder = false)
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
		val oldStylable = context.stylable
		context.stylable = this
		if (drawPortViews) {
			portViews.forEach { it.draw(context) }
		}
		context.stylable = oldStylable
	}

	private fun drawImpl(context: DrawContext, drawPortViews: Boolean, beforeBorder: Boolean) {
		val oldStylable = context.stylable
		context.stylable = this
		if (drawPortViews) {
			portViews.forEach { if (beforeBorder) it.drawBelowOwner(context) else it.drawAboveOwner(context) }
		}
		context.stylable = oldStylable
	}

	/**
	 * Drawing wrapper method that prepares a setup for location and rotation independent drawing of custom drawing code.
	 *
	 * This method translates the [Graphics2D] context to the location of this [VerticeView] and also
	 * rotates it to the current [Rotation].
	 * @param context the [DrawContext] to be used for drawing
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
		val appContext = context.castedAppContext<GraphApplicationContext>()!!
		if (isExecutionInfoDrawn(appContext.systemSpeedCategory.systemSpeedCategory == SystemSpeedCategory.Explore, appContext.isPausing)) {
			configureExecutionInfoLabel()
			executionInfoLabel.draw(context)
		}

		DrawModule.drawLocatableDebugBoundingBox(this, context)

		context.g.color = oldColor
	}

	protected fun getApplicableForegroundColor(context: DrawContext): Color {
		return if (context.useContextColors) {
			transparent.applyTo(context.color!!.foregroundColor)
		} else {
			transparent.applyTo(foregroundColor)
		}
	}

	protected fun getApplicableBackgroudColor(context: DrawContext): Color {
		return if (context.useContextColors) {
			transparent.applyTo(context.color!!.backgroundColor)
		} else {
			transparent.applyTo(backgroundColor)
		}
	}

	private fun isExecutionInfoDrawn(requiredBySystemSpeed: Boolean, isPausing: Boolean): Boolean {
		return requiredBySystemSpeed && model.waiting && isPausing
	}

	/**
	 * Configures and updates the [Label] that displays the execution info text above the bounding box.
	 * Since [AbstractVerticeView] doesn't update itself when the [Vertice]'s execution state has changed,
	 * this method must always be called before using [executionInfoLabel].
	 */
	private fun configureExecutionInfoLabel() {
		val bbox = plainBoundingBox
		val text = "$propagationDelay ns"
		val style = Themes.get<GraphTheme>().annotation
		executionInfoLabel.font = style.font
		executionInfoLabel.text = text
		executionInfoLabel.color = transparent.applyTo(style.color.textColor)
		executionInfoLabel.location = Point2D(bbox.centerX.toInt(), bbox.minY.toInt() - 3)
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
	protected fun rotateBack(x: Double, y: Double): Point2D {
		return rotation.inverse().rotatePointAround(location, x, y)
	}

	/** Rotates the specified [Direction] by the [Rotation] of this [VerticeView].*/
	private fun rotate(direction: Direction): Direction {
		return rotation.rotateDirection(direction)
	}

	/** Returns all [PortView]s of this [VerticeView] whose [Port]s are connected to the specified [Net].*/
	private fun getNetPortViews(net: Net<*>): Collection<PortView<*>> = portViews.filter { it.port.net === net }
}