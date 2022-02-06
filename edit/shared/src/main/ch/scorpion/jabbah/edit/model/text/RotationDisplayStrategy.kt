package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext

/** Defines how a [Label] reacts to a [Rotation] when drawing itself. */
enum class RotationDisplayStrategy {

	/** Doesn't react to owner rotation, i.e. the label text is fully rotated.*/
	IGNORE {
		override fun beforeDraw(context: DrawContext, label: Label) {
			// empty
		}

		override fun afterDraw(context: DrawContext, label: Label) {
			// empty
		}
	},

	/**
	 * Rotates the label text so that it is horizontal (when rotated 0 or 180 degrees),
	 * or so that it is written from upwards and can be read from left (when rotated 90 or 270 degrees)
	 */
	ROTATE_HALF {
		override fun beforeDraw(context: DrawContext, label: Label) {
			val pivot = label.rotation.rotatePointAround(label.location, label.bounds.center)
			context.g.translate(pivot)
			context.g.rotate(calculateRotation(label).angle)
			context.g.translate(pivot.negate)
		}

		override fun afterDraw(context: DrawContext, label: Label) {
			val pivot = label.rotation.rotatePointAround(label.location, label.bounds.center)
			context.g.translate(pivot)
			context.g.rotate(calculateRotation(label).angle)
			context.g.translate(pivot.negate)
		}

		private fun calculateRotation(label: Label): Rotation =
			when (label.ownerRotation.add(label.rotation)) {
				Rotation.R0 -> Rotation.R0
				Rotation.R180 -> Rotation.R180
				Rotation.R90 -> Rotation.R0
				Rotation.R270 -> Rotation.R180
			}
	},

	/** Keeps the label text always horizontal.*/
	KEEP_HORIZONTAL {
		override fun beforeDraw(context: DrawContext, label: Label) {
			val pivot = label.rotation.rotatePointAround(label.location, label.bounds.center)
			context.g.translate(pivot)
			context.g.rotate(-label.ownerRotation.angle)
			context.g.translate(pivot.negate)
		}

		override fun afterDraw(context: DrawContext, label: Label) {
			val pivot = label.rotation.rotatePointAround(label.location, label.bounds.center)
			context.g.translate(pivot)
			context.g.rotate(label.ownerRotation.angle)
			context.g.translate(pivot.negate)
		}
	};

	internal abstract fun beforeDraw(context: DrawContext, label: Label)
	internal abstract fun afterDraw(context: DrawContext, label: Label)

}