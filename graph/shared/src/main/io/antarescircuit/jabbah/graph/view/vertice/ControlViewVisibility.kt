package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.view.ControlView

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