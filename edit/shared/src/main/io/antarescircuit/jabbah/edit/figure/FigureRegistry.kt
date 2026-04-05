package io.antarescircuit.jabbah.edit.figure

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Ellipse2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.model.rectangle.EllipseComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent

/**
 * A registry for registering factories for [Figure] grouped by [FigureGroups][FigureGroup].
 */
object FigureRegistry {

	/** The default translation key of the the default [FigureGroup] for "Geometrical Figures". */
	const val GEOMETRICAL_FIGURE_GROUP_KEY = "edit.figure.geometrical"

	private val _groups = mutableListOf<FigureGroup>()

	val groups: Iterator<FigureGroup> get() = _groups.iterator()

	fun registerGroup(name: String): FigureGroup {
		if (_groups.any { it.name == name }) {
			throw IllegalArgumentException("FigureGroup $name already registered")
		}
		return FigureGroup(name).also { _groups.add(it) }
	}

	fun getGroup(name: String): FigureGroup =
		_groups.first { it.name == name }

	fun registerDefaultGeometricalFigures() {
		registerGroup(Translations.getString(GEOMETRICAL_FIGURE_GROUP_KEY)).apply {
			register(FigureProvider(RectangleComponent.TYPE) { RectangleComponent(shape = Rectangle2D(0, 0, 60, 30)) })
			register(FigureProvider(EllipseComponent.TYPE) { EllipseComponent(shape = Ellipse2D(0, 0, 60, 30)) })
		}
	}
}

fun interface FigureFactory {
	fun create(): Figure
}

data class FigureProvider(
	val name: String,
	val factory: FigureFactory
)

class FigureGroup(val name: String) {

	private val _providers = mutableListOf<FigureProvider>()

	val providers: Iterator<FigureProvider> get() = _providers.iterator()

	val size: Int get() = _providers.size

	fun register(provider: FigureProvider) {
		_providers.add(provider)
	}
}