package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.IconButton
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.view.app.OscilloscopeViewService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.port.GenericPortView
import ch.scorpion.jabbah.graph.view.port.PortFactory
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class OscilloscopeView(
        private val service: OscilloscopeViewService = GraphViewModule.oscilloscopeViewService,
        private val portFactory: PortFactory = GraphViewModule.portFactory,
        private val factory: OscilloscopeViewFactory = GraphViewModule.oscilloscopeViewFactory,
        referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
        model: Oscilloscope = Oscilloscope(),
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularVerticeView<Oscilloscope>(
        styleProvider,
        "graph.component.oscilloscope",
        model,
        x = 0.0,
        y = 0.0,
        w = WIDTH.toDouble(),
        h = DEF_HEIGHT.toDouble()
) {

    companion object {
        private val LOG by logger(OscilloscopeView::class)
        private const val WIDTH = 700
        private const val DEF_HEIGHT = 200
        private const val TITLE_HEIGHT = 40
        private const val MAX_ROW_NUMBER = 9
        private const val ROW_INSET = 10
        private const val ICON_BUTTON_SIZE = 20
        private const val MIN_SIGNAL_WIDTH = 15.0
        private const val DISPLAY_X = ROW_INSET + 2 * ICON_BUTTON_SIZE
        private const val DISPLAY_WIDTH = WIDTH - 2 * ROW_INSET - 2 * ICON_BUTTON_SIZE
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

    private val container = DrawableContainerImpl<Drawable>(useLocation = true)

    private val rows = mutableListOf<RowView>()

    private val scaleRow = ScaleRow(Point2D())

    private val refColorSequence = referenceColorSequenceProvider.provide()

    private val removeListener = RemoveListener()

    /** Replaced if model changes when reading from persistent store.*/
    private var timeline = OscilloscopeViewTimeline(1.0, model, MIN_SIGNAL_WIDTH)

    init {
        preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
        container.add(scaleRow)
        adjustSize()

        DrawableOwner(this, container)
    }

    override fun modelExchanged(oldModel: Oscilloscope?) {
        super.modelExchanged(oldModel)
        timeline = OscilloscopeViewTimeline(timelineScale, model!!, MIN_SIGNAL_WIDTH)
    }

    /** ---- [Drawable] */

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return container.getInputEventHandler(context)
    }

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        return container.getToolTipText(x, y, width)
    }

    override fun <T: Drawable> handleAdded(container: DrawableContainer<T>) {
        super.handleAdded(container)
        container.addDrawableContainerListener(removeListener as DrawableContainerListener<T>)
    }

    override fun <T: Drawable> handleRemoved(container: DrawableContainer<T>) {
        super.handleRemoved(container)
        container.removeDrawableContainerListener(removeListener as DrawableContainerListener<T>)
    }

    /** ---- [AbstractRectangularVerticeView] */

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.translate(location.x, location.y)
        context.g.color = context.choose(color).backgroundColor
        context.g.fill(bounds)
        context.g.color = context.choose(color).foregroundColor
        context.g.draw(bounds)
        context.g.drawString("Oscilloscope", x.toInt() + 10, y.toInt() + 20)
        context.g.translate(-location.x, -location.y)
        container.draw(context)
    }

    override var location: Point2D
        get() = super.location
        set(value) {
            super.location = value
            container.location = value
        }

    /** ---- [AbstractGraphElementView] */

    override fun handleExecutionStarted(signalHandler: SignalHandler) {
        super.handleExecutionStarted(signalHandler)
        rows.forEach { it.bindDrawer() }
    }

    override fun handleExecutionStopped(signalHandler: SignalHandler) {
        super.handleExecutionStopped(signalHandler)
        rows.forEach { it.unbindDrawer() }
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
        for (i in 1..model!!.portsCount) {
            addPortView(GenericPortView<Any>(model!!.getPort(i.toString())))
            addRowView(i)
        }
        scaleRow.updateAddButtonState()
        adjustSize()

        parent!!
                .getDrawables { it is OscilloscopeProbeVerticeView<*> }
                .map { it as OscilloscopeProbeVerticeView<Any> }
                .sortedBy { it.rowNumber }
                .forEach { rows[it.rowNumber - 1].loadedWith(it) }
    }

    /** ---- [OscilloscopeView] */

    fun addRow() {
        val newRowNumber = rows.size + 1

        val port = portFactory.createOscilloscopeProbePort<Any>(newRowNumber.toString())
        model!!.addPort(port)
        addPortView(GenericPortView(port))

        invalidate()
        addRowView(newRowNumber)
        scaleRow.updateAddButtonState()
        adjustSize()
    }

    /** Removes the row with the specified index, starting with 1.*/
    fun removeRow(index: Int) {
        val row = rows[index - 1]
        rows.removeAt(index - 1)
        container.remove(row)
        refColorSequence.free(row.color)
        findProbeViewInDrawing(row.rowNumber)?.let { (parent as DrawableContainer<Component>)?.remove(it) }

        val port = model!!.getPort<Any>(index.toString())
        val portView = getPortView(port)
        removePortView(portView!!)
        model!!.removePort(port)

        rearrangeFromIndex(index)
        adjustSize()
    }

    private fun adjustSize() {
        scaleRow.updateLocation()
        invalidate()
        setBounds(0.0, 0.0, WIDTH.toDouble(), (TITLE_HEIGHT + (rows.size + 1) * factory.rowHeight).toDouble())
        invalidate()
        update()
    }

    private fun addRowView(rowNumber: Int) {
        val y = TITLE_HEIGHT + rows.size * factory.rowHeight
        val rowView = RowView(rowNumber, Point2D(0, y), refColorSequence.next(), factory)
        rows.add(rowView)
        container.add(rowView)
    }

    private fun rearrangeFromIndex(index: Int) {
        for (i in index - 1 ..rows.size - 1) {
            rows[i].location = Point2D(rows[i].location.x, rows[i].location.y - factory.rowHeight)
            rows[i].rowNumber = i + 1
        }
        scaleRow.updateLocation()
        scaleRow.updateAddButtonState()
    }

    private fun findProbeViewInDrawing(rowNumber: Int): OscilloscopeProbeVerticeView<*>? {
        return parent!!.getDrawable { it is OscilloscopeProbeVerticeView<*> && it.rowNumber == rowNumber }
                as OscilloscopeProbeVerticeView<*>?
    }

    /** [SimpleScale] is currently only drawn if there is only a single row.*/
    private inner class SimpleScale(shape: RectangularShape) : AbstractRectangle(shape) {

        private val label = Label(
                text = "Test",
                font = Themes.get<GraphTheme>().annotation.font,
                color = color.textColor,
                horizontalAlignment = Label.HorizontalAlignment.CENTER,
                verticalAlignment = Label.VerticalAlignment.BOTTOM,
                location = Point2D(x + width / 2, y + height / 2 - 5)
        )

        override val lineWidth: Double get() = 0.0

        override fun draw(context: DrawContext) {
            if (context.castedAppContext<GraphApplicationContext>()!!.mode != ApplicationMode.EXECUTE) {
                return
            }
            val deltaTime = when (model!!.portsCount) {
                0 -> 0
                1 -> model!!.getSignalHistory("1")!!.minDelay
                else -> model!!.minDiffTime
            }
            if (deltaTime > 0) {
                val length = timeline.getDx(deltaTime) / timelineScale

                val x1 = x + width / 2 - length / 2
                val x2 = x + width / 2 + length / 2
                val y = y + height / 2

                context.g.color = color.foregroundColor
                context.g.font = font
                context.g.drawLine(x1, y, x2, y)
                context.g.drawLine(x1, y - 3, x1, y + 3)
                context.g.drawLine(x2, y - 3, x2, y + 3)

                label.text = "${Math.round(deltaTime / timelineScale).toString()} ns"
                label.draw(context)
            }
        }
    }

    private inner class ScaleRow(
            location: Point2D
    ) : DrawableContainerImpl<Drawable>(location = location, useLocation =  true) {

        private val addButton = IconButton(
            icon = AddIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
            action = { service.addRow(this@OscilloscopeView) },
            location = Point2D(ROW_INSET, factory.rowHeight / 2 - ICON_BUTTON_SIZE / 2))

        private val scale = SimpleScale(Rectangle2D(DISPLAY_X, 0, DISPLAY_WIDTH, factory.rowHeight))

        init {
            add(addButton)
            add(scale)
        }

        fun updateAddButtonState() {
            addButton.enabled = rows.size < MAX_ROW_NUMBER
            addButton.tooltipKey = if (addButton.enabled) "graph.action.oscilloscope.addRow.name" else "graph.action.oscilloscope.addRow.limit"
        }

        fun updateLocation() {
            location = Point2D(0, TITLE_HEIGHT + factory.rowHeight * rows.size)
        }
    }

    /**
     * @param rowNumber the number of the row, starting with 1
     */
    private inner class RowView(
            rowNumber: Int,
            location: Point2D,
            val color: CompositeColor,
            factory: OscilloscopeViewFactory
    ) : DrawableContainerImpl<Drawable>(location = location, useLocation = true) {

        private val drawer = factory.createSignalHistoryDrawer()

        private val probeView = OscilloscopeProbeView(
                location = Point2D(2.0 * ROW_INSET + ICON_BUTTON_SIZE, factory.rowHeight / 2 - OscilloscopeProbeViewDrawable.SIZE / 2),
                rowNumber = rowNumber,
                color = color,
                origLocSource = { this@OscilloscopeView.location.add(this.location) })

        init {
            add(IconButton(
                    icon = RemoveIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
                    tooltipKey = "graph.action.oscilloscope.removeRow.name",
                    location = Point2D(ROW_INSET, factory.rowHeight / 2 - ICON_BUTTON_SIZE / 2),
                    action = { service.removeRow(probeView.rowNumber, this@OscilloscopeView) }))
            add(probeView)

            val drawerX = 3.0 * ROW_INSET + ICON_BUTTON_SIZE + probeView.width
            drawer.setBounds(drawerX, 0.0, WIDTH - drawerX - ROW_INSET, factory.rowHeight.toDouble())
            add(drawer)
        }

        var rowNumber: Int
            get() = probeView.rowNumber
            set(value) {
                probeView.rowNumber = value
            }

        fun loadedWith(vertice: OscilloscopeProbeVerticeView<Any>) {
            probeView.vertice = vertice
            probeView.vertice!!.refColor = color
        }

        fun handleProbeViewRemovedFromDrawing() {
            probeView.handleProbeViewRemovedFromDrawing()
        }

        fun bindDrawer() {
            drawer.bind(
                    model!!.getSignalHistory(rowNumber.toString())!!,
                    model!!.getSignalHistory("1"),
                    timeline,
                    color
            )
        }

        fun unbindDrawer() {
            drawer.bind(null, null, null, color)
        }
    }

    /** Listens for removals of [OscilloscopeProbeView]s in order to put them back in the list.*/
    private inner class RemoveListener : DrawableContainerAdapter<Drawable>() {
        override fun drawableRemoved(event: DrawableContainerEvent<Drawable>) {
            super.drawableRemoved(event)
            if (event.child is OscilloscopeProbeVerticeView<*>) {
                LOG.debug("Removed OscilloscopeProbeView from drawing")
                val comp = event.child as OscilloscopeProbeVerticeView<*>
                rows[comp.rowNumber - 1].handleProbeViewRemovedFromDrawing()
            }
        }
    }
}

class OscilloscopeViewTimeline(
        override var scale: Double,
        private val model: Oscilloscope,
        private val minSignalWidth: Double
) : SignalHistoryTimeline {

    override fun getDx(duration: Long): Double {
        return scale * when (model.portsCount) {
            0 -> 0.0
            1 -> duration / model.getSignalHistory("1")!!.minDelay * minSignalWidth
            else -> {
                if (model.minDiffTime == Long.MAX_VALUE) {
                    duration / model.overallMinDelay * minSignalWidth
                } else {
                    duration / model.minDiffTime * minSignalWidth
                }
            }
        }
    }

    override fun getX(time: Long): Double {
        return getDx(model.maxTime - time)
    }
}

private class AddIcon(override val dim: Dimension2D) : Icon {

    override fun draw(context: DrawContext, location: Point2D) {
        context.g.drawOval(location.x, location.y, dim.width, dim.height)
        context.g.drawLine(
                location.x + dim.width / 3, location.y + dim.height / 2,
                location.x + 2 * dim.width / 3, location.y + dim.height / 2)
        context.g.drawLine(
                location.x + dim.width / 2, location.y + dim.height / 3,
                location.x + dim.width / 2, location.y + 2 * dim.height / 3)
    }
}

private class RemoveIcon(override val dim: Dimension2D) : Icon {

    override fun draw(context: DrawContext, location: Point2D) {
        context.g.drawOval(location.x, location.y, dim.width, dim.height)
        context.g.drawLine(
                location.x + dim.width / 3, location.y + dim.height / 2,
                location.x + 2 * dim.width / 3, location.y + dim.height / 2)
    }
}
