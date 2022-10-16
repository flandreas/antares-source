package ch.scorpion.jabbah.edit.figure

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Ellipse2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.model.rectangle.EllipseComponent
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent

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
			register { RectangleComponent(shape = Rectangle2D(0, 0, 60, 30)) }
			register { EllipseComponent(shape = Ellipse2D(0, 0, 60, 30)) }
		}
	}
}

fun interface FigureFactory {
	fun create(): Figure
}

class FigureGroup(val name: String) {

	private val _factories = mutableListOf<FigureFactory>()

	val factories: Iterator<FigureFactory> get() = _factories.iterator()

	val size: Int get() = _factories.size

	fun register(factory: FigureFactory) {
		_factories.add(factory)
	}
}