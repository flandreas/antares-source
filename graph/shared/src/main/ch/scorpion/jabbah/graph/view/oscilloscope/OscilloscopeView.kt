package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Tooltip
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
import ch.scorpion.jabbah.execution.actor.ActorViewContainer
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.GenericPortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class OscilloscopeView(
	private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService,
	private val portFactory: PortFactory = GraphModelModule.portFactory,
	private val factory: OscilloscopeViewFactory = GraphViewModule.oscilloscopeViewFactory,
	referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
	model: Oscilloscope = Oscilloscope(),
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
		const val TITLE_HEIGHT = 15
		const val MAX_ROW_NUMBER = 9
		const val ROW_INSET = 10
		const val ICON_BUTTON_SIZE = 20
		val DRAWER_X = 3.0 * ROW_INSET + ICON_BUTTON_SIZE + OscilloscopeProbeViewIcon.SIZE
		val DRAWER_W = WIDTH - DRAWER_X - ROW_INSET
	}

	var timelineScale: Double
		get() = timeline.scale
		set(value) {
			invalidate()
			timeline.scale = value
			validate()
		}

	/** Returns the number of rows of this [OscilloscopeView].*/
	val rowsCount: Int get() = rows.size

	private val container = ActorViewContainer<Drawable>(useLocation = true)

	private val rows = mutableListOf<OscilloscopeSignalRowView>()

	val signalRowViews: ImmutableList<OscilloscopeSignalRowView> get() = rows.toImmutableList()

	private val scaleRow = OscilloscopeScaleRowView(this, Point2D.ZERO, service, factory)

	private val refColorSequence = referenceColorSequenceProvider.provide()

	private val removeListener = RemoveListener()

	/** Replaced if model changes when reading from persistent store.*/
	var timeline = OscilloscopeViewTimeline(1.0, model)

	private val applicationModeHandler: (ApplicationModeEvent) -> Unit = { applicationMode = it.applicationMode }

	private val probeNameHandler: (OscilloscopeProbeNameEvent) -> Unit = { handle(it) }

	var applicationMode: ApplicationMode = ApplicationMode.EDIT
		private set(value) {
			if (field != value) {
				field = value
				updateSate()
			}
		}

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(OscilloscopeProbeNameEvent::class, probeNameHandler)

		visible = false
		preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
		container.add(scaleRow)
		adjustSize()

		DrawableOwner(this, container)

		updateSate()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(probeNameHandler)
	}

	override fun modelExchanged(oldModel: Oscilloscope?) {
		super.modelExchanged(oldModel)
		timeline = OscilloscopeViewTimeline(timelineScale, model)
	}

	private fun updateSate() {
		rows.forEach { it.updateState() }
		scaleRow.updateState()
	}

	/** ---- [Drawable] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return container.getInputEventHandler(context)
	}

	override fun getTooltip(x: Double, y: Double): Tooltip? {
		return container.getTooltip(x, y) ?: super.getTooltip(x, y)
	}

	override fun <T : Drawable> handleAdded(container: DrawableContainer<T>) {
		super.handleAdded(container)
		container.addDrawableContainerListener(removeListener as DrawableContainerListener<T>)
	}

	override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
		super.handleRemoved(container)
		container.removeDrawableContainerListener(removeListener as DrawableContainerListener<T>)
	}

	/** ---- [AbstractVerticeView] */

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? {
		return container.getExecutionTooltip(x, y) ?: super.getExecutionTooltip(x, y)
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
		context.g.translate(location.x, location.y)
		context.g.color = context.choose(color).backgroundColor
		context.g.fill(bounds)
		context.g.color = context.choose(color).foregroundColor
		context.g.stroke = stroke
		context.g.draw(bounds)
		context.g.translate(-location.x, -location.y)
		container.draw(context)
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return container.getActorInteractionHandler(context)
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleExecutionStarted(signalHandler: SignalHandler) {
		super.handleExecutionStarted(signalHandler)
		rows.forEach { it.bindDrawer() }
		scaleRow.bindDrawer()
	}

	override fun handleExecutionStopped(signalHandler: SignalHandler) {
		super.handleExecutionStopped(signalHandler)
		rows.forEach { it.unbindDrawer() }
		scaleRow.unbindDrawer()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("scale", timelineScale)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("scale")) {
			timelineScale = reader.readDouble("scale")
		}
	}

	override fun allResolutionDone() {
		super.allResolutionDone()

		for (port in model.getPorts()) {
			addPortView(GenericPortView(port))
			addRowView(port.name!!)
		}

		scaleRow.updateState()
		adjustSize()

		parent!!
			.getDrawables{ it is OscilloscopeProbeVerticeView<*> }
			.map { it as OscilloscopeProbeVerticeView<Any> }
			.forEach { rows.find { row -> row.name == it.name }!!.loadedWith(it) }
	}

	/** ---- [OscilloscopeView] */

	fun addRow() {
		val name = createRowName()

		val port = portFactory.createOscilloscopeProbePort<Any>(name)
		model.addPort(port)
		addPortView(GenericPortView(port))

		invalidate()
		addRowView(name)
		scaleRow.updateState()
		adjustSize()
	}

	private fun createRowName(): String {
		var nameNumber = rows.size + 1
		while (rowWithName(nameNumber.toString()) != null) {
			nameNumber++
		}
		return nameNumber.toString()
	}

	private fun rowWithName(name: String): OscilloscopeSignalRowView? = rows.firstOrNull { it.name == name }

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

	/** Finds the [SignalRowView] with the specified name, if existing.*/
	private fun findRowView(name: String): OscilloscopeSignalRowView? {
		return rows.firstOrNull { it.name == name }
	}

	private fun adjustSize() {
		scaleRow.updateLocation()
		invalidate()
		setBounds(0.0, 0.0, WIDTH.toDouble(), (TITLE_HEIGHT + (rows.size + 1) * factory.rowHeight).toDouble())
		invalidate()
		update()
	}

	private fun addRowView(name: String) {
		val y = TITLE_HEIGHT + rows.size * factory.rowHeight
		val rowView = OscilloscopeSignalRowView(this, name, Point2D(0, y), nextProbeColor, service, factory)
		rows.add(rowView)
		container.add(rowView)
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
			rows[i].location = Point2D(rows[i].location.x, rows[i].location.y - factory.rowHeight)
		}
		scaleRow.updateLocation()
		scaleRow.updateState()
	}

	private fun findProbeViewInDrawing(name: String): OscilloscopeProbeVerticeView<*>? {
		return parent!!.getDrawable { it is OscilloscopeProbeVerticeView<*> && it.name == name }
			as OscilloscopeProbeVerticeView<*>?
	}

	private fun handle(event: OscilloscopeProbeNameEvent) {
		if (model.hasPort(event.oldName)) {
			model.getPort<Any>(event.oldName).name = event.newName
			invalidate()
			rowWithName(event.oldName)?.name = event.newName
			validate()
		}
	}

	/**
	 * Listens for removals of [OscilloscopeProbeVerticeView]s in order to put them back in the list.
	 * This is only necessary if the [OscilloscopeProbeVerticeView] has been directly removed in the [GraphView]
	 * and not indirectly by removing an [OscilloscopeProbeView] from this [OscilloscopeView].
	 */
	private inner class RemoveListener : DrawableContainerAdapter<Drawable>() {
		override fun drawableRemoved(event: DrawableContainerEvent<Drawable>) {
			super.drawableRemoved(event)
			if (event.child is OscilloscopeProbeVerticeView<*>) {
				LOG.trace("Removed OscilloscopeProbeView from drawing")
				val comp = event.child as OscilloscopeProbeVerticeView<*>
				findRowView(comp.name)?.handleProbeViewRemovedFromDrawing()
			}
		}
	}
}
