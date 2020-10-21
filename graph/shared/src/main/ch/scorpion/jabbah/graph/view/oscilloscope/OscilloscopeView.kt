package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.IconButton
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.AbstractActorIconButton
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorViewContainer
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.port.PortFactory
import ch.scorpion.jabbah.graph.ui.KnobView
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
		private const val TITLE_HEIGHT = 15
		private const val MAX_ROW_NUMBER = 9
		private const val ROW_INSET = 10
		private const val ICON_BUTTON_SIZE = 20
		private val DRAWER_X = 3.0 * ROW_INSET + ICON_BUTTON_SIZE + OscilloscopeProbeViewIcon.SIZE
		private val DRAWER_W = WIDTH - DRAWER_X - ROW_INSET
		private val KNOB: KnobView by lazy { KnobView(unit = "x") }
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

	private val rows = mutableListOf<SignalRowView>()

	private val scaleRow = ScaleRowView(Point2D.ZERO)

	private val refColorSequence = referenceColorSequenceProvider.provide()

	private val removeListener = RemoveListener()

	/** Replaced if model changes when reading from persistent store.*/
	private var timeline = OscilloscopeViewTimeline(1.0, model)

	private val applicationModeHandler: (ApplicationModeEvent) -> Unit = { applicationMode = it.applicationMode }

	private  var applicationMode: ApplicationMode = ApplicationMode.EDIT
		set(value) {
			if (field != value) {
				field = value
				updateSate()
			}
		}

	init {
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
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

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
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

	override fun resolutionDone() {
		super.resolutionDone()

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

	private fun rowWithName(name: String): SignalRowView? = rows.firstOrNull { it.name == name }

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
	private fun findRowView(name: String): SignalRowView? {
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
		val rowView = SignalRowView(name, Point2D(0, y), nextProbeColor, factory)
		rows.add(rowView)
		container.add(rowView)
	}

	private val nextProbeColor: CompositeColor get() = if (BaseModule.properties.getBoolean(PROP_INDIVIDUAL_PROBE_COLORS)) {
		refColorSequence.next()
	} else {
		color
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

	private inner class ScaleButton(
		location: Point2D
	) : AbstractActorIconButton(
		icon = KnobIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
		location = location,
		tooltipKey = "graph.action.oscilloscope.scale.name",
	) {

		override fun handleClicked(context: ActorInteractionContext) {
			showKnob(context.view as DrawingView<*>)
		}

		private fun showKnob(view: DrawingView<*>) {
			KNOB.valueChangeHandler = {timelineScale = it.toDouble() }
			KNOB.location = Point2D(boundingBox.center
				.add(this@OscilloscopeView.location)
				.add(scaleRow.location)
				.subtract(Point2D(KnobView.OUTER_SIZE / 2, KnobView.OUTER_SIZE / 2))
			)
			KNOB.value = timelineScale.toLong()
			KNOB.defaultValue = 10

			view.content.animationContainer.add(KNOB)
			view.content.animationContainer.validate()
		}
	}

	private inner class ScaleRowView(
		location: Point2D
	) : ActorViewContainer<Drawable>(location = location, useLocation = true) {

		private val addButton = IconButton<EditInputEventContext>(
			icon = AddIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
			action = { service.addRow(this@OscilloscopeView) },
			location = Point2D(ROW_INSET, factory.rowHeight / 2 - ICON_BUTTON_SIZE / 2))


		private val scaleButton = ScaleButton(
			location = Point2D(2 * ROW_INSET + ICON_BUTTON_SIZE, factory.rowHeight / 2 - ICON_BUTTON_SIZE / 2))

		private val timelineView = factory.createSignalHistoryTimelineView()

		init {
			add(addButton)
			add(scaleButton)

			timelineView.setBounds(DRAWER_X, 0.0, DRAWER_W, factory.rowHeight.toDouble())
			add(timelineView)
		}

		fun updateState() {
			scaleButton.enabled = applicationMode.isExecute()
			addButton.enabled = applicationMode.isEdit() && rows.size < MAX_ROW_NUMBER
			addButton.tooltipKey = if (addButton.enabled) "graph.action.oscilloscope.addRow.name" else "graph.action.oscilloscope.addRow.limit"
		}

		fun updateLocation() {
			location = Point2D(0, TITLE_HEIGHT + factory.rowHeight * rows.size)
		}

		fun bindDrawer() {
			timelineView.bind(
				model.getSignalHistory("1"),
				timeline)
		}

		fun unbindDrawer() {
			timelineView.bind(null, null)
		}
	}

	private inner class SignalRowView(
		name: String,
		location: Point2D,
		val color: CompositeColor,
		factory: OscilloscopeViewFactory
	) : DrawableContainerImpl<Drawable>(location = location, useLocation = true) {

		private val drawer = factory.createSignalHistoryDrawer()

		private val probeView = OscilloscopeProbeView(
			location = Point2D(2.0 * ROW_INSET + ICON_BUTTON_SIZE, factory.rowHeight / 2 - OscilloscopeProbeViewIcon.SIZE / 2),
			name = name,
			color = color,
			origLocSource = { this@OscilloscopeView.location.add(this.location) })

		private val removeButton = IconButton<EditInputEventContext>(
			icon = RemoveIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
			tooltipKey = "graph.action.oscilloscope.removeRow.name",
			location = Point2D(ROW_INSET, factory.rowHeight / 2 - ICON_BUTTON_SIZE / 2),
			action = { service.removeRow(probeView.name, this@OscilloscopeView) })

		init {
			add(removeButton)
			add(probeView)

			drawer.setBounds(DRAWER_X, 0.0, DRAWER_W, factory.rowHeight.toDouble())
			add(drawer)
		}

		var name: String
			get() = probeView.name
			set(value) {
				probeView.name = value
			}

		fun updateState() {
			removeButton.enabled = applicationMode.isEdit()
		}

		fun loadedWith(vertice: OscilloscopeProbeVerticeView<Any>) {
			probeView.verticeView = vertice
			probeView.verticeView!!.refColor = color
		}

		fun handleProbeViewRemovedFromDrawing() {
			probeView.handleProbeViewRemovedFromDrawing()
		}

		fun bindDrawer() {
			drawer.bind(
				model.getSignalHistory(name)!!,
				model.getSignalHistory("1"),
				timeline,
				color
			)
		}

		fun unbindDrawer() {
			drawer.bind(null, null, null, color)
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
				LOG.debug("Removed OscilloscopeProbeView from drawing")
				val comp = event.child as OscilloscopeProbeVerticeView<*>
				findRowView(comp.name)?.handleProbeViewRemovedFromDrawing()
			}
		}
	}
}

class OscilloscopeViewTimeline(
	override var scale: Double,
	private val model: Oscilloscope
) : SignalHistoryTimeline {

	override val maxTime: Long get() = model.maxTime

	override fun getDx(duration: Long): Double {
		return scale * duration / 20
	}

	override fun getX(time: Long): Double {
		return getDx(model.maxTime - time)
	}
}
