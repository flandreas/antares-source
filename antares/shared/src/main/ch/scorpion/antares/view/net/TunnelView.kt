package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.GraphApplicationContext

/**
 * A view representation of a [Tunnel].
 */
class TunnelView(
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        model: Tunnel = Tunnel()
) : DigitalComponentView<Tunnel>(styleProvider, "library.element.Tunnel", model) {

    companion object {
        val SIZE = 4 * 7
        val LABEL_DIST = Look.SCALE
    }

    private val label = HorizontalLabel(
            owner = this,
            relLocation = Point2D(SIZE / 2 + (DigitalPortView.LENGTH + SIZE / 2) + LABEL_DIST, 0),
            font = font
    )

    init {
        modelExchanged(null)
        setBounds(getInput().unconnectedLength - SIZE / 2, -SIZE / 2, SIZE, SIZE)
    }

    override fun modelExchanged(oldModel: Tunnel?) {
        super.modelExchanged(oldModel)
        val portView = DigitalPortView(
            styleProvider = styleProvider,
            port = model!!.getPort(),
            direction = Direction.WEST,
            length = DigitalPortView.LENGTH + SIZE / 2,
            predefinedConnectedLength = SIZE / 2)
        portView.setLocation(portView.length.toDouble(), 0.0)
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

    var bitWidth: BitWidth
        get() = model!!.bitWidth
        set(value) {
            invalidate()
            model!!.bitWidth = value
            invalidate()
        }

    /** ---- [AbstractDrawable] */

    override fun draw(context: DrawContext) {
        super.draw(context)
        context.g.color = context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).textColor
        label.draw(context)
    }

    override fun drawImpl(context: DrawContext) {

        context.g.color = context.choose(color).backgroundColor
        context.g.fillRect(xInt, yInt, SIZE, SIZE)

        context.g.color = context.choose(color).foregroundColor
        context.g.stroke = stroke
        context.g.drawRect(xInt, yInt, SIZE, SIZE)

        // Draw the PortView above the border
        super.drawImpl(context)

        if (ApplicationMode.EXECUTE == context.castedAppContext<GraphApplicationContext>()!!.mode) {
            context.g.color = model!!.getInOrOutSignal().getColor().foregroundColor
        }
        context.g.fillOval(xInt + SIZE / 4, yInt + SIZE / 4, SIZE / 2, SIZE / 2)
    }

    override val boundingBox: Rectangle2D
        get() {
            val bb = super.boundingBox
            val lbb = label.boundingBox.moveBy(location)
            bb.add(lbb)
            return bb
        }

    /** ---- [AbstractComponent] */

    override fun rotationChanged(newRotation: Rotation) {
        super.rotationChanged(newRotation)
        label.rotationChanged()
    }

    /** ---- [TunnelView] */

    private fun updateLabel() {
        invalidate()
        label.text = StringUtils.orEmpty(name)
        label.rotationChanged()
        invalidate()
    }
}