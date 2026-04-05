package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.module.DrawModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.draw.graphics] package.
 */
object DrawGraphicsModule : AbstractModule() {

    val RED = CompositeColor.withDarkerText(Color(236, 35, 46), Color(248, 170, 145))
	val RED_ON_DARK = CompositeColor(Color(236, 35, 46), Color(90, 3, 7), Color(255, 204, 204))

    val BLUE = CompositeColor.withDarkerText(Color(72, 186, 233), Color(185, 223, 245))
	val BLUE_ON_DARK = CompositeColor.withBrighterText(Color(44, 116, 179), Color(20,66,114))

    val GREEN = CompositeColor.withDarkerText(Color(115, 191, 91), Color(198, 226, 184))
	val GREEN_ON_DARK = CompositeColor.withBrighterText(Color(115, 191, 91), Color(7, 87, 9))

    val YELLOW = CompositeColor.withDarkerText(Color(254, 209, 58), Color(251, 245, 183))
	val YELLOW_ON_DARK = CompositeColor(Color(245, 235, 62), Color(67, 69, 10), Color(255, 255, 228))

    val VIOLET = CompositeColor.withDarkerText(Color(91, 84, 161), Color(211, 207, 231))
	val VIOLET_ON_DARK = CompositeColor.withBrighterText(Color(125, 108, 171), Color(55, 14, 91))

    val PINK = CompositeColor.withDarkerText(Color(234, 34, 123), Color(250, 214, 223))
	val PINK_ON_DARK = CompositeColor.withBrighterText(Color(188, 126, 179), Color(104, 8, 89))

	val BROWN = CompositeColor.withDarkerText(Color(149, 92, 11), Color(234, 173, 85))
	val BROWN_ON_DARK = CompositeColor(Color(203,119,18), Color(94,65,4), Color(255, 197, 109))

	val TURQUOISE = CompositeColor.withDarkerText(Color(69, 181, 172), Color(187, 255, 242))
	val TURQUOISE_ON_DARK = CompositeColor.withBrighterText(Color(69,213,203), Color(12,86,103))

    val WHITE = CompositeColor(Color.BLACK, Color.WHITE)
    val BLACK = CompositeColor(Color.WHITE, Color.BLACK)
    val GRAY = CompositeColor(Color(114, 114, 114), Color(227, 227, 227))
	val GRAY_ON_DARK = CompositeColor.withBrighterText(Color(104, 104, 104), Color(64, 64, 64))

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
	    fillProperties(DrawModule.properties)
    }

	override fun resetDependencies() {
		BaseModule.reset()
	}

    /** ---- [DrawGraphicsModule] */

    private fun fillProperties(properties: Properties) {
	    properties.set(DropShadow.PROP_SHADOW, false)
	    properties.set(DropShadow.PROP_OFFSET, 2)
    }

    private fun predefineColors(repository: PredefinedColorRepository) {
        repository.register(PredefinedColor(PredefinedColorIdentity.White, WHITE))
        repository.register(PredefinedColor(PredefinedColorIdentity.Black, BLACK))
        repository.register(PredefinedColor(PredefinedColorIdentity.Gray, GRAY))
        repository.register(PredefinedColor(PredefinedColorIdentity.Yellow, YELLOW))
        repository.register(PredefinedColor(PredefinedColorIdentity.Brown, BROWN))
        repository.register(PredefinedColor(PredefinedColorIdentity.Red, RED))
        repository.register(PredefinedColor(PredefinedColorIdentity.Violet, VIOLET))
        repository.register(PredefinedColor(PredefinedColorIdentity.Blue, BLUE))
        repository.register(PredefinedColor(PredefinedColorIdentity.Turquoise, TURQUOISE))
        repository.register(PredefinedColor(PredefinedColorIdentity.Green, GREEN))
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