package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Module definitions for the [jabbah.draw.graphics] package.
 */
object DrawGraphicsModule : AbstractModule() {

    val WHITE = CompositeColor(backgroundColor = Color.WHITE, foregroundColor = Color.BLACK)
    val BLACK = CompositeColor(backgroundColor = Color.BLACK, foregroundColor = Color.WHITE)
    val GRAY = CompositeColor(backgroundColor = Color(227, 227, 227), foregroundColor = Color(114, 114, 114))
    val RED = CompositeColor(backgroundColor = Color(248, 170, 145), foregroundColor = Color(236, 35, 46))
    val BLUE = CompositeColor(backgroundColor = Color(185, 223, 245), foregroundColor = Color(72, 186, 233))
    val GREEN = CompositeColor(backgroundColor = Color(198, 226, 184), foregroundColor = Color(115, 191, 91))
    val YELLOW = CompositeColor(backgroundColor = Color(251, 245, 183), foregroundColor = Color(254, 209, 58))

    /** ---- [AbstractModule] */

    override fun initialize() {
        BaseModule.require()
        predefineColors(PredefinedColorRepository)
    }

    /** ---- [DrawGraphicsModule] */

    private fun predefineColors(repository: PredefinedColorRepository) {
        repository.register(PredefinedColor("white", "graphics.color.white.name", WHITE))
        repository.register(PredefinedColor("black", "graphics.color.black.name", BLACK))
        repository.register(PredefinedColor("gray", "graphics.color.gray.name", GRAY))
        repository.register(PredefinedColor("red", "graphics.color.red.name", RED))
        repository.register(PredefinedColor("blue", "graphics.color.blue.name", BLUE))
        repository.register(PredefinedColor("green", "graphics.color.green.name", GREEN))
        repository.register(PredefinedColor("yellow", "graphics.color.yellow.name", YELLOW))
    }
}