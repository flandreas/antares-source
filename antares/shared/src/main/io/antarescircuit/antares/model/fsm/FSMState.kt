package io.antarescircuit.antares.model.fsm

import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Ellipse2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.resettableLazy
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularComponent
import io.antarescircuit.jabbah.edit.model.text.description.Describable
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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
            require(value >= 0) { "State number must not be negative." }
            if (field != value) {
                field = value
                updateStateNumberLabel()
            }
        }

    private val stateNumberLabel = Label("", font)

    private val tooltip = resettableLazy { buildTooltipText()?.let {
        Tooltip(it, Rectangle2D(label.boundingBox).moveBy(location))
    } }

    val radius: Double get() = width / 2.0

    init {
        updateStateNumberLabel()
        updateOutputLabel()
    }

    /** ---- [Namable] and [Describable] */

    override var name: Name = Name(name)
        set(value) {
            field = value
            label.text = field.getTranslation()
            tooltip.reset()
        }

    override var description: Description
        get() = super.description
        set(value) {
            super.description = value
            tooltip.reset()
        }

    /** ---- [Drawable] */

    override fun update() {
        super.update()
        parent?.let {
            AntaresModelModule.fsmEditorService.handleStateUpdated(this, it as FSMDrawing)
        }
    }

    override fun <T : InputEventContext> getTooltip(context: T): Tooltip? =
        tooltip.value?.also { it.sourceRect = boundingBox }

    /** --- [Component] */

    override val type: String get() = TYPE

    override fun getDeleteBuddies(drawing: Drawing<*>): List<Component> =
        AntaresModelModule.fsmEditorService.getTransitions(this, drawing as FSMDrawing)

    override fun beforePaste(drawing: Drawing<Component>) {
        super.beforePaste(drawing)
        stateNumber = AntaresModelModule.fsmEditorService.freeStateNumber(drawing as FSMDrawing)
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
        context.g.color = textColor
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
            textColor
        } else {
            backgroundColor
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

    private fun buildTooltipText(): String? = if (description.isNotEmpty) {
        buildToolTipText(type, description.value, typeDesc)
    } else {
        buildToolTipText(type, typeDesc, null)
    }
}