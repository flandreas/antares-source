package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.LED
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes

/**
 * A view of an [LED].
 */
class LEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: LED = LED(),
    lightColor: LightColor = LEDView.DEFAULT_LIGHT_COLOR
) : DigitalComponentView<LED>(styleProvider, "library.element.LED", model), ControlView<LED>, ControlViewSource<LED>{

    companion object {
        val PROP_ICON_PATH = "ch.scorpion.antares.view.output.LEDView.iconPath"
        val DEFAULT_LIGHT_COLOR = LightColor.RED
        val SIZE = 4 * Look.SCALE
        val COLOR_BORDER = Color.DARK_GRAY
        val COLOR_CASE = Color.DARK_GRAY
        val BORDER_WIDTH = 3
        val LABEL_DIST = Look.SCALE
    }

    var lightColor: LightColor = lightColor
        set(value) {
            invalidate()
            field = value
        }

    private val label = HorizontalLabel(
            owner = this,
            relLocation = Point2D(SIZE + DigitalPortView.LENGTH + LABEL_DIST, 0),
            font = font)

    init {
        modelExchanged(null)
        setBounds(getInput().unconnectedLength, -SIZE / 2, SIZE, SIZE)
    }

    override fun modelExchanged(oldModel: LED?) {
        super.modelExchanged(oldModel)
        val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getInput(),
			direction = Direction.WEST)
		portView.setLocation(portView.unconnectedLength, 0)
		addPortView(portView)
        updateLabel()
    }

    var name: String?
        get() = model!!.name
        set(value) {
            model!!.name = value
            updateLabel()
        }

    /** ---- [ControlView] */

    override val controlId: String
        get() {
            // Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
            // when ControlViews (event as part of a wrapping Component) are added to a Drawing
            return "led:" + model!!.id
        }

    /** ---- [ControlViewSource] */

    override val controlName: String
        get() {
            if (StringUtils.isEmpty(model!!.name)) {
                return "$type ($id)"
            }
            return "$type \"${model!!.name}\""
        }

    override fun createControlView(): ControlView<LED> {
        val clone = LEDView(styleProvider, model!!, lightColor)
        clone.isShowPortViews = false
        clone.location = Point2D(0, 0)
        return clone
    }

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    override fun bindToModel(model: LED) {
        this.model = model
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("color", lightColor.customName)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        lightColor = LightColor.withName(reader.readString("color"))
    }

    /** ---- [AbstractComponent] */

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.REPLACE
        set(value) {super.preferredSelectionDrawingStrategy = value}

    override fun rotationChanged(newRotation: Rotation) {
        super.rotationChanged(newRotation)
        label.rotationChanged()
    }

    /** ---- [AbstractDrawable] */

    override val boundingBox: Rectangle2D
        get() {
            val bb = Rectangle2D(super.boundingBox)
            val lbb = label.boundingBox.moveBy(location)
            bb.add(lbb)
            return bb
        }

    /** ---- [AbstractVerticeView] */

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
        label.draw(context)
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        drawBody(context)
    }

    /** ---- [LEDView] */

    override fun drawSelected(context: DrawContext) {
        context.g.color = context.color!!.foregroundColor
        draw(context) { c ->
            super.drawImpl(c)
            context.g.drawOval(xInt, yInt, SIZE, SIZE)
            context.g.drawOval(
                xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
        }
        label.draw(context)
    }

    private fun drawBody(context: DrawContext) {
        context.g.color = COLOR_CASE
        context.g.fillOval(xInt, yInt, SIZE, SIZE)
        context.g.color = COLOR_BORDER
        context.g.drawOval(xInt, yInt, SIZE, SIZE)
        context.g.color = getBulbColor()
        context.g.fillOval(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
        context.g.drawOval(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
    }

    private fun getBulbColor(): Color {
        if (model!!.isOn) {
            return lightColor.onColor
        }
        return lightColor.offColor
    }

    private fun updateLabel() {
        invalidate()
        label.text = StringUtils.orEmpty(name)
        label.rotationChanged()
        invalidate()
    }
}

class LEDViewSelectionModel(c: LEDView) : AbstractSelectionModel<LEDView>(c) {

    override fun draw(context: DrawContext) {
        val oldUseContextColors = context.useContextColors
        context.useContextColors = true
        context.color = Themes.get<AntaresTheme>().selection
        component.drawSelected(context)
        context.useContextColors = oldUseContextColors
    }

    override val boundingBox: RectangularShape get() = component.boundingBox

    override fun contains(x: Double, y: Double): Boolean {
        return component.contains(x, y)
    }

    override fun componentUpdated() {
        validate()
    }
}