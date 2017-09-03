package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.IconButton
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

class OscilloscopeView(
        private val factory: OscilloscopeViewFactory = GraphViewModule.oscilloscopeViewFactory,
        referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider
) : RectangularComponent(shape = Rectangle2D(0, 0, WIDTH, DEF_HEIGHT)) {

    companion object {
        private val LOG by logger(OscilloscopeView::class)
        private const val WIDTH = 500
        private const val DEF_HEIGHT = 200
        private const val TITLE_HEIGHT = 40
        private const val MAX_ROW_NUMBER = 9
        private const val ROW_INSET = 10
        private const val ICON_BUTTON_SIZE = 20
    }

    private val container = DrawableContainerImpl<Drawable>(useLocation = true)

    private val rows = mutableListOf<RowView>()

    private val addButton = IconButton(
            icon = AddIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
            action = {
                addRow()
                validate()
            },
            location = Point2D(10, TITLE_HEIGHT + 15))

    private val refColorSequence = referenceColorSequenceProvider.provide()

    private val removeListener = RemoveListener()

    init {
        preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
        container.add(addButton)
        for (row in 0..3) {
            addRow()
        }
        adjustSize()

        DrawableOwner(this, container)
    }

    /** ---- [Component] interface */

    override val type: String? get() = Translations.getString("graph.component.oscilloscope")

    /** ---- [Drawable] */

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return container.getInputEventHandler(context)
    }

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.drawString("Oscilloscope", x.toInt() + 10, y.toInt() + 20)
        container.draw(context)
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

    /** ---- [RectangularComponent] */

    override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        super.setFrame(x, y, width, height)
        container.location = location
    }

    /** ---- [OscilloscopeView] */

    private fun adjustSize() {
        updateAddButtonLocation()
        invalidate()
        setFrame(x, y, WIDTH.toDouble(), (TITLE_HEIGHT + (rows.size + 1) * factory.rowHeight).toDouble())
        invalidate()
        update()
    }

    private fun addRow() {
        val y = TITLE_HEIGHT + rows.size * factory.rowHeight
        val rowView = RowView(rows.size + 1, Point2D(0, y), refColorSequence.next(), factory)
        rows.add(rowView)
        container.add(rowView)
        updateAddButtonState()
        adjustSize()
    }

    /** Removes the row with the specified index, starting with 1.*/
    private fun removeRow(index: Int) {
        val row = rows[index - 1]
        rows.removeAt(index - 1)
        container.remove(row)
        refColorSequence.free(row.color)
        findProbeViewInDrawing(row.rowNumber)?.let { (parent as DrawableContainer<Component>)?.remove(it) }
        rearrangeFromIndex(index)
        adjustSize()
        validate()
    }

    private fun rearrangeFromIndex(index: Int) {
        for (i in index - 1 ..rows.size - 1) {
            rows[i].location = Point2D(rows[i].location.x, rows[i].location.y - factory.rowHeight)
            rows[i].rowNumber = i + 1
        }
        updateAddButtonLocation()
        updateAddButtonState()
    }

    private fun updateAddButtonLocation() {
        addButton.location = Point2D(addButton.location.x, TITLE_HEIGHT + (rows.size + 0.5) * factory.rowHeight - addButton.height / 2)
    }

    private fun updateAddButtonState() {
        addButton.enabled = rows.size < MAX_ROW_NUMBER
        addButton.tooltipKey = if (addButton.enabled) "graph.action.oscilloscope.addRow.name" else "graph.action.oscilloscope.addRow.limit"
    }

    private fun findProbeViewInDrawing(rowNumber: Int): OscilloscopeProbeVerticeView<*>? {
        return parent!!.getDrawable { it is OscilloscopeProbeVerticeView<*> && it.rowNumber == rowNumber }
                as OscilloscopeProbeVerticeView<*>?
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
                    action = { removeRow(probeView.rowNumber) }))
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

        fun handleProbeViewRemovedFromDrawing() {
            probeView.handleProbeViewRemovedFromDrawing()
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