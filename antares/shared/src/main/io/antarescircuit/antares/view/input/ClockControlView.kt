package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.Clock
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventHandlerAdapter
import io.antarescircuit.jabbah.draw.drawable.IconDrawableButtonRenderer
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.draw.drawable.TransparentImpl
import io.antarescircuit.jabbah.draw.graphics.KnobIcon
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.EditInputEventContext
import io.antarescircuit.jabbah.edit.model.rectangle.AbstractRectangularComponent
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorDrawableButton
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.ui.knob.KnobLauncher
import io.antarescircuit.jabbah.graph.ui.knob.KnobLauncherImpl
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class ClockControlView(
	initModel: Clock = Clock(),
	styleType: StyleType = StyleType.FIGURE,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	var knobLauncher: KnobLauncher = KnobLauncherImpl
) : AbstractGraphElementView<Clock>(styleProvider, styleType, initModel), ControlView<Clock> {

	companion object {
		const val ICON_BUTTON_SIZE = 20
	}

	private val iconButton = IconButton(Point2D())

	private val shape =  Rectangle2D(0, 0, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)

	init {
		DrawableOwner(this, iconButton)
	}

	/** ---- [Locatable] interface */

	override var location: Point2D
		get() = shape.topLeft
		set(value) {
			setFrame(value.x, value.y, shape.width, shape.height)
		}

	private fun setFrame(x: Double, y: Double, width: Double, height: Double) {
		invalidate()
		shape.setFrame(x, y, width, height)
		iconButton.location = Point2D(x, y)
		invalidate()
		update()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("x", shape.x)
		writer.writeDouble("y", shape.y)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		location = Point2D(
			reader.readDouble("x"),
			reader.readDouble("y"))
	}


	/** ---- [Drawable] */

	override val boundingBox: RectangularShape get() = AbstractRectangularComponent.createBoundingBox(this, shape)

	override fun draw(context: DrawContext) {
		iconButton.draw(context)
	}

	override fun contains(x: Double, y: Double): Boolean = shape.contains(x, y)

	/** ---- [Component] */

	override val type: String get() = Translations.getString("${Clock.BASE_RESOURCE_KEY}.name")

	/** ---- [Transparent] */

	private val transparent = TransparentImpl(this)

	override var transparency: Int
		get() = transparent.transparency
		set(value) { transparent.transparency = value }

	/** ---- [GraphElementView] */

	override val isFullyConnected: Boolean get() = true

	/** ---- [ControlView] */

	private var subGraphVerticeView: SubGraphVerticeView<*>? = null

	override val controlId: String get() = "clock:${model.id}"

	override val controlName: String get() = "$type (${model.id})"

	override var isShowPortViews: Boolean = false

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = shape.width

	override val mirrorHeight: Double get() = -shape.height

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = iconButton.getActorInteractionHandler(context)

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		model = link.getLinkedObject(startGraph) as Clock
		this.subGraphVerticeView = subGraphVerticeView
	}

	override fun executionStarted(signalHandler: SignalHandler) { }

	override fun executionStopped(signalHandler: SignalHandler) { }

	override fun sourcePropertiesChanged(source: ControlViewSource<Clock>) { }

	override fun writeModelProperties(writer: StoreWriter) { }

	override fun readModelProperties(reader: StoreReader) { }

	/** ---- [ClockControlView] */

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
					initialValue = model.periodOrFrequency,
					location = knobLocation,
					mouseMovedCondition = { keepMouseMoved(it.location) },
					displayHandler = { isHovering = false },
					valueChangeHandler = {
						model.periodOrFrequency = it
					 },
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