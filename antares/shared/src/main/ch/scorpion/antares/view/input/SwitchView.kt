package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
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
    private val textRenderInfoFactory: TextRenderInfoFactory = DrawModule.textRenderInfoFactory,
    private val eventBus: EventBus = BaseModule.eventBus
) : DigitalComponentView<Switch>(styleProvider, model), ControlView<Switch>, ControlViewSource<Switch> {

    companion object {
        val LOG by logger(SwitchView::class)
        const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.SwitchView.iconPath"
        const val SIZE = 4 * Look.SCALE
        const val BORDER_WIDTH = 3
        const val DIAMETER = 12
        const val LABEL_DIST = Look.SCALE
        const val LABEL_INSET = 4.0
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
	        postControlViewSourceChangeEvent(eventBus)
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
            horizontalAlignment = HorizontalAlignment.CENTER,
            verticalAlignment = VerticalAlignment.CENTER,
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
	        if (value != model!!.name) {
		        model!!.name = value
		        updateLabels()
		        validate()
		        postControlViewSourceChangeEvent(eventBus)
	        }
        }

    /**
     * Controls the interactive behaviour of this [SwitchView]. If set to `true`, the [Switch]
     * stays in the new state when the user releases the mouse button. If set to `false`,
     * the [Switch] returns to 0 state.
     */
    var toggle: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

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

    override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
        return actorInteractionHandler
    }

    /** ---- [Component] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.REPLACE
        set(@Suppress("UNUSED_PARAMETER") value) {
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
	        if (StringUtils.isNotEmpty(externalLabel.text)) {
	            val lbb = externalLabel.boundingBox.moveBy(location)
	            bb.add(lbb)
	        }
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
        clone.location = Point2D.ZERO
	    copyControlViewProperties(this, clone)
        return clone
    }


    /** ---- [ControlView] */

    override fun bindToModel(model: Switch) {
        this.model = model
    }

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
	}

	override fun readModelProperties(reader: StoreReader) {
		if (reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<Switch>) {
		if (source is SwitchView) {
			copyControlViewProperties(source, this)
		}
	}

	private fun copyControlViewProperties(source: SwitchView, dest: SwitchView) {
		dest.name = source.name
		dest.labelPosition = source.labelPosition
		dest.toggle = source.toggle
	}

	/** ---- [SwitchView] */

    override fun drawSelected(context: DrawContext) {
        context.g.color = context.color!!.foregroundColor
        draw(context) {
	        super.drawImpl(it)
	        context.g.stroke = stroke
	        context.g.drawRect(xInt, yInt, width.toInt(), SIZE)
	        context.g.stroke = Themes.get<AntaresTheme>().annotation.stroke
	        context.g.drawRoundRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
		        width.toInt() - 2 * BORDER_WIDTH, SIZE - 2 * BORDER_WIDTH, DIAMETER, DIAMETER)
	        if (labelPosition == VerticeLabelPosition.INTERNAL) {
		        internalLabel.draw(context)
	        }
        }
	    if (labelPosition == VerticeLabelPosition.EXTERNAL) {
            externalLabel.draw(context)
        }
    }

    private fun drawBodyDigital(context: DrawContext) {
	    if (shadow) {
			DropShadow.draw(context) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
	    }

        context.g.color = transparent.applyTo(color.backgroundColor)
        context.g.fillRect(xInt, yInt, widthInt, heightInt)

        context.g.color = transparent.applyTo(Bit.of(model!!.isOn).color.foregroundColor)
        context.g.fillRect(xInt + BORDER_WIDTH, yInt + BORDER_WIDTH,
                widthInt - 2 * BORDER_WIDTH, heightInt - 2 * BORDER_WIDTH)

        context.g.color = transparent.applyTo(color.foregroundColor)
	    context.g.stroke = stroke
        context.g.drawRect(xInt, yInt, widthInt, heightInt)

        internalLabel.color = transparent.applyTo(if (model!!.isOn) Themes.get<AntaresTheme>().one.textColor else Themes.get<AntaresTheme>().zero.textColor)
        if (labelPosition == VerticeLabelPosition.INTERNAL) {
            internalLabel.text = StringUtils.orEmpty(model!!.name)
        } else {
            internalLabel.text = Bit.of(model!!.isOn).toHexString()
        }
        internalLabel.draw(context)
    }

    private fun drawFocus(context: DrawContext) {
        if (isFocusOwner) {
            context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
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
        update()
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
        val tri = textRenderInfoFactory.measureSingleLineText(model!!.name!!, font)
        val requiredSpace = tri.textBounds.width + 2 * LABEL_INSET
        return (SIZE * Math.max(1.0, Math.ceil(requiredSpace / SIZE))).toInt()
    }

    private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

        override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
            model!!.toggle(context.signalHandler)
	        context.mouseEvent?.consume()
	        requestFocus()
	        return this
        }

	    override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler? {
		    return this
	    }

        override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
            if (!toggle) {
                model!!.toggle(context.signalHandler)
	            context.mouseEvent?.consume()
            }
	        return null
        }

	    override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
		    context.mouseEvent?.consume()
		    return this
	    }

        override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
            when(context.keyEvent?.key) {
                '0'.toInt() -> model!!.setOn(context.signalHandler, false)
                '1'.toInt() -> model!!.setOn(context.signalHandler, true)
                '\n'.toInt() -> model!!.toggle(context.signalHandler)
            }
	        return null
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