package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.resettableLazy
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.draw.drawable.TransparentImpl
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Graphics2D
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.edit.model.text.description.Describable
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewConnectionGeometry
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.port.PortView.Companion.PROP_SENSITIVE_AREA
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StorableCloner

/**
 * Abstract base implementation of the [VerticeView] interface.
 */
abstract class AbstractVerticeView<T : Vertice>(
	styleProvider: StyleProvider,
	model: T
) : AbstractGraphElementView<T>(styleProvider, GraphStyleType.VERTICE, model), VerticeView<T>, Describable, Transparent {

	companion object {

		fun cannotOpenMsg(c: Component) {
			BaseModule.eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = c, messageKey = "graph.vertice.cannotOpen.msg"))
		}

		protected object CannotOpenClickHandler : InputEventHandlerAdapter<InputEventContext>() {
			var component: Component? = null
			override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
				if (context.mouseEvent?.button == Button.BUTTON1 && context.mouseEvent?.clickCount == 2) {
					cannotOpenMsg(component!!)
				}
				return null
			}
		}

		open class CannotOpenActorClickHandler : InputEventHandlerAdapter<ActorInteractionContext>() {
			var component: Component? = null

			override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
				when (context.mouseEvent?.clickCount) {
					1 -> {
						if (component is VerticeView<*>) {
							(component as VerticeView<*>).getPortViewAt(context.x, context.y)?.let { pv ->
								if (!pv.port.isConnected && pv.port.portType.isInput) {
									pv.handleExecutionClick(context)
								}
							}
						}
					}
					2 -> cannotOpenMsg(component!!)
				}
				return null
			}
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
	private val executionInfoLabel = Label(
		text = "",
		font = font,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.BOTTOM)

	/** Caches the [Tooltip] (if any) created by [getTooltip]. */
	protected val tooltip = resettableLazy {
		buildVerticeViewTooltipText()?.let {
			Tooltip(it, plainBoundingBox.centerX, plainBoundingBox.maxY)
		}
	}

	protected val executionTooltip = resettableLazy {
		buildToolTipText(type, typeDesc, executionTooltipSubtext)?.let {
			Tooltip(it, plainBoundingBox.centerX, plainBoundingBox.maxY)
		}
	}

	/** ---- [Cloneable] interface */

	override fun doClone(): Component = StorableCloner.clone(VerticeViewStorable(this)).verticeView!!

	/** ---- [Describable] */

	override var description: Description
		get() = model.description
		set(value) {
			model.description = value
			tooltip.reset()
		}

	/** ---- [GraphElementView] */

	override val isFullyConnected: Boolean get() = model.isFullyConnected

	/** ---- [VerticeView] */

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

	@Suppress("UNCHECKED_CAST")
	override fun <G : Any> getPortView(port: Port<G>): PortView<G>? {
		return portViews.filter { it.port == port }.map { it as PortView<G> }.firstOrNull()
	}

	override fun getPort(portId: Int): Port<*>? {
		if (model.designError != null) {
			return null
		}
		return model.getPort<Any>(portId)
	}

	override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?, geometry: EdgeViewConnectionGeometry) {
		if (port != null) {
			getPortView(port)?.let {
				invalidate()
				it.handleConnect(edgeView, geometry)
				invalidate()
			}
		}
	}

	override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?, lockEndpoint: Boolean) {
		if (port != null) {
			getPortView(port)?.let {
				invalidate()
				it.handleUnconnect(edgeView, lockEndpoint)
				invalidate()
			}
		}
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

	override fun getUnconnectedPortConnectionPoint(port: Port<*>): Point2D {
		return rotate(getPortView(port)!!.unconnectedConnectionPoint.add(location))
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

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX> get() =
		if (portViews.isEmpty()) super.snappableX else portViews.toTypedArray()

	override val snappableY: Array<SnappableY> get() =
		if (portViews.isEmpty()) super.snappableY else portViews.toTypedArray()

	/** ---- [Drawable] */

	private val plainBoundingBox get() = rotate(getBoundingBoxImpl())

	override val boundingBox: RectangularShape
		get() {
			val bbox = plainBoundingBox
			if (isExecutionInfoDrawn(requiredBySystemSpeed = true, isPausing = true)) {
				configureExecutionInfoLabel()
				bbox.add(executionInfoLabel.boundingBox)
			}
			return bbox
		}

	override fun <T: InputEventContext> getTooltip(context: T): Tooltip? {
		val portView = getPortViewAt(context.x, context.y)
		if (portView != null) {
			return portView.getTooltip(context)
		}
		return tooltip.value?.also { it.sourceRect = plainBoundingBox }
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

	override fun handleStateChanged(event: GraphElementEvent) {
		if (event.signalHandler is Scheduler && (event.reason == Vertice.STATE_CHANGE_INPUT || event.reason == Vertice.STATE_CHANGE_OUTPUT)) {
			if (!simulationEventRequiresRepaint(event.signalHandler)) {
				// Avoid leading to unnecessary View repainting
				return
			}
		}
		super.handleStateChanged(event)
	}

	protected open fun simulationEventRequiresRepaint(scheduler: Scheduler): Boolean =
		GraphApplicationContext.isShowNetState(scheduler)

	/** ---- [ActorView] interface */

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? {
		val portTooltip = getPortViewAtConnectionPoint(context.location)?.getExecutionTooltip(context)
		if (portTooltip != null) {
			return portTooltip
		}
		return executionTooltip.value?.also { it.sourceRect = plainBoundingBox }
	}

	protected open val executionTooltipSubtext: String? get() = description.value

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		VerticeViewActorInteractionHandler.getInactiveInstance(this)

	override fun executionStarted(signalHandler: SignalHandler) { }

	override fun executionStopped(signalHandler: SignalHandler) { }

	/** ---- [AbstractVerticeView] */

	/** Returns the unrotated bounding box in absolute view coordinates.*/
	protected abstract fun getBoundingBoxImpl(): RectangularShape

	protected open fun buildVerticeViewTooltipText(): String? =
		if (description.isNotEmpty) {
			buildToolTipText(type, description.value, typeDesc)
		} else {
			buildToolTipText(type, typeDesc, null)
		}

	protected fun clearPortViews() {
		portViews.forEach { it.dispose() }
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
	protected fun addPortViewsTo(boundingBox: MutableRectangularShape, containsBox: MutableRectangularShape?) {
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
	protected fun addPortViewTo(portView: PortView<*>, boundingBox: MutableRectangularShape, containsBox: MutableRectangularShape?) {
		val outset = BaseModule.properties.getInt(PROP_SENSITIVE_AREA)
		val bb = Rectangle2D(portView.boundingBox)

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
	 * Basic drawing method that only draws all [PortView]s of this [VerticeView].
	 *
	 * When this method gets called, the [DrawContext] is translated to the location and rotated to the rotation
	 * angle of this [AbstractVerticeView].
	 *
	 * Subclasses will extends this method in order to draw their individual look. Note that this method is typically
	 * called by [AbstractVerticeView.draw], which prepares a setup for location and rotation independent drawing of
	 * this method.
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

	private fun drawImpl(context: DrawContext, drawPortViews: Boolean) {
		val oldStylable = context.stylable
		context.stylable = this
		if (drawPortViews) {
			context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke
			portViews.forEach { it.draw(context) }
		}
		context.stylable = oldStylable
	}

	private fun drawImpl(context: DrawContext, drawPortViews: Boolean, beforeBorder: Boolean) {
		val oldStylable = context.stylable
		context.stylable = this
		if (drawPortViews) {
			context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke
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
	override fun draw(context: DrawContext, drawer: (DrawContext) -> Unit) {
		super.draw(context, drawer)

		// Draw propagation delay above bounding box if in waiting state
		val appContext = context.castedAppContext<GraphApplicationContext>()!!
		if (isExecutionInfoDrawn(appContext.systemSpeedCategory.systemSpeedCategory == SystemSpeedCategory.Explore, appContext.isPausing)) {
			configureExecutionInfoLabel()
			executionInfoLabel.draw(context)
		}
	}

	protected fun getApplicableForegroundColor(context: DrawContext): Color {
		return if (context.useContextColors) {
			transparent.applyTo(context.color!!.foregroundColor)
		} else {
			transparent.applyTo(foregroundColor)
		}
	}

	protected open fun getApplicableBackgroundColor(context: DrawContext): Color {
		return if (context.useContextColors) {
			transparent.applyTo(context.color!!.backgroundColor)
		} else {
			transparent.applyTo(backgroundColor)
		}
	}

	private fun isExecutionInfoDrawn(requiredBySystemSpeed: Boolean, isPausing: Boolean): Boolean =
		requiredBySystemSpeed && model.waiting && isPausing && (this !is ControlView<*> || !this.isActiveControlView)

	/**
	 * Configures and updates the [Label] that displays the execution info text above the bounding box.
	 * Since [AbstractVerticeView] doesn't update itself when the [Vertice]'s execution state has changed,
	 * this method must always be called before using [executionInfoLabel].
	 */
	private fun configureExecutionInfoLabel() {
		val bbox = plainBoundingBox
		val text = "${propagationDelay.value} ns"
		val style = Themes.get<GraphTheme>().annotation
		executionInfoLabel.font = style.font
		executionInfoLabel.text = text
		executionInfoLabel.color = transparent.applyTo(style.color.textColor)
		executionInfoLabel.location = Point2D(bbox.centerX.toInt(), bbox.minY.toInt() - 3)
	}

	protected fun rotate(rect: RectangularShape): Rectangle2D {
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
	protected fun rotateBack(x: Double, y: Double): Point2D =
		if (rotation == Rotation.R0) {
			Point2D(x, y)
		} else {
			rotation.inverse().rotatePointAround(location, x, y)
		}

	protected fun rotateBack(p: Point2D): Point2D =
		if (rotation == Rotation.R0) {
			p
		} else {
			rotation.inverse().rotatePointAround(location, p)
		}

	/** Rotates the specified [Direction] by the [Rotation] of this [VerticeView].*/
	private fun rotate(direction: Direction): Direction {
		return rotation.rotateDirection(direction)
	}

	/** Returns all [PortView]s of this [VerticeView] whose [Port]s are connected to the specified [Net].*/
	private fun getNetPortViews(net: Net<*>): Collection<PortView<*>> = portViews.filter { it.port.net === net }
}