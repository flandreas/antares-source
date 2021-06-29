package ch.scorpion.jabbah.draw.graphics

enum class LineCap {
	BUTT,
	ROUND,
	SQUARE
}

enum class LineJoin {
	MITER,
	ROUND,
	BEVEL
}

/**
 * A [Stroke] contains platform-independent definitions of how lines and shapes are rendered.
 */
data class Stroke(
	val width: Float = 1.0f,
	val cap: LineCap = LineCap.SQUARE,
	val join: LineJoin = LineJoin.MITER,
	val miterLimit: Float = 10.0f,
	val dash: FloatArray? = null,
	val dashPhase: Float? = null) {

	init {
		if (width < 0.0) {
			throw IllegalArgumentException("negative width")
		}
		if (join == LineJoin.MITER && miterLimit < 1.0) {
			throw IllegalArgumentException("miterlimit < 1")
		}
		if (dash != null) {
			if (dashPhase != null && dashPhase < 0.0) {
				throw IllegalArgumentException("negative dash phase")
			}
			var allZero = false
			for (value in dash) {
				if (value > 0.0) {
					allZero = false
				} else if (value < 0.0) {
					throw IllegalArgumentException("negative dash length")
				}
			}
			if (allZero) {
				throw IllegalArgumentException("dash lengths all zero")
			}
		}
	}

	val thinner: Stroke get() = this.copy(width = this.width * 0.7f)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as Stroke

		if (width != other.width) return false
		if (cap != other.cap) return false
		if (join != other.join) return false
		if (miterLimit != other.miterLimit) return false
		if (dash != null) {
			if (other.dash == null) return false
			if (!dash.contentEquals(other.dash)) return false
		} else if (other.dash != null) return false
		if (dashPhase != other.dashPhase) return false

		return true
	}

	override fun hashCode(): Int {
		var result = width.hashCode()
		result = 31 * result + cap.hashCode()
		result = 31 * result + join.hashCode()
		result = 31 * result + miterLimit.hashCode()
		result = 31 * result + (dash?.contentHashCode() ?: 0)
		result = 31 * result + (dashPhase?.hashCode() ?: 0)
		return result
	}
}




