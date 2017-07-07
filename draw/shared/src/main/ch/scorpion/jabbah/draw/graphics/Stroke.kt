package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.exception.IllegalArgumentException

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
 * An implementation of the [Stroke] interface mainly to be used for targeting the JavaScript platform.
 */
data class Stroke(
        val width: Float = 1.0f,
        val cap: LineCap = LineCap.SQUARE,
        val join: LineJoin = LineJoin.MITER,
        val miterLimit: Float = 10.0f,
        val dash: FloatArray? = null,
        val dashPhase: Float? = null)
{
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
}




