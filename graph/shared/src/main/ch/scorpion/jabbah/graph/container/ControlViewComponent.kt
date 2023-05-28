package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.Mirrorable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

/**
 * A [Component] that wraps a [ControlView] in order to allow deferred reference
 * to a [SubGraphVerticeView]'s model.
 */
class ControlViewComponent(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	source: ControlViewSource<Vertice>? = null,
	baseLink: DeepVerticeLink = DeepVerticeLink.EMPTY
) : AbstractComponent(styleProvider), GraphElementView<Vertice>, ActorView, Transparent, Mirrorable {

	/**
	 * The ID of the model displayed by [controlView]. This ID is made persistent
	 * and is used to resolve the link to the model when the underlying [Graph] gets bound.
	 */
	var controlModelLink: DeepVerticeLink
		private set

	lateinit var controlView: ControlView<Vertice>

	private var drawableOwner: DrawableOwner? = null

	/**
	 * Will be displayed instead of [controlView] if [controlModelLink] is broken,
	 * which isn't detected before [bindControlView] is called.
	 */
	private var brokenView: BrokenDeepLinkView? = null

	init {
		if (source != null) {
			controlView = source.createControlView()
			controlView.id = source.id
			controlView.isActiveControlView = true
			controlModelLink = baseLink.append(controlView.model.id)
			drawableOwner = DrawableOwner(this, controlView)
		} else {
			controlModelLink = DeepVerticeLink.EMPTY
		}
	}

	/** ---- UI related properties */

	val modelId: Int get() = if (controlModelLink.empty) 0 else controlModelLink.last

	val name: String get() = controlView.controlName

	/** ---- [Transparent] interface */

	override var transparency: Int
		get() = controlView.transparency
		set(value) {
			controlView.transparency = value
		}

	/** ---- [GraphElementView] interface */

	override val model: Vertice get() = controlView.model

	override fun bind(graphView: GraphView, deep: Boolean) { }

	override val isFullyConnected: Boolean get() = true

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeStorable("controlView", controlView)
		writer.writeString("controlModelId", controlModelLink.toStoreFormat())
		controlView.writeModelProperties(writer)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (drawableOwner != null) {
			drawableOwner!!.dispose()
		}
		controlView = reader.readStorable("controlView") as ControlView<Vertice>
		drawableOwner = DrawableOwner(this, controlView)
		controlModelLink = DeepVerticeLink.fromStoreFormat(reader.readString("controlModelId"))
		controlView.readModelProperties(reader)
		super.read(reader)
		controlView.isShowPortViews = false
		controlView.isActiveControlView = true
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape get() = controlView.boundingBox

	override fun draw(context: DrawContext) {
		if (brokenView != null) {
			brokenView!!.draw(context)
		} else {
			controlView.draw(context)
		}
	}

	override fun contains(x: Double, y: Double): Boolean = controlView.contains(x, y)

	/** ---- [Mirrorable] */

	override fun mirrorHorizontally(x: Double) {
		invalidate()
		location = location.mirrorHorizontally(x).addX(-controlView.mirrorWidth)
		invalidate()
		update()
	}

	override fun mirrorVertically(y: Double) {
		invalidate()
		location = location.mirrorVertically(y).addY(controlView.mirrorHeight)
		invalidate()
		update()
	}

	/** ---- [Locatable] */

	override var location: Point2D
		get() = controlView.location
		set(value) {
			controlView.location = value
		}

	/** ---- [Component] interface */

	override val type: String get() = controlView.type

	override val selectableComponent: Component get() = controlView

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = controlView.preferredSelectionDrawingStrategy
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler = controlView.getActorInteractionHandler(context)

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? = controlView.getExecutionTooltip(x, y)

	override fun executionStarted(signalHandler: SignalHandler) {
		controlView.executionStarted(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		controlView.executionStopped(signalHandler)
	}

	/** ---- [ControlViewComponent] */

	fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, graph: Graph, repository: MetaGraphRepository) {
		try {
			val vertice = controlModelLink.getLinkedVertice(graph, repository)
			controlView.bindControlView(subGraphVerticeView, vertice)
		} catch (e: IllegalArgumentException) {
			invalidate()
			brokenView = BrokenDeepLinkView(styleProvider, controlView.boundingBox)
			invalidate()
			update()
		}
	}

	/** A [Drawable] to be displayed when the [DeepVerticeLink] of the [ControlView] is broken.*/
	private class BrokenDeepLinkView(
		private val styleProvider: StyleProvider,
		bounds: RectangularShape
	) : AbstractRectangle(bounds.centerX - HALF_SIZE, bounds.centerY - HALF_SIZE, 2 * HALF_SIZE, 2 * HALF_SIZE) {

		companion object {
			private const val HALF_SIZE = 10.0
		}

		private val style: Style get() = styleProvider.getStyle(StyleType.FIGURE)

		private val color: CompositeColor = Themes.get<GraphTheme>().error

		private val label: Label = Label(
			text = "?",
			font = style.font,
			color = style.color.textColor,
			location = Point2D(bounds.centerX, bounds.centerY)
		)

		override fun draw(context: DrawContext) {
			drawRectangle(
				context,
				color.foregroundColor,
				color.backgroundColor,
				style.stroke)
			label.color = color.textColor
			label.draw(context)
		}

		override val lineWidth: Double get() = style.stroke.width.toDouble()

	}
}