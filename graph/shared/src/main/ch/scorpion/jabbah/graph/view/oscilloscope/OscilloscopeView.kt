package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.graphics.ReferenceColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ActorViewContainer
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.model.GenericGraphType
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.oscilloscope.SignalHistoriesType
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.GenericPortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class OscilloscopeView(
	graphType: GraphType = GenericGraphType,
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService,
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val factory: OscilloscopeViewFactory = GraphViewModule.oscilloscopeViewFactory,
	referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
	model: Oscilloscope = Oscilloscope(graphType = graphType),
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractRectangularVerticeView<Oscilloscope>(
	styleProvider,
	model,
	x = 0.0,
	y = 0.0,
	w = WIDTH.toDouble(),
	h = DEF_HEIGHT.toDouble()
) {

	companion object {

		/** The name of the [Boolean] property in [Properties] that determines whether to use reference colors for probes.*/
		const val PROP_INDIVIDUAL_PROBE_COLORS = "OscilloscopeView.individualProbeColors"

		private val LOG by logger(OscilloscopeView::class)

		private const val WIDTH = 700
		private const val DEF_HEIGHT = 200
		private const val INIT_SCALE = 1_000.0
		private const val RIGHT_INSET = 20
		const val TITLE_HEIGHT = 15
		const val MAX_ROW_NUMBER = 9
		const val ROW_INSET = 10
		const val ICON_BUTTON_SIZE = 20
		const val DRAWER_X = 3.0 * ROW_INSET + ICON_BUTTON_SIZE + OscilloscopeProbeViewIcon.SIZE

		private val CLOCKED_ANNOTATION = System.createPath()
			.moveTo(0, -5)
			.lineTo(-(ROW_INSET - 2), 0)
			.lineTo(0, 5)
			.close()
	}

	/**
	 * The scale of the [OscilloscopeViewTimeline] changed by the user during simulation.
	 * Is preserved during multiple simulation runs.
	 */
	var timelineScale: Double
		get() = timeline.scale
		set(value) {
			invalidate()
			timeline.scale = value
			validate()
		}

	var mode: SignalHistoriesType
		get() = model.mode
		set(value) {
			invalidate()
			model.mode = value
			validate()
		}

	/** Changed by the user in edit mode. Persistently stored in the [GraphView].*/
	@Suppress("MemberVisibilityCanBePrivate") // Reflection
	var persistentTimelineScale: Double = INIT_SCALE
		set(value) {
			field = value
			timelineScale = field
		}

	/** Returns the height of the drawing area used by a [SignalHistoryDrawer] returned by [OscilloscopeViewFactory].*/
	val rowHeight: Int get() = factory.getRowHeight(model.graphType)

	/** Returns the number of rows of this [OscilloscopeView].*/
	val rowsCount: Int get() = rows.size

	/** Contains all the [OscilloscopeScaleRowView]s and the [OscilloscopeScaleRowView].*/
	private val container = ActorViewContainer<Drawable>(useLocation = true)

	private val rows = mutableListOf<OscilloscopeSignalRowView>()

	val signalRowViews: ImmutableList<OscilloscopeSignalRowView> get() = rows.toImmutableList()

	/** Depend on current model and its [GraphType]. */
	val scaleRowView by lazy { OscilloscopeScaleRowView(this, Point2D.ZERO, RIGHT_INSET, service, factory) }

	private val refColorSequence = referenceColorSequenceProvider.provide()

	private val removeListener = RemoveListener()

	/** Replaced if model changes when reading from persistent store.*/
	var timeline = OscilloscopeViewTimeline(INIT_SCALE, model::maxTime)

	private val applicationModeHandler: (ApplicationModeEvent) -> Unit = { applicationMode = it.applicationMode }

	private val probeNameHandler: (OscilloscopeProbeNameEvent) -> Unit = {
		if (containsProbeVerticeView(it.source)) {
			handle(it)
		}
	}

	var applicationMode: ApplicationMode = ApplicationMode.EDIT
		private set(value) {
			if (field != value) {
				field = value
				updateState()
			}
		}

	private val yAxisWidth: Double get() =
		rows.filter { it.yAxis != null }.maxOfOrNull { it.yAxis!!.preferredWidth.toDouble() } ?: 0.0

	val drawerWidth: Double get() = WIDTH - DRAWER_X - ROW_INSET - yAxisWidth

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(OscilloscopeProbeNameEvent::class, probeNameHandler)

		preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW

	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(probeNameHandler)
	}

	override fun modelExchanged(oldModel: Oscilloscope?) {
		super.modelExchanged(oldModel)
		timeline = OscilloscopeViewTimeline(timelineScale, model::maxTime)

		container.add(scaleRowView)
		adjustSize()

		DrawableOwner(this, container)

		updateState()
	}

	private fun updateState() {
		rows.forEach { it.updateState() }
		scaleRowView.updateState()
	}

	/** ---- [Drawable] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		container.getInputEventHandler(context)

	override fun getTooltip(x: Double, y: Double, editable: Boolean): Tooltip? =
		container.getTooltip(x, y) ?: super.getTooltip(x, y, editable)

	override fun <T : Drawable> handleAdded(container: DrawableContainer<T>) {
		super.handleAdded(container)
		container.addDrawableContainerListener(removeListener as DrawableContainerListener<T>)
	}

	override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
		super.handleRemoved(container)
		container.removeDrawableContainerListener(removeListener as DrawableContainerListener<T>)
	}

	/** ---- [Component] */

	override val copyable: Boolean get() = false

	override val deleteBuddies: List<Component> get() = rows.mapNotNull { it.probeView.verticeView }

	/** ---- [AbstractVerticeView] */

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? =
		container.getExecutionTooltip(x, y) ?: super.getExecutionTooltip(x, y)

	override fun handleStateChanged(event: GraphElementEvent) {
		if (event.signalHandler != null && event.reason == Oscilloscope.SIGNAL_RECEIVED) {
			handleSignalReceived()
		}
		super.handleStateChanged(event)
	}

	private fun handleSignalReceived() {
		scaleRowView.timelineView.updateGeometry()
	}

	/** ---- [AbstractRectangularVerticeView] */

	override var location: Point2D
		get() = super.location
		set(value) {
			super.location = value
			container.location = value
		}

	override fun draw(context: DrawContext) {
		super.draw(context)

		context.translated(location) {
			it.g.color = it.choose(color).backgroundColor
			it.g.fill(bounds)
			it.g.color = it.choose(color).foregroundColor
			it.g.stroke = stroke
			it.g.draw(bounds)
		}

		container.draw(context)

		// "Clocked" annotation
		if (rows.size >= 1 && model.mode == SignalHistoriesType.Clocked) {
			context.g.color = color.foregroundColor
			context.g.stroke = stroke
			context.translated(location.x + bounds.width, location.y + TITLE_HEIGHT + rowHeight / 2) {
				it.g.draw(CLOCKED_ANNOTATION)
			}
		}
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		container.getActorInteractionHandler(context)

	/** ---- [ActorView] */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		model.enabled = visible
		if (visible) {
			rows.forEach { it.bindDrawer() }
			scaleRowView.bindDrawer()
		}
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		rows.forEach { it.unbindDrawer() }
		scaleRowView.unbindDrawer()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("scale", persistentTimelineScale)
		writer.writeBoolean("visible", visible)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("scale")) {
			persistentTimelineScale = reader.readDouble("scale")
		}
		if (reader.hasAttribute("visible")) {
			visible = reader.readBoolean("visible")
		}
	}

	override fun allResolutionDone() {
		super.allResolutionDone()

		for (port in model.getPorts()) {
			addPortView(GenericPortView(port))
			addRowView(port.name!!)
		}

		scaleRowView.updateState()
		adjustSize()

		parent?.getDrawables{ it is OscilloscopeProbeVerticeView<*> }
			?.map { it as OscilloscopeProbeVerticeView<Any> }
			?.forEach { rows.find { row -> row.name == it.name }!!.loadedWith(it) }
	}

	/** ---- [OscilloscopeView] */

	fun addRow() {
		val name = createRowName(null, rows.size + 1)

		val port = portFactory.createOscilloscopeProbePort<Any>(name, model.graphType)
		model.addPort(port)
		addPortView(GenericPortView(port))

		invalidate()
		addRowView(name)
		scaleRowView.updateState()
		adjustSize()
	}

	private fun createRowName(target: OscilloscopeSignalRowView?, prefNameNumber: Int): String {
		if (rowWithName(prefNameNumber.toString()) === target) {
			return prefNameNumber.toString()
		}

		var nameNumber = prefNameNumber
		while (rowWithName(nameNumber.toString()) != null) {
			nameNumber++
		}
		return nameNumber.toString()
	}

	/** Finds the [OscilloscopeSignalRowView] with the specified name, if existing.*/
	fun rowWithName(name: String): OscilloscopeSignalRowView? = rows.firstOrNull { it.name == name }

	fun getRow(index: Int): OscilloscopeSignalRowView = rows[index]

	/** Removes the row with the specified rowNumber, starting with 1.*/
	fun removeRow(name: String) {
		val row = rowWithName(name)!!
		val rowIndex = rows.indexOf(row)
		rows.remove(row)
		container.remove(row)
		refColorSequence.free(row.color)
		findProbeViewInDrawing(row.name)?.let { (parent as DrawableContainer<Component>).remove(it) }

		val port = model.getPort<Any>(name)
		val portView = getPortView(port)
		removePortView(portView!!)
		model.removePort(port)

		rearrangeFromRowIndex(rowIndex)
		adjustSize()
	}

	fun removeLastRow() {
		removeRow(rows.last().name)
	}

	private fun adjustSize() {
		scaleRowView.updateLocation()
		invalidate()
		setBounds(0.0, 0.0, WIDTH.toDouble(), (TITLE_HEIGHT + rows.size * rowHeight + OscilloscopeScaleRowView.ROW_HEIGHT).toDouble())
		invalidate()
		update()
	}

	private fun addRowView(name: String) {
		val y = TITLE_HEIGHT + rows.size * rowHeight
		val yAxis = factory.createSignalHistoryYAxis(model.graphType)
		val rowView = OscilloscopeSignalRowView(
			this,
			name,
			Point2D(0, y),
			nextProbeColor,
			service,
			factory.createSignalHistoryDrawer(model.graphType, yAxis, RIGHT_INSET),
			yAxis)
		rows.add(rowView)
		container.add(rowView)
		updateRowViews()
	}

	private fun updateRowViews() {
		rows.forEach { it.updateGeometry() }
	}

	private val nextProbeColor: ReferenceColor get() = if (BaseModule.properties.getBoolean(PROP_INDIVIDUAL_PROBE_COLORS)) {
		refColorSequence.next()
	} else {
		ReferenceColor(color)
	}

	/**
	 * Rearranges the rows after the row with the specified row index (starting with 0) has been deleted.
	 * For example, if row with number 3 out of 5 has been deleted, the rows with former row number 4 and 5 now
	 * become row numbers 3 and 4, and their location is updated accordingly.
	 */
	private fun rearrangeFromRowIndex(rowIndex: Int) {
		for (i in rowIndex until rows.size) {
			rows[i].location = Point2D(rows[i].location.x, rows[i].location.y - rowHeight)
		}
		scaleRowView.updateLocation()
		scaleRowView.updateState()
	}

	private fun findProbeViewInDrawing(name: String): OscilloscopeProbeVerticeView<*>? {
		return parent!!.getDrawable { it is OscilloscopeProbeVerticeView<*> && it.name == name }
			as OscilloscopeProbeVerticeView<*>?
	}

	private fun handle(event: OscilloscopeProbeNameEvent) {
		if (rows.any { it.probeView.verticeView === event.source } && model.hasPort(event.oldName)) {
			invalidate()
			changeProbeName(event.oldName, event.newName)
			validate()
		}
	}

	private fun changeProbeName(oldName: String, newName: String) {
		model.getPort<Any>(oldName).name = newName
		rowWithName(oldName)?.name = newName
	}

	private fun containsProbeVerticeView(pvv: OscilloscopeProbeVerticeView<*>): Boolean =
		rows.any { it.probeView.verticeView === pvv }

	/**
	 * Listens for removals of [OscilloscopeProbeVerticeView]s in order to put them back in the list.
	 * This is only necessary if the [OscilloscopeProbeVerticeView] has been directly removed in the [GraphView]
	 * and not indirectly by removing an [OscilloscopeProbeView] from this [OscilloscopeView].
	 */
	private inner class RemoveListener : DrawableContainerAdapter<Drawable>() {

		override fun drawableRemoved(event: DrawableContainerEvent<Drawable>) {
			super.drawableRemoved(event)
			if (event.child is OscilloscopeProbeVerticeView<*> && !(event.child as OscilloscopeProbeVerticeView<*>).dragGhost) {
				LOG.userTrail("Delete Oscilloscope probe from graph")
				val comp = event.child as OscilloscopeProbeVerticeView<*>
				rowWithName(comp.name)?.apply {
					val oldName = name
					val newName = createRowName(this, rows.indexOf(this) + 1)
					handleProbeViewRemovedFromDrawing()
					changeProbeName(oldName, newName)
				}
			}
		}
	}
}
