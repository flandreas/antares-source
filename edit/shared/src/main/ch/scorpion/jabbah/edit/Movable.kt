package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.edit.select.MoveCommand

/**
 * Represents a [Locatable] that can be moved interactively.
 */
interface Movable : Locatable {

	companion object {

		/** Moves a [Collection] of [Locatable]s all by the same offset.*/
		fun moveBy(movables: Collection<Movable>, offset: Point2D) {
			movables.forEach { it.prepareMoveBy(movables) }
			movables.forEach { it.moveBy(offset.x, offset.y) }
			movables.forEach { it.completeMoveBy() }
		}

		/** Drags a [Collection] of [Locatable]s all by the same offset.*/
		fun dragBy(editor: Editor, movables: Collection<Movable>, offset: Point2D) {
			moveBy(movables, offset)
			if (movables.size == 1) {
				movables.first().dragged(editor)
			}
		}

		fun dragFinished(locatables: Collection<Movable>) {
			locatables.forEach { it.dragFinished() }
		}
	}

	/** The unique identification of this [Movable] in its containing [DrawableContainer]*/
	val id: Int

	/** Informs this [Movable] that it is about to be moved together with other [Movable]s.*/
	fun prepareMoveBy(components: Collection<Movable>) {}

	/**
	 * Notifies this [Movable] that it has been dragged.
	 * Called only if this [Movable] is the only one currently being dragged.
	 */
	fun dragged(editor: Editor) {
		// empty
	}

	fun dragFinished() {
		// empty
	}

	fun getMoveCommand(editor: Editor, offset: Point2D): Command = MoveCommand(editor, listOf(id), offset)

	/** Informs this [Movable] that moving previously announced by [prepareMoveBy] has been completed for all [Movable]s.*/
	fun completeMoveBy() {}
}