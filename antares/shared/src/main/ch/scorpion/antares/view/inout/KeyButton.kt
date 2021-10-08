package ch.scorpion.antares.view.inout

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView

class KeyButton(
	location: Point2D = Point2D.ZERO,
	character: Char,
	private val key: Int,
	size: Int,
	private val handler: (Int) -> Unit
) : AbstractRectangle(location.x, location.y, size.toDouble(), size.toDouble()), ActorView {

	companion object {
		private val STROKE = Themes.get<DrawTheme>().annotation.stroke
	}

	private val actorInteractionHandler = Handler()

	private val label = Label(
		text = character.toString(),
		font = Themes.get<DrawTheme>().figure.font,
		color = Color.GREEN,
		location = bounds.center)

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		actorInteractionHandler

	override fun draw(context: DrawContext) {
		context.g.color = Color.GREEN
		context.g.draw(bounds)
		label.draw(context)
	}

	override val lineWidth: Double get() = STROKE.width.toDouble()

	private inner class Handler : InputEventHandlerAdapter<ActorInteractionContext>() {
		override fun mouseClicked(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			handler(key)
			return null
		}
	}
}