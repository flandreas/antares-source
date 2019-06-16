package ch.scorpion.jabbah.base.fx

import javafx.beans.binding.NumberExpression
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.TextField
import java.math.BigInteger

/**
 * Copy/paste from org.controlsfx.property.editor in order to implement property editors for ControlFX's
 * PropertyPanel that support optional values.
 */
class NumericField(cls: Class<*>) : TextField() {

	private val value: NumericValidator<Number>

	init {

		value = if (cls == Byte::class.javaPrimitiveType || cls == Byte::class.java || cls == Short::class.javaPrimitiveType || cls == Short::class.java ||
			cls == Int::class.javaPrimitiveType || cls == Int::class.java || cls == Long::class.javaPrimitiveType || cls == Long::class.java ||
			cls == BigInteger::class.java) {
			LongValidator(this)
		} else {
			DoubleValidator(this)
		}

		focusedProperty().addListener { _, _, newValue ->
			if (!newValue) {
				value.value = value.toNumber(text)
			}
		}
	}

	fun valueProperty(): ObservableValue<Number> {
		return value
	}

	override fun replaceText(start: Int, end: Int, text: String) {
		if (replaceValid(start, end, text)) {
			super.replaceText(start, end, text)
		}
	}

	override fun replaceSelection(text: String) {
		val range = selection
		if (replaceValid(range.start, range.end, text)) {
			super.replaceSelection(text)
		}
	}

	private fun replaceValid(start: Int, end: Int, fragment: String): Boolean {
		try {
			val newText = text.substring(0, start) + fragment + text.substring(end)
			if (newText.isEmpty()) return true
			value.toNumber(newText)
			return true
		} catch (ex: Throwable) {
			return false
		}

	}


	private interface NumericValidator<out T : Number> : NumberExpression {
		fun setValue(num: Number)
		fun toNumber(s: String?): T

	}

	internal class DoubleValidator(private val field: NumericField)//$NON-NLS-1$
		: SimpleDoubleProperty(field, "value", 0.0), NumericValidator<Double> {

		override fun invalidated() {
			field.text = java.lang.Double.toString(get())
		}

		override fun toNumber(s: String?): Double {
			if (s == null || s.trim { it <= ' ' }.isEmpty()) return 0.0
			val d = s.trim { it <= ' ' }
			if (d.endsWith("f") || d.endsWith("d") || d.endsWith("F") || d.endsWith("D")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				throw NumberFormatException("There should be no alpha symbols") //$NON-NLS-1$
			}
			return d.toDouble()
		}

	}


	internal class LongValidator(private val field: NumericField)//$NON-NLS-1$
		: SimpleLongProperty(field, "value", 0L), NumericValidator<Long> {

		override fun invalidated() {
			field.text = java.lang.Long.toString(get())
		}

		override fun toNumber(s: String?): Long {
			if (s == null || s.trim { it <= ' ' }.isEmpty()) return 0L
			val d = s.trim { it <= ' ' }
			return d.toLong()
		}

	}


}