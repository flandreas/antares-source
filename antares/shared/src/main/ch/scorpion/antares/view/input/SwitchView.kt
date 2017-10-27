package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.vertice.VerticeLabelPosition
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A view representation of a [Switch] that supports persistent toggling between two states.
 */
class SwitchView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Switch = Switch(),
    private val textRenderInfoFactory: TextRenderInfoFactory = DrawModule.textRenderInfoFactory
) : DigitalComponentView<Switch>(styleProvider, "library.element.Switch", model), ControlView<Switch>, ControlViewSource<Switch> {

    companion object {
        val LOG by logger(SwitchView::class)
        val PROP_ICON_PATH = "ch.scorpion.antares.view.input.SwitchView.iconPath"
        val SIZE = 4 * Look.SCALE
        val BORDER_WIDTH = 3
        val DIAMETER = 12
        val LABEL_DIST = Look.SCALE
        val LABEL_INSET = 4.0
    }

    var labelPosition: VerticeLabelPosition = VerticeLabelPosition.EXTERNAL
        set(value) {
            invalidate()
            field = value
            setBounds(calculateBounds())
            updateLabelGeometries()
            invalidate()
            update()
            validate()
        }

    /** Handles mouse interactions during execution*/
    private val actorInteractionHandler = InteractionHandler()

    /**
     * The [Label] that displays the signal for [VerticeLabelPosition.EXTERNAL], or the name of this [SwitchView]
     * for [VerticeLabelPosition.INTERNAL].
     */
    private val internalLabel: Label = Label(
            font = font,
            text = "",
            location = Point2D(DigitalPortView.LENGTH - SIZE / 2.0, 0.0),
            horizontalAlignment = Label.HorizontalAlignment.CENTER,
            verticalAlignment = Label.VerticalAlignment.CENTER,
            rotationDisplayStrategy = Label.RotationDisplayStrategy.KEEP_HORIZONTAL)

    private val externalLabel = HorizontalLabel(
            owner = this,
            relLocation = Point2D(-(SIZE + DigitalPortView.LENGTH + LABEL_DIST), 0),
            orientation = Direction.WEST,
            font = font)

    init {
        isFocusable = true
        modelExchanged(null)
        setBounds(calculateBounds())
    }

    /**
     * Calculates the bounds of this [SwitchView] depending on the [labelPosition] and the
     * current externalLabel text
     */
    private fun calculateBounds(): RectangularShape {
        val width = calculateWidth()
        return Rectangle2D(-DigitalPortView.LENGTH - width, -SIZE / 2, width, SIZE)
    }

    private fun updateLabelGeometries() {
        internalLabel.location = Point2D(bounds.centerX, bounds.centerY)
        if (labelPosition == VerticeLabelPosition.INTERNAL) {
            internalLabel.rotationDisplayStrategy = Label.RotationDisplayStrategy.ROTATE_HALF
        } else {
            internalLabel.rotationDisplayStrategy = Label.RotationDisplayStrategy.KEEP_HORIZONTAL
        }
    }

    override fun modelExchanged(oldModel: Switch?) {
        super.modelExchanged(oldModel)
        val portView = DigitalPortView(
            styleProvider = styleProvider,
            port = model!!.getOutput(),
            direction = Direction.EAST)
        portView.setLocation(-portView.length.toDouble(), 0.0)
        addPortView(portView)
        updateLabels()
    }

    /** ---- UI properties */

    var name: String?
        get() = model!!.name
        set(value) {
            model!!.name = value
            updateLabels()
            validate()
        }

    /**
     * Controls the interactive behaviour of this [SwitchView]. If set to `true`, the [Switch]
     * stays in the new state when the user releases the mouse button. If set to `false`,
     * the [Switch] returns to 0 state.
     */
    var toggle: Boolean = true

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (labelPosition != VerticeLabelPosition.EXTERNAL) {
            writer.writeString("labelPos", labelPosition.customName)
        }
        if (!toggle) {
            writer.writeBoolean("toggle", toggle)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("labelPos")) {
            labelPosition = VerticeLabelPosition.withName(reader.readString("labelPos"))
        }
        if (reader.hasAttribute("toggle")) {
            toggle = reader.readBoolean("toggle")
        }
    }

    /** ---- [ActorView] interface */

    override fun getActorInteractionHandler(): ActorInteractionHandler? {
        return actorInteractionHandler
    }

    /** ---- [Component] */

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.REPLACE
        set(value) {
            throw UnsupportedOperationException()
        }

    override fun rotationChanged(newRotation: Rotation) {
        super.rotationChanged(newRotation)
        internalLabel.ownerRotation = rotation
        updateLabels()
    }

    /** ---- [AbstractDrawable] */

    override val boundingBox: Rectangle2D
        get() {
            val bb = super.boundingBox
            val lbb = externalLabel.boundingBox.moveBy(location)
            bb.add(lbb)
            return bb
        }

    override fun draw(context: DrawContext) {
        super.draw(context)
        if (labelPosition != VerticeLabelPosition.INTERNAL) {
            context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
        } else {
            context.g.color = if (model!!.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor
        }
        if (labelPosition == VerticeLabelPosition.EXTERNAL) {
            externalLabel.draw(context)
        }
    }

    override fun drawImpl(context: DrawContext) {
        val oldColor = context.g.color
        super.drawImpl(context)
        drawBodyDigital(context)
        if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
            drawFocus(context)
        }
        context.g.color = oldColor
    }

    /** ---- [ControlViewSource] */

    override val controlId: String
        get() {
            // Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
            // when ControlViews (event as part of a wrapping Component) are added to a Drawing
            return "switch:" + model!!.id
        }

    override val controlName: String
        get() {
            if (StringUtils.isEmpty(model!!.name)) {
                return "$type ($id)"
            }
            return "$type \"${model!!.name}\""
        }

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    override fun createControlView(): ControlView<Switch> {
        val clone = SwitchView(styleProvider, model!!)
        clone.isShowPortViews = false
        clone.location = Point2D()
        clone.name = name
        clone.labelPosition = labelPosition
        return clone
    }

    /** ---- [ControlView] */

    override fun bindToModel(model: Switch) {
        this.model = model
    }


    /** ---- [SwitchView] */

    override fun drawSelected(context: DrawContext) {
        context.g.color = context.color!!.foregroundColor
        draw(context, {
            super.drawImpl(it)
            context.g.drawRect(xInt, yInt, width.toInt(), SIZE)
            context.g.drawRoundRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                    width.toInt() - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH, DIAMETER, DIAMETER)
            if (labelPosition == VerticeLabelPosition.INTERNAL) {
                internalLabel.draw(context)
            }
        })
        if (labelPosition == VerticeLabelPosition.EXTERNAL) {
            externalLabel.draw(context)
        }
    }

    private fun drawBodyDigital(context: DrawContext) {
        val fillColor = Bit.of(model!!.isOn).color.foregroundColor
        context.g.color = color.backgroundColor
        context.g.fillRect(xInt, yInt, widthInt, heightInt)

        context.g.color = fillColor
        context.g.drawRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                widthInt - 2 * BORDER_WIDTH, heightInt - 2 * BORDER_WIDTH)
        context.g.fillRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                widthInt - 2 * BORDER_WIDTH, heightInt - 2 * BORDER_WIDTH)

        context.g.color = color.foregroundColor
        context.g.drawRect(x.toInt(), y.toInt(), widthInt, heightInt)

        internalLabel.color = if (model!!.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor
        if (labelPosition == VerticeLabelPosition.INTERNAL) {
            internalLabel.text = StringUtils.orEmpty(model!!.name)
        } else {
            internalLabel.text = Bit.of(model!!.isOn).toHexString()
        }
        internalLabel.draw(context)
    }

    private fun drawFocus(context: DrawContext) {
        if (isFocusOwner) {
            context.g.color = Themes.get<AntaresTheme>().focus.color.foregroundColor
            context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
            context.g.drawRect(xInt + BORDER_WIDTH - 1, yInt + BORDER_WIDTH - 1,
                    widthInt - 2 * BORDER_WIDTH + 2, heightInt - 2 * BORDER_WIDTH + 2)
        }
    }

    private fun updateLabels() {
        invalidate()
        if (labelPosition == VerticeLabelPosition.INTERNAL) {
            internalLabel.text = StringUtils.orEmpty(name)
        } else {
            externalLabel.text = StringUtils.orEmpty(name)
            externalLabel.rotationChanged()
        }
        setBounds(calculateBounds())
        updateLabelGeometries()
        invalidate()
    }

    /**
     * Calculates the width of this [SwitchView] depending on the current externalLabel and
     * the [VerticeLabelPosition]. If [labelPosition] is [VerticeLabelPosition.INTERNAL],
     * the width is calculated as the smallest integer multiple of [SIZE] that contains the
     * externalLabel when drawn with the current font.
     */
    private fun calculateWidth(): Int {
        if (labelPosition != VerticeLabelPosition.INTERNAL || StringUtils.isEmpty(model!!.name)) {
            return SIZE
        }
        val tri = textRenderInfoFactory.invoke(model!!.name!!, font)
        val requiredSpace = tri.textBounds.width + 2 * LABEL_INSET
        return (SIZE * Math.max(1.0, Math.ceil(requiredSpace / SIZE))).toInt()
    }

    private inner class InteractionHandler : ActorInteractionHandlerAdapter() {
        override fun mousePressed(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
            model!!.toggle(signalHandler)
        }

        override fun mouseReleased(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
            if (!toggle) {
                model!!.toggle(signalHandler)
            }
        }

        override fun keyPressed(signalHandler: SignalHandler, event: KeyEvent) {
            when(event.key) {
                '0'.toInt() -> model!!.setOn(signalHandler, false)
                '1'.toInt() -> model!!.setOn(signalHandler, true)
                '\n'.toInt() -> model!!.toggle(signalHandler)
            }
        }
    }
}

class SwitchViewSelectionModel(component: SwitchView) : AbstractSelectionModel<SwitchView>(component) {

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