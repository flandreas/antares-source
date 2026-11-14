package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Grid
import io.antarescircuit.jabbah.edit.model.rectangle.ResizableRectangularComponent
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.*
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.io.*

/** Stable owner of ports and connections, with independently managed graph presentation. */
class SubGraphVerticeViewImpl(
	graphElement: SubGraphVerticeRef = SubGraphVerticeRef(),
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	repository: MetaGraphRepository = LibraryModule.libraryHolder,
	eventBus: EventBus = BaseModule.eventBus,
) : AbstractSubGraphVerticeView(graphElement, styleProvider, repository, eventBus), ResizableRectangularComponent {

	companion object {
		private val creatingGraphs = mutableSetOf<Any>()
	}

	private var presentation: EmbeddedSubGraphPresentation? = null
	private var signalHandler: SignalHandler? = null
	private var frameWidth = 0.0
	private var frameHeight = 0.0
	private var ready = true
	private var refreshingLayout = false
	private var containingGraph: GraphView? = null
	internal val embeddingGridDistance: Double? get() = (containingGraph?.snapper as? Grid)?.distance?.takeIf { it > 0 && it.isFinite() }

	override fun <T : Drawable> handleAdded(container: DrawableContainer<T>) {
		if (container is GraphView) containingGraph = container
		super.handleAdded(container)
	}

	override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
		if (container === containingGraph) containingGraph = null
		super.handleRemoved(container)
	}

	internal val graphWidth get() = frameWidth
	internal val graphHeight get() = frameHeight

	override fun dispose() {
		closePresentation()
		super.dispose()
	}

	override var displayMode: SubGraphDisplayMode = SubGraphDisplayMode.SYMBOL
		set(value) {
			if (field == value) return
			invalidate()
			closePresentation()
			field = value
			if (!isReading) ensurePresentation()
			update()
			invalidate()
		}

	internal fun mirrorPoint(p: Point2D) = Point2D(
		if (isHorizontallyMirrored) -p.x else p.x,
		if (isVerticallyMirrored) -p.y else p.y,
	)

	internal fun localPoint(p: Point2D) = mirrorPoint(rotateBack(p).subtract(location))

	/** ---- [ResizableRectangularComponent] */

	override val shape: MutableRectangularShape get() = Rectangle2D(location.x, location.y, frameWidth, frameHeight)

	override val minimumWidth get() = ensurePresentation()?.minimumWidth ?: 0.0

	override val minimumHeight get() = ensurePresentation()?.minimumHeight ?: 0.0

	override fun handleToWorld(p: Point2D): Point2D = rotation.rotatePointAround(location, mirrorPoint(p.subtract(location)).add(location))

	override fun worldToHandle(p: Point2D): Point2D = localPoint(p).add(location)

	override fun resizeFrame(x: Double, y: Double, width: Double, height: Double) {
		val origin = handleToWorld(Point2D(x, y))
		setFrame(origin.x, origin.y, width, height)
	}

	override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
		invalidate()
		frameWidth = maxOf(width, minimumWidth)
		frameHeight = maxOf(height, minimumHeight)
		location = Point2D(x, y)
		presentation?.layoutPorts()
		update()
		invalidate()
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)

		writer.writeString("displayMode", displayMode.customName)
		writer.writeDouble("graphWidth", frameWidth)
		writer.writeDouble("graphHeight", frameHeight)
	}

	override fun read(reader: StoreReader) {
		ready = false
		super.read(reader)

		if (reader.hasAttribute("graphWidth")) {
			frameWidth = reader.readDouble("graphWidth")
		}
		if (reader.hasAttribute("graphHeight")) {
			frameHeight = reader.readDouble("graphHeight")
		}
		if (reader.hasAttribute("displayMode")) {
			displayMode = SubGraphDisplayMode.withName(reader.readString("displayMode"))
		}
	}

	override fun getPortConnectionPoint(port: Port<*>?): Point2D {
		ensurePresentation()
		return super.getPortConnectionPoint(port)
	}

	override fun getUnconnectedPortConnectionPoint(port: Port<*>): Point2D {
		ensurePresentation()
		return super.getUnconnectedPortConnectionPoint(port)
	}

	/** ---- [AbstractVerticeView] */

	override fun <T : InputEventContext> getExecutionTooltip(context: T): Tooltip? =
		ensurePresentation()?.tooltip(context) ?: super.getExecutionTooltip(context)

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		ensurePresentation()?.interactionHandler ?: super.getActorInteractionHandler(context)

	override fun modelExchanged(oldModel: SubGraphVerticeRef?) {
		closePresentation()
		super.modelExchanged(oldModel)
	}

	/** ---- [AbstractSubGraphVerticeView] */

	override fun getBoundingBoxImpl(): Rectangle2D {
		val p = ensurePresentation() ?: return super.getBoundingBoxImpl()
		return p.bounds()
	}

	override fun contains(x: Double, y: Double): Boolean = ensurePresentation()?.let {
		Rectangle2D(0.0, 0.0, frameWidth, frameHeight).contains(localPoint(Point2D(x, y))) || getPortViewAt(x, y) != null
	} ?: super<AbstractSubGraphVerticeView>.contains(x, y)

	override fun drawImpl(context: DrawContext) {
		val p = ensurePresentation()
		if (p == null) super.drawImpl(context) else {
			drawImplBeforeBorder(context)
			p.draw(context)
			drawImplAfterBorder(context)
		}
	}

	override fun drawWithDrawableDrawer(context: DrawContext, drawableDrawer: (Drawable) -> Unit) {
		if (ensurePresentation() == null) super.drawWithDrawableDrawer(context, drawableDrawer)
		else draw(context)
	}

	override fun bind(graphView: GraphView, deep: Boolean) {
		closePresentation()
		containingGraph = graphView
		super.bind(graphView, deep)
		ready = true
		ensurePresentation()
	}

	override fun allResolutionDone() {
		ready = true
		ensurePresentation()
		super<AbstractSubGraphVerticeView>.allResolutionDone()
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		this.signalHandler = signalHandler
		super.executionStarted(signalHandler)
		val existing = presentation
		if (existing != null) existing.graph.executionStart(signalHandler) else ensurePresentation()
	}

	override fun executionStartDone(signalHandler: SignalHandler) {
		super.executionStartDone(signalHandler)
		presentation?.graph?.executionStartDone(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		presentation?.graph?.executionStop(signalHandler)
		this.signalHandler = null
		super.executionStopped(signalHandler)
	}

	override fun simulationEventRequiresRepaint(scheduler: Scheduler): Boolean =
		presentation != null || super.simulationEventRequiresRepaint(scheduler)

	override fun mirrorHorizontally(x: Double) {
		super.mirrorHorizontally(x)
		presentation?.layoutPorts()
		update()
	}

	override fun mirrorVertically(y: Double) {
		super.mirrorVertically(y)
		presentation?.layoutPorts()
		update()
	}

	internal fun handleGraphBackgroundClick(context: ActorInteractionContext) {
		val port = getPortViewAt(context.x, context.y)
		if (port != null && !port.port.isConnected && port.port.portType.isInput) port.handleExecutionClick(context)
		else if (context.mouseEvent?.clickCount == 2) requestOpenSubGraph(context.mouseEvent!!)
	}

	private fun closePresentation() {
		val previous = presentation
		presentation = null
		previous?.let { p ->
			signalHandler?.let { p.graph.executionStop(it) }
			p.dispose()
		}
	}

	private fun ensurePresentation(): EmbeddedSubGraphPresentation? {
		if (!ready || displayMode != SubGraphDisplayMode.GRAPH || model.isBroken || model.designError != null) return null
		if (presentation == null) {
			val graphId = model.graphUUID ?: return null
			if (!creatingGraphs.add(graphId)) return null
			try {
				presentation = EmbeddedSubGraphPresentation(this, createSubGraphView(signalHandler))
			} finally {
				creatingGraphs.remove(graphId)
			}
		}
		val p = presentation!!
		if (!refreshingLayout && p.needsLayout()) {
			refreshingLayout = true
			try {
				invalidate()
				frameWidth = if (frameWidth <= 0) p.preferredWidth else maxOf(frameWidth, p.minimumWidth)
				frameHeight = if (frameHeight <= 0) p.preferredHeight else maxOf(frameHeight, p.minimumHeight)
				p.layoutPorts()
				update()
				invalidate()
			} finally {
				refreshingLayout = false
			}
		}
		return p
	}
}
