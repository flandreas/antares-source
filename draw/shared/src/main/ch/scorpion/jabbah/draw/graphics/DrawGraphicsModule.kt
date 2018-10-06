package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Module definitions for the [ch.scorpion.jabbah.draw.graphics] package.
 */
object DrawGraphicsModule : AbstractModule() {

    val WHITE = CompositeColor(backgroundColor = Color.WHITE, foregroundColor = Color.BLACK)
    val BLACK = CompositeColor(backgroundColor = Color.BLACK, foregroundColor = Color.WHITE)
    val GRAY = CompositeColor(backgroundColor = Color(227, 227, 227), foregroundColor = Color(114, 114, 114), textColor = Color.BLACK)
    val RED = CompositeColor(backgroundColor = Color(248, 170, 145), foregroundColor = Color(236, 35, 46), textColor = Color.BLACK)
    val BLUE = CompositeColor(backgroundColor = Color(185, 223, 245), foregroundColor = Color(72, 186, 233), textColor = Color.BLACK)
    val GREEN = CompositeColor(backgroundColor = Color(198, 226, 184), foregroundColor = Color(115, 191, 91), textColor = Color.BLACK)
    val YELLOW = CompositeColor(backgroundColor = Color(251, 245, 183), foregroundColor = Color(254, 209, 58), textColor = Color.BLACK)
    val VIOLET = CompositeColor(backgroundColor = Color(211, 207, 231), foregroundColor = Color(91, 84, 161), textColor = Color.BLACK)
    val PINK = CompositeColor(backgroundColor = Color(250, 214, 223), foregroundColor = Color(234, 34, 123), textColor = Color.BLACK)

	private val dottedArray = floatArrayOf(1f, 5f)

	private val THIN_DOTTED = Stroke(0.5f, dash = dottedArray)
	private val THIN_SOLID = Stroke(0.5f)
	private val THIN_DASHED = Stroke(0.5f, dash = floatArrayOf(5f))
	private val NORMAL_DOTTED = Stroke(1.5f, dash = dottedArray)
	private val NORMAL_SOLID = Stroke(1.5f)
	private val NORMAL_DASHED = Stroke(1.5f, dash = floatArrayOf(5f))
	private val THICK_DOTTED = Stroke(3.0f, dash = dottedArray)
	private val THICK_SOLID = Stroke(3.0f)
	private val THICK_DASHED = Stroke(3.0f, dash = floatArrayOf(5f))

    /** ---- [AbstractModule] */

    override fun initialize() {
        BaseModule.require()
        predefineColors(PredefinedColorRepository)
	    predefineStrokes(PredefinedStrokeRepository)
    }

    /** ---- [DrawGraphicsModule] */

    private fun predefineColors(repository: PredefinedColorRepository) {
        repository.register(PredefinedColor(PredefinedColorIdentity.White, WHITE))
        repository.register(PredefinedColor(PredefinedColorIdentity.Black, BLACK))
        repository.register(PredefinedColor(PredefinedColorIdentity.Gray, GRAY))
        repository.register(PredefinedColor(PredefinedColorIdentity.Red, RED))
        repository.register(PredefinedColor(PredefinedColorIdentity.Blue, BLUE))
        repository.register(PredefinedColor(PredefinedColorIdentity.Green, GREEN))
        repository.register(PredefinedColor(PredefinedColorIdentity.Yellow, YELLOW))
    }

	private fun predefineStrokes(repository: PredefinedStrokeRepository) {
		repository.register(PredefinedStroke(PredefinedStrokeIdentity.ThinDotted, THIN_DOTTED))
		repository.register(PredefinedStroke(PredefinedStrokeIdentity.ThinSolid, THIN_SOLID))
		repository.register(PredefinedStroke(PredefinedStrokeIdentity.ThinDashed, THIN_DASHED))

		repository.register(PredefinedStroke(PredefinedStrokeIdentity.NormalDotted, NORMAL_DOTTED))
		repository.register(PredefinedStroke(PredefinedStrokeIdentity.NormalSolid, NORMAL_SOLID))
		repository.register(PredefinedStroke(PredefinedStrokeIdentity.NormalDashed, NORMAL_DASHED))

		repository.register(PredefinedStroke(PredefinedStrokeIdentity.ThickDotted, THICK_DOTTED))
		repository.register(PredefinedStroke(PredefinedStrokeIdentity.ThickSolid, THICK_SOLID))
		repository.register(PredefinedStroke(PredefinedStrokeIdentity.ThickDashed, THICK_DASHED))
	}
}