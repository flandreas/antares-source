package ch.scorpion.antares.model.fsm

import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Ellipse2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class FSMState(
    name: String = "",
    styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : RectangularComponent(StyleType.FIGURE, styleProvider, Ellipse2D(0, 0, DEF_SIZE, DEF_SIZE)), Namable {

    companion object {
        private const val DEF_SIZE = Look.SCALE * 10
        private const val MIN_SIZE = Look.SCALE * 5
        private const val FINAL_INSET = 5
        private const val LABEL_DIST_Y = 5
        private val TYPE = Translations.getString("antares.fsm.state")
    }

    @Suppress("MemberVisibilityCanBePrivate") // Reflection
    var stateType: FSMStateType = FSMStateType.Normal
        set(value) {
            if (field != value) {
                field = value
                updateVisualisation()
            }
        }

    /** The output value(s) of the system when the system is in this [FSMState] (Moore machine). */
    var output: String = ""
        set(value) {
            if (field != value) {
                field = value
                updateOutputLabel()
            }
        }

    private val outputLabel = Label("", font)

    /** The unique number of this [FSMState] also used in the generated circuit.*/
    var stateNumber: Int = 0
        set(value) {
            if (field != value) {
                field = value
                updateStateNumberLabel()
            }
        }

    private val stateNumberLabel = Label("", font)

    val radius: Double get() = width / 2.0

    init {
        updateStateNumberLabel()
        updateOutputLabel()
    }

    /** ---- [Drawable] */

    override fun update() {
        super.update()
        parent?.let {
            AntaresModelModule.fsmService.handleStateUpdated(this, it as Drawing<Component>)
        }
    }

    /** --- [Component] */

    override val type: String get() = TYPE

    override fun getDeleteBuddies(drawing: Drawing<Component>): List<Component> =
        AntaresModelModule.fsmService.getTransitions(this, drawing)

    override var name: Name = Name(name)
        set(value) {
            field = value
            label.text = field.getTranslation()
        }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeInt("stateNumber", stateNumber)
        writer.writeString("type", stateType.customName)
        if (name.isNotEmpty) {
            name.write("name", writer)
        }
        if (StringUtils.isNotBlank(output)) {
            writer.writeString("output", output)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        stateNumber = reader.readInt("stateNumber")
        stateType = FSMStateType.withName(reader.readString("type"))
        if (reader.hasElement("name")) {
            name = Name.read("name", reader)
        }
        if (reader.hasAttribute("output")) {
            output = reader.readString("output")
        }
    }

    /** ---- [RectangularComponent] */

    override val maintainAspectRation: Boolean get() = true

    override fun drawShape(context: DrawContext, strokeColor: Color?, fillColor: Color?) {
        when (stateType) {
            FSMStateType.Normal -> {
                super.drawShape(context, strokeColor, fillColor)
            }
            FSMStateType.Initial -> {
                drawFill(context, shapeToDraw, strokeColor)
            }
            FSMStateType.Final -> {
                super.drawShape(context, strokeColor, fillColor)
                context.g.color = strokeColor!!
                context.g.fillOval(x + FINAL_INSET, y + FINAL_INSET, width - 2 * FINAL_INSET, height - 2 * FINAL_INSET)
            }
        }
        context.g.color = color.textColor
        context.translated(location) {
            outputLabel.draw(context)
            stateNumberLabel.draw(context)
        }
    }

    override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        if (width >= MIN_SIZE && height >= MIN_SIZE) {
            super.setFrame(x, y, width, height)
            updateOutputLabelLocation()
            updateStateNumberLabelLocation()
        }
    }

    /** ---- [FSMState] */

    private fun updateVisualisation() {
        invalidate()
        val textColor = if (stateType == FSMStateType.Normal) {
            color.textColor
        } else {
            color.backgroundColor
        }
        label.color = textColor
        outputLabel.color = textColor
        stateNumberLabel.color = textColor

        label.inverse = stateType != FSMStateType.Normal
        outputLabel.inverse = stateType != FSMStateType.Normal
        stateNumberLabel.inverse = stateType != FSMStateType.Normal

        validate()
    }

    private fun updateOutputLabel() {
        invalidate()
        outputLabel.text = output
        invalidate()
        validate()
    }

    private fun updateOutputLabelLocation() {
        outputLabel.location = Point2D(label.location.x, label.location.y + label.font.size / 2 + outputLabel.font.size / 2 + LABEL_DIST_Y)
    }

    private fun updateStateNumberLabel() {
        invalidate()
        stateNumberLabel.text = stateNumber.toString()
        invalidate()
        validate()
    }

    private fun updateStateNumberLabelLocation() {
        stateNumberLabel.location = Point2D(label.location.x, label.location.y - label.font.size / 2 - stateNumberLabel.font.size / 2 - LABEL_DIST_Y)
    }
}