package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.Themes


/**
 * A view representation of a [Switch] that switches on when the mouse is pressed, and that switches off when the
 * mouse is released.
 */
class ToggleButtonView(
    styleProvider: StyleProvider,
    model: Switch
) : DigitalComponentView<Switch>(styleProvider, "library.element.Toggle", model), ControlViewSource<Switch>, ControlView<Switch> {

    constructor(styleProvider: StyleProvider): this(styleProvider, Switch())
    @Suppress("unused") constructor(): this(DrawStyleModule.styleProvider)

    companion object {
        val PROP_ICON_PATH = "ch.scorpion.antares.view.input.ToggleButtonView.iconPath"
        val SIZE = 4 * Look.SCALE
        val INSET = 4
        val DIAMETER = 6
        val UL_TRIANGLE = System.get().createPath()
            .moveTo(0, 0)
            .lineTo(SIZE, 0)
            .lineTo(0, SIZE)
            .close()
        val LR_TRIANGLE = System.get().createPath()
            .moveTo(SIZE, SIZE)
            .lineTo(0, SIZE)
            .lineTo(SIZE, 0)
            .close()
    }

    /** Handles mouse interactions during execution*/
    private val actorInteractionHandler = InteractionHandler()

    init {
        modelExchanged(null)
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

    /** ---- [AbstractDrawable] */

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        drawBody(context)
    }

    /** ---- [ControlViewSource] */

    override val controlId: String
        get() {
            // Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
            // when ControlViews (event as part of a wrapping Component) are added to a Drawing
            return "toggle:" + model!!.id
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
        val clone = ToggleButtonView(styleProvider, model!!)
        clone.isShowPortViews = false
        clone.location = Point2D()
        return clone
    }

    /** ---- [ControlView] */

    override fun bindToModel(model: Switch) {
        this.model = model
    }

    /** ---- [ToggleButtonView] */

    fun drawSelected(context: DrawContext) {
        draw(context, {
            super.drawImpl(it)
            context.g.color = context.color!!.foregroundColor
            context.g.drawRect(xInt, yInt, SIZE, SIZE)
            context.g.drawRoundRect(xInt + INSET, yInt + INSET, SIZE - 2 * INSET,
                    SIZE - 2 * INSET, DIAMETER, DIAMETER)
        })
    }

    private fun drawBody(context: DrawContext) {
        val oldColor = context.g.color

        context.g.translate(x, y)

        // Compensate rotation in order to maintain 3D effect of rotated button
        context.g.translate(SIZE / 2.0, SIZE / 2.0)
        context.g.rotate(-rotation.angle)
        context.g.translate(-SIZE / 2.0, -SIZE / 2.0)

        val bright = Color.WHITE
        val dark = Color.GRAY
        val normal = Color.LIGHT_GRAY

        context.g.color = if (model!!.isOn) dark else bright
        context.g.fill(UL_TRIANGLE)

        context.g.color = if (model!!.isOn) bright else dark
        context.g.fill(LR_TRIANGLE)

        // Undo rotation compensation for 3D effect of rotated button
        context.g.translate(SIZE / 2.0, SIZE / 2.0)
        context.g.rotate(rotation.angle)
        context.g.translate(-SIZE / 2.0, -SIZE / 2.0)

        context.g.color = normal
        context.g.fillRoundRect(INSET, INSET, SIZE - 2 * INSET, SIZE - 2 * INSET, DIAMETER, DIAMETER)

        context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
        context.g.color = Color.GRAY
        context.g.drawRect(0, 0, SIZE, SIZE)

        context.g.translate(-x, -y)

        context.g.color = oldColor
    }

    private inner class InteractionHandler : ActorInteractionHandlerAdapter() {
        override fun mousePressed(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
            model!!.toggle(signalHandler)
        }
        override fun mouseReleased(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
            model!!.toggle(signalHandler)
        }
    }
}

class ToggleButtonViewSelectionModel(component: ToggleButtonView) : AbstractSelectionModel<ToggleButtonView>(component) {

    override fun draw(context: DrawContext) {
        val oldUseContextColors = context.useContextColors
        context.useContextColors = true
        context.color = Themes.get<AntaresTheme>().selection
        component.drawSelected(context)
        context.useContextColors = oldUseContextColors
    }

    override val boundingBox: RectangularShape
        get() = component.boundingBox

    override fun contains(x: Double, y: Double): Boolean {
        return component.contains(x, y)
    }

    /** ---- [AbstractSelectionModel]  */

    override fun componentUpdated() {
        validate()
    }
}