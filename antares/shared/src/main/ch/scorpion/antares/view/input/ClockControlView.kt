package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Clock
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.KnobIcon
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.execution.actor.AbstractActorIconButton
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class ClockControlView(
	initModel: Clock = Clock(),
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularComponent(styleType, styleProvider, Rectangle2D(0, 0, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)), ControlView<Clock> {

	companion object {
		private const val ICON_BUTTON_SIZE = 20
	}

	private val iconButton = IconButton(Point2D())

	/** ---- [Drawable] */

	override fun draw(context: DrawContext) {
		iconButton.draw(context)
	}

	override var location: Point2D
		get() = super.location
		set(value) {
			super.location = value
			iconButton.location = value
		}

	/** ---- [Component] */

	override val type: String get() = "TODO"

	/** ---- [Transparent] */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) { transparent.transparency = value }

	/** ---- [ControlView] */

	private var _model: Clock = initModel

	override val model: Clock get() = _model

	override val controlId: String get() = "clock:${model.id}"

	override val controlName: String get() = "$type (${model.id})"

	override var isShowPortViews: Boolean = false

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = iconButton.getActorInteractionHandler(context)

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? = null

	override fun bindToModel(model: Clock) {
		_model = model
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<Clock>) { }

	override fun writeModelProperties(writer: StoreWriter) { }

	override fun readModelProperties(reader: StoreReader) { }

	private inner class IconButton(
		location: Point2D
	) : AbstractActorIconButton(
		icon = KnobIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)),
		location = location
	) {
		override fun createActorInteractionHandler(): InputEventHandlerAdapter<ActorInteractionContext> = MouseMoveHandler()

		override fun handleClicked(context: ActorInteractionContext) {
			println("ClockControlView: handleClicked")
		}

		private inner class MouseMoveHandler : Handler() {
			override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
				println("ClockControlView: mouseMoved")

				// Hover highlighting
				if (super.mouseMoved(context) == null) {
					return null
				}
				return null
			}
		}
	}
}