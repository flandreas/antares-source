package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Clock
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.IconDrawableButtonRenderer
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.KnobIcon
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorDrawableButton
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.ui.KnobLauncher
import ch.scorpion.jabbah.graph.ui.KnobLauncherImpl
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class ClockControlView(
	initModel: Clock = Clock(),
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	var knobLauncher: KnobLauncher = KnobLauncherImpl
) : AbstractRectangularComponent(styleType, styleProvider, Rectangle2D(0, 0, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)), ControlView<Clock> {

	companion object {
		const val ICON_BUTTON_SIZE = 20
	}

	private val iconButton = IconButton(Point2D())

	init {
		DrawableOwner(this, iconButton)
	}

	/** ---- [Drawable] */

	override fun draw(context: DrawContext) {
		iconButton.draw(context)
	}

	/** ---- [AbstractRectangularComponent] */

	override fun setFrame(x: Double, y: Double, width: Double, height: Double) {
		super.setFrame(x, y, width, height)
		iconButton.location = Point2D(x, y)
	}

	/** ---- [Component] */

	override val type: String get() = Translations.getString("${Clock.BASE_RESOURCE_KEY}.name")

	/** ---- [Transparent] */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) { transparent.transparency = value }

	/** ---- [ControlView] */

	private var subGraphVerticeView: SubGraphVerticeView<*>? = null

	private var _model: Clock = initModel

	override val model: Clock get() = _model

	override val controlId: String get() = "clock:${model.id}"

	override val controlName: String get() = "$type (${model.id})"

	override var isShowPortViews: Boolean = false

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = width

	override val mirrorHeight: Double get() = -height

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = iconButton.getActorInteractionHandler(context)

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? = null

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		_model = link.getLinkedObject(startGraph) as Clock
		this.subGraphVerticeView = subGraphVerticeView
	}

	override fun executionStarted(signalHandler: SignalHandler) { }

	override fun executionStopped(signalHandler: SignalHandler) { }

	override fun sourcePropertiesChanged(source: ControlViewSource<Clock>) { }

	override fun writeModelProperties(writer: StoreWriter) { }

	override fun readModelProperties(reader: StoreReader) { }

	private inner class IconButton(
		location: Point2D
	) : ActorDrawableButton<EditInputEventContext>(
		renderer = IconDrawableButtonRenderer(KnobIcon(Dimension2D(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE))),
		location = location,
		actorAction = {}
	) {

		override fun createActorInteractionHandler(): InputEventHandlerAdapter<ActorInteractionContext> = MouseMoveHandler()

		private inner class MouseMoveHandler : ActorHandler() {

			override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
				// Hover highlighting
				if (super.mouseMoved(context) == null) {
					return null
				}

				return knobLauncher.launchAfterDelay(
					initialValue = model.propagationDelay.value / 1_000,
					location = knobLocation,
					unit = "µs",
					mouseMovedCondition = { keepMouseMoved(it.location) },
					displayHandler = { isHovering = false },
					valueChangeHandler = { model.propagationDelay = LongValueImpl(it * 1_000) },
					signalHandler = context.signalHandler
				)
			}

			private val knobLocation: Point2D get() {
				val center = boundingBox.center
				val rotatedCenter = subGraphVerticeView!!.rotation.rotatePointAround(Point2D.ZERO, center.x, center.y)
				return rotatedCenter.add(subGraphVerticeView!!.location)
			}
		}
	}
}