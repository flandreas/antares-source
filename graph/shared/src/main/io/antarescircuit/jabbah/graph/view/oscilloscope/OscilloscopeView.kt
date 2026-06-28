package io.antarescircuit.jabbah.graph.view.oscilloscope

import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.container.DrawableContainerAdapter
import io.antarescircuit.jabbah.draw.graphics.ReferenceColor
import io.antarescircuit.jabbah.draw.graphics.ReferenceColorSequenceProvider
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.execution.actor.ActorViewContainer
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.oscilloscope.Oscilloscope
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.model.port.PortFactory
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.oscilloscope.OscilloscopeViewService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.port.GenericPortView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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

	var bufferSize: Int
		get() = model.bufferSize
		set(value) {
			model.bufferSize = value
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

	private val probeVerticeViews: Collection<OscilloscopeProbeVerticeView<*>> get() =
		parent?.getDrawables { it is OscilloscopeProbeVerticeView<*> }
			?.map { it as OscilloscopeProbeVerticeView<*> }
			?: emptyList()

	var editable = true
		private set

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(OscilloscopeProbeNameEvent::class, probeNameHandler)

		preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
	}

	override fun dispose() {
		super.dispose()
		rows.forEach { it.dispose() }
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

	override fun <T: InputEventContext> getTooltip(context: T): Tooltip? =
		container.getTooltip(context) ?: super.getTooltip(context)

	override fun <T : Drawable> handleAdded(container: DrawableContainer<T>) {
		super.handleAdded(container)
		@Suppress("UNCHECKED_CAST")
		container.addDrawableContainerListener(removeListener as DrawableContainerListener<T>)
	}

	override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
		super.handleRemoved(container)
		@Suppress("UNCHECKED_CAST")
		container.removeDrawableContainerListener(removeListener as DrawableContainerListener<T>)
	}

	/** ---- [Component] */

	override val copyable: Boolean get() = false

	override fun getDeleteBuddies(drawing: Drawing<*>): List<Component> =
		rows.mapNotNull { it.probeView.verticeView }

	override fun notifyEditable(editable: Boolean) {
		this.editable = editable
		scaleRowView.updateState()
		rows.forEach { it.updateState() }
	}

	/** ---- [AbstractVerticeView] */

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? =
		container.getExecutionTooltip(context) ?: super.getExecutionTooltip(context)

	override fun handleStateChanged(event: GraphElementEvent) {
		if (event.signalHandler != null && event.reason?.startsWith(Oscilloscope.SIGNAL_RECEIVED) == true) {
			val probeId = event.reason.split(':')[1].toInt()

			val probeView = probeVerticeViews.firstOrNull { it.model.id == probeId }
			if (probeView != null) {
				invalidate()
				readProbeView(probeView, event.signalHandler)
				scaleRowView.timelineView.updateGeometry()
				validate()
			}
		} else {
			super.handleStateChanged(event)
		}
	}

	private fun readSignalsOnStart(signalHandler: SignalHandler) {
		invalidate()
		probeVerticeViews.forEach { probeView ->
			readProbeView(probeView, signalHandler)
		}
		scaleRowView.timelineView.updateGeometry()
		validate()
	}

	private fun readProbeView(probeView: OscilloscopeProbeVerticeView<*>, signalHandler: SignalHandler) {
		val inputPort = probeView.model.getInput<Any>()
		var signal = inputPort.getIncomingSignal()!!

		// Due to an earlier bug, were OscilloscopeProbeVerticeView.edgeView was always null, because it was
		// never set while editing. In order to support circuit simulation with graphs built earlier,
		// still handle the signal, but uncompleted (i.e. using the default)
		if (probeView.edgeView != null) {
			signal = GraphViewModule.oscilloscopeViewFactory.completeSignal(probeView.model, signal, probeView.edgeView!!)
		}

		model.storeSignal(inputPort.name!!, signal, signalHandler)
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
			it.g.color = it.chooseBackground(backgroundColor)
			it.g.fill(bounds)
			it.g.color = it.chooseForeground(foregroundColor)
			it.g.stroke = stroke
			it.g.draw(bounds)
		}

		container.draw(context)

		// "Clocked" annotation
		if (rows.isNotEmpty() && model.mode == SignalHistoriesType.Clocked) {
			context.g.color = foregroundColor
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

	override fun executionStartDone(signalHandler: SignalHandler) {
		super.executionStartDone(signalHandler)
		if (visible) {
			readSignalsOnStart(signalHandler)
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
			addRowView(port)
		}

		scaleRowView.updateState()
		adjustSize()

		probeVerticeViews.forEach {
			rows.find { row -> row.name == it.name }!!.loadedWith(it)
		}
	}

	/** ---- [OscilloscopeView] */

	fun addRow(): String {
		val name = createRowName(null, rows.size + 1)

		val port = portFactory.createOscilloscopeProbePort<Any>(name, model.graphType)
		model.addPort(port)
		addPortView(GenericPortView(port))

		invalidate()
		addRowView(port)
		scaleRowView.updateState()
		adjustSize()

		return name
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
		findProbeViewInDrawing(row.name)?.let { parent?.remove(it) }

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

	private fun addRowView(port: Port<*>) {
		val y = TITLE_HEIGHT + rows.size * rowHeight
		val yAxis = factory.createSignalHistoryYAxis(model.graphType, port)
		val rowView = OscilloscopeSignalRowView(
			this,
			port.name!!,
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
	 * Listens for removals of [OscilloscopeProbeVerticeView]s to put them back in the list.
	 * This is only necessary if the [OscilloscopeProbeVerticeView] has been directly removed in the [GraphView]
	 * and not indirectly by removing an [OscilloscopeProbeView] from this [OscilloscopeView].
	 */
	private inner class RemoveListener : DrawableContainerAdapter<Drawable>() {

		override fun drawableRemoved(event: DrawableContainerEvent<Drawable>) {
			super.drawableRemoved(event)
			if (event.child is OscilloscopeProbeVerticeView<*> && !(event.child as OscilloscopeProbeVerticeView<*>).dragGhost) {
				val comp = event.child as OscilloscopeProbeVerticeView<*>
				LOG.userTrail("Deleted Oscilloscope probe '${comp.name}' from GraphView")
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
