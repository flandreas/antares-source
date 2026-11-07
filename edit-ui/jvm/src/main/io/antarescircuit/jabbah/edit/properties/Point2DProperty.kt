package io.antarescircuit.jabbah.edit.properties

import com.l2fprod.common.beans.editor.StringPropertyEditor
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.swing.ToStringRenderer
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.componentBeanProvider
import javax.swing.JTextField

/** A command-backed property for editing [Point2D] values as comma-separated coordinates. */
class Point2DProperty(
	propertyName: String = "location",
	baseKey: String = Component.BASE_KEY_LOCATION,
	beanProvider: BeanProvider = componentBeanProvider
) : CommandPropertySwing<Point2D>(propertyName, baseKey, Point2D::class.java, beanProvider) {

	var parseException: IllegalArgumentException? = null

	override fun writeToBeans(force: Boolean) {
		parseException?.let { throw it }
		super.writeToBeans(force)
	}

	override fun readFromObject(bean: Any?) {
		super.readFromObject(bean)
		parseException = null
	}
}

class Point2DRenderer : ToStringRenderer<Point2D>() {

	companion object {
		fun format(point: Point2D): String = "${formatCoordinate(point.x)},${formatCoordinate(point.y)}"

		private fun formatCoordinate(coordinate: Double): String = coordinate.toString().removeSuffix(".0")
	}

	override fun setValue(value: Any?) {
		text = (value as Point2D?)?.let { format(it) } ?: ""
	}
}

open class Point2DEditor(
	private val errorCallback: (IllegalArgumentException) -> Unit
) : StringPropertyEditor() {

	companion object {

		fun parse(text: String): Point2D {
			val coordinates = text.split(',').map { it.trim() }
			if (coordinates.size != 2) {
				throw IllegalArgumentException("A point must contain an x and y coordinate separated by a comma")
			}

			val (x, y) = try {
				coordinates[0].toDouble() to coordinates[1].toDouble()
			} catch (e: NumberFormatException) {
				throw IllegalArgumentException(Translations.getString("edit.property.location.invalid.text"), e)
			}
			if (!x.isFinite() || !y.isFinite()) {
				throw IllegalArgumentException("Point coordinates must be finite")
			}
			return Point2D(x, y)
		}
	}

	override fun getValue(): Any {
		return try {
			parse((editor as JTextField).text)
		} catch (e: IllegalArgumentException) {
			errorCallback(e)
			// CellEditorAdapter calls getValue while stopping editing, outside the property panel's error handler.
			// Return a distinct value so the property sheet invokes writeToBeans(), where the stored exception is rethrown.
			Unit
		}
	}

	override fun setValue(value: Any?) {
		val point = value as Point2D?
		(editor as JTextField).text = point?.let(Point2DRenderer::format) ?: ""
	}
}
