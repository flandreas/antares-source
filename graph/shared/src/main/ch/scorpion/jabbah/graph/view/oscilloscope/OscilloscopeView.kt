package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.drawable.IconButton
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent

class OscilloscopeView(
        referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider
) : RectangularComponent(shape = Rectangle2D(0, 0, WIDTH, DEF_HEIGHT)) {

    companion object {
        private val LOG by logger(OscilloscopeView::class)
        private const val WIDTH = 400
        private const val DEF_HEIGHT = 200
        private const val TITLE_HEIGHT = 40
        private const val ROW_HEIGHT = 40
        private const val MAX_ROW_NUMBER = 9
    }

    private val container = DrawableContainerImpl<Drawable>(useLocation = true)

    private val rows = mutableListOf<RowView>()

    private val addButton = IconButton(
            icon = AddIcon(Dimension2D(20, 20)),
            action = {
                addRow()
                validate()
            },
            location = Point2D(10, TITLE_HEIGHT + 15))

    private val refColorSequence = referenceColorSequenceProvider.provide()

    init {
        preferredSelectionDrawingStrategy = SelectionDrawingStrategy.BELOW
        container.add(addButton)
        for (row in 0..3) {
            addRow()
        }

        DrawableOwner(this, container)
    }

    /** ---- [Component] interface */

    override val type: String? get() = Translations.getString("graph.component.oscilloscope")

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return container.getInputEventHandler(context)
    }

    /** ---- [Drawable] */

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.drawString("Oscilloscope", x.toInt() + 10, y.toInt() + 20)
        container.draw(context)
    }

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        return container.getToolTipText(x, y, width)
    }

    /** ---- [RectangularComponent] */

    override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        super.setFrame(x, y, width, height)
        container.location = location
    }

    /** ---- [OscilloscopeView] */

    private fun adjustSize() {
        addButton.location = Point2D(addButton.location.x, (TITLE_HEIGHT + rows.size * ROW_HEIGHT).toDouble())
        invalidate()
        setFrame(x, y, WIDTH.toDouble(), (TITLE_HEIGHT + (rows.size + 1) * ROW_HEIGHT).toDouble())
        invalidate()
        update()
    }

    private fun addRow() {
        val y = TITLE_HEIGHT + rows.size * ROW_HEIGHT
        val rowView = RowView(rows.size + 1, Point2D(0, y), refColorSequence.next())
        rows.add(rowView)
        container.add(rowView)
        addButton.enabled = rows.size < MAX_ROW_NUMBER
        adjustSize()
    }

    /** Removes the row with the specified index, starting with 1.*/
    private fun removeRow(index: Int) {
        val row = rows[index - 1]
        rows.removeAt(index - 1)
        container.remove(row)
        refColorSequence.free(row.color)
        rearrangeFromIndex(index)
        adjustSize()
        validate()
    }

    private fun rearrangeFromIndex(index: Int) {
        for (i in index - 1 ..rows.size - 1) {
            rows[i].location = Point2D(rows[i].location.x, rows[i].location.y - ROW_HEIGHT)
            rows[i].setRowNumber(i + 1)
        }
        addButton.location = Point2D(addButton.location.x, addButton.location.y - ROW_HEIGHT)
        addButton.enabled = rows.size < MAX_ROW_NUMBER
    }

    /**
     * @property rowNumber the number of the row, starting with 1
     */
    private inner class RowView(
            rowNumber: Int,
            location: Point2D,
            val color: CompositeColor
    ) : DrawableContainerImpl<Drawable>(location = location, useLocation = true) {

        private val probeView = OscilloscopeProbeView(Point2D(40, -15), rowNumber, color)

        init {
            add(IconButton(
                    icon = RemoveIcon(Dimension2D(20, 20)),
                    location = Point2D(10, 0),
                    action = { removeRow(probeView.rowNumber) }))
            add(probeView)
        }

        fun setRowNumber(rowNumber: Int) {
            probeView.rowNumber = rowNumber
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