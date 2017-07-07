package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Module definitions for the [jabbah.draw.graphics] package.
 */
object DrawGraphicsModule : AbstractModule() {

    /** ---- [AbstractModule] */

    override fun initialize() {
        BaseModule.require()
        predefineColors(PredefinedColorRepository)
    }

    /** ---- [DrawGraphicsModule] */

    private fun predefineColors(repository: PredefinedColorRepository) {
        repository.register(PredefinedColor(
            "white",
            "graphics.color.white.name",
            CompositeColor(
                backgroundColor = Color.WHITE,
                foregroundColor = Color.BLACK)))

        repository.register(PredefinedColor(
                "black",
                "graphics.color.black.name",
                CompositeColor(
                        backgroundColor = Color.BLACK,
                        foregroundColor = Color.WHITE)))

        repository.register(PredefinedColor(
                "gray",
                "graphics.color.gray.name",
                CompositeColor(
                        backgroundColor = Color(227, 227, 227),
                        foregroundColor = Color(114, 114, 114))))

        repository.register(PredefinedColor(
                "red",
                "graphics.color.red.name",
                CompositeColor(
                        backgroundColor = Color(248, 170, 145),
                        foregroundColor = Color(236, 35, 46))))

        repository.register(PredefinedColor(
                "blue",
                "graphics.color.blue.name",
                CompositeColor(
                        backgroundColor = Color(185, 223, 245),
                        foregroundColor = Color(72, 186, 233))))

        repository.register(PredefinedColor(
                "green",
                "graphics.color.green.name",
                CompositeColor(
                        backgroundColor = Color(198, 226, 184),
                        foregroundColor = Color(115, 191, 91))))

        repository.register(PredefinedColor(
                "yellow",
                "graphics.color.yellow.name",
                CompositeColor(
                        backgroundColor = Color(251, 245, 183),
                        foregroundColor = Color(245, 235, 62))))
    }
}