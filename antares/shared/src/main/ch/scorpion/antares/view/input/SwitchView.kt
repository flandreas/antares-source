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
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel


/**
 * A view representation of a [Switch] that supports persistent toggling between two states.
 */
class SwitchView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Switch = Switch()
) : DigitalComponentView<Switch>(styleProvider, "library.element.Switch", model), ControlView<Switch>, ControlViewSource<Switch> {

    companion object {
        val LOG by logger(SwitchView::class)
        val PROP_ICON_PATH = "ch.scorpion.antares.view.input.SwitchView.iconPath"
        val SIZE = 4 * Look.SCALE
        val BORDER_WIDTH = 3
        val DIAMETER = 12
        val LABEL_DIST = Look.SCALE
    }

    /** Handles mouse interactions during execution*/
    private val actorInteractionHandler = InteractionHandler()

    private val signalLabel: Label

    private val label = HorizontalLabel(
            owner = this,
            relLocation = Point2D(-(SIZE + DigitalPortView.LENGTH + LABEL_DIST), 0),
            orientation = Direction.WEST,
            font = font)

    init {
        isFocusable = true
        modelExchanged(null)
        signalLabel = Label(
            font = font,
            text = "",
            location = Point2D(-getOutput().length - SIZE / 2.0, 0.0),
            horizontalAlignment = Label.HorizontalAlignment.CENTER,
            verticalAlignment = Label.VerticalAlignment.CENTER,
            rotationDisplayStrategy = Label.RotationDisplayStrategy.KEEP_HORIZONTAL)

        setBounds(-getOutput().length - SIZE, -SIZE / 2, SIZE, SIZE)
    }

    override fun modelExchanged(oldModel: Switch?) {
        super.modelExchanged(oldModel)
        val portView = DigitalPortView(
            styleProvider = styleProvider,
            port = model!!.getOutput(),
            direction = Direction.EAST)
        portView.setLocation(-portView.length.toDouble(), 0.0)
        addPortView(portView)
        updateLabel()
    }

    /** ---- UI properties */

    var name: String?
        get() = model!!.name
        set(value) {
            model!!.name = value
            updateLabel()
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
        signalLabel.ownerRotation = rotation
        label.rotationChanged()
    }

    /** ---- [AbstractDrawable] */

    override val boundingBox: Rectangle2D
        get() {
            val bb = super.boundingBox
            val lbb = label.boundingBox.moveBy(location)
            bb.add(lbb)
            return bb
        }

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
        label.draw(context)
    }

    override fun drawImpl(context: DrawContext) {
        val oldColor = context.g.color
        super.drawImpl(context)
        drawBodyDigital(context)
        drawFocus(context)
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
            context.g.drawRect(xInt, yInt, SIZE, SIZE)
            context.g.drawRoundRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                    SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH, DIAMETER, DIAMETER)
        })
        label.draw(context)
    }

    private fun drawBodyDigital(context: DrawContext) {
        val fillColor = Bit.of(model!!.isOn).color.foregroundColor
        context.g.color = color.backgroundColor
        context.g.fillRect(xInt, yInt, SIZE, SIZE)

        context.g.color = fillColor
        context.g.drawRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)
        context.g.fillRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                SIZE - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH)

        signalLabel.color = if (model!!.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor
        signalLabel.text = Bit.of(model!!.isOn).toHexString()
        signalLabel.draw(context)

        context.g.color = color.foregroundColor
        context.g.drawRect(x.toInt(), y.toInt(), SIZE, SIZE)
    }

    private fun drawFocus(context: DrawContext) {
        if (isFocusOwner) {
            context.g.color = Themes.get<AntaresTheme>().focus.color.foregroundColor
            context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
            context.g.drawRect(xInt + BORDER_WIDTH - 1, yInt + BORDER_WIDTH - 1,
                    SIZE - 2 * BORDER_WIDTH + 2, SIZE - 2 * BORDER_WIDTH + 2)
        }
    }

    private fun updateLabel() {
        invalidate()
        label.text = StringUtils.orEmpty(name)
        label.rotationChanged()
        invalidate()
    }

    private inner class InteractionHandler : ActorInteractionHandlerAdapter() {
        override fun mousePressed(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
            model!!.toggle(signalHandler)
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