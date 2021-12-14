package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.view.ControlView

enum class ControlViewVisibility(override val customName: String) : EnumProperty<ControlViewVisibility> {
	Never("never") {
		override fun drawFilter(drawable: Drawable, context: DrawContext): Boolean =
			drawable !is ControlViewComponent
	},

	Simulation("simulation") {
		override fun drawFilter(drawable: Drawable, context: DrawContext): Boolean =
			drawable !is ControlViewComponent || context.castedAppContext<GraphApplicationContext>()!!.isExecute
	},

	Always("always") {
		override fun drawFilter(drawable: Drawable, context: DrawContext): Boolean = true
	};

	companion object {

		val DEFAULT = Simulation
		const val BASE_KEY = "graph.property.controlViewVisibility"

		fun withName(customName: String): ControlViewVisibility =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown ControlViewVisibility $customName")
	}

	override fun toString(): String = Translations.getString("$BASE_KEY.$customName")

	abstract fun drawFilter(drawable: Drawable, context: DrawContext): Boolean
}