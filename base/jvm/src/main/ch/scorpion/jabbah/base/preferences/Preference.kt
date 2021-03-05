package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.PropertiesProxy
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import java.text.DecimalFormat
import javax.swing.JCheckBox
import javax.swing.JFormattedTextField
import javax.swing.JTextField
import javax.swing.text.NumberFormatter


class PreferenceGroup(
	private val nameKey: String
) {

	/** Used to maintain the desired sort order of the added [PreferenceGroup].*/
	private val _groupsList = mutableListOf<PreferenceGroup>()

	/** Maps the nameKey of a [PreferenceGroup] to the corresponding [PreferenceGroup].*/
	private val _groupsMap = mutableMapOf<String,PreferenceGroup>()

	private val _preferences = mutableListOf<Preference>()

	val children: Iterator<PreferenceGroup> get() = _groupsList.iterator()

	val preferences: Iterator<Preference> get() = _preferences.iterator()

	/** Returns the displayable, translated name of this [PreferenceGroup].*/
	val name: String get() = Translations.getString(nameKey)

	override fun toString(): String = name

	fun add(group: PreferenceGroup): PreferenceGroup {
		_groupsList.add(group)
		_groupsMap[group.nameKey] = group
		return this
	}

	fun add(property: Preference): PreferenceGroup {
		_preferences.add(property)
		return this
	}

	/**
	 * Returns the child [PreferenceGroup] of this [PreferenceGroup] with the specified name
	 * @throws IllegalArgumentException if not found
	 */
	fun getGroup(nameKey: String): PreferenceGroup {
		return _groupsMap[nameKey] ?: throw IllegalArgumentException("unknown PreferenceGroup '$nameKey'")
	}
}

/**
 * Represents an individual user preference stored in [Properties] that can be edited by the user in the UI.
 * A [Preference] is therefore a user-customizable property.
 */
interface Preference {

	/** The unique ID of this [Preference] used when accessing [Properties].*/
	val id: String

	/** The displayable, translated name of this [Preference].*/
	val name: String

	/** Determines whether the program has to be restarted if a change of this [Preference] should have an effect.*/
	val needsRestart: Boolean

	/** Adds the editor for editing the value of this [Preference] to the specified [PreferencesPanel].*/
	fun addToPanel(panel: PreferencesPanel)

	/** Loads the current value of the [Preference] in [Properties] to the editor of this [Preference].*/
	fun load()
}

open class Preferences(origProperties: Properties) : PropertiesProxy(origProperties) {

	open fun customize(preference: Preference, value: Any) {
		customize(preference.id, value)
	}
}

abstract class AbstractPreference(
	override val id: String,
	private val nameKey: String,
	override val needsRestart: Boolean = false
) : Preference {

	override val name: String get() = Translations.getString(nameKey)

	protected var panel: PreferencesPanel? = null
}

/** A [Preference] used for [Boolean] values.*/
class BooleanPreference(
	id: String,
	nameKey: String,
	needsRestart: Boolean = false
) : AbstractPreference(id, nameKey, needsRestart) {

	private val editor = JCheckBox()

	private val value: Boolean get() = panel!!.preferences.getBoolean(id)

	init {
		editor.addItemListener {
			if (editor.isSelected != value) {
				panel?.preferences?.customize(this, editor.isSelected)
			}
		}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		editor.text = name
		panel.addRow(editor)
	}

	override fun load() {
		editor.isSelected = value
	}
}

/** A [Preference] used for [Int] values.*/
class IntPreference(
	id: String,
	nameKey: String,
	needsRestart: Boolean = false,
	minValue: Int = Int.MIN_VALUE,
	maxValue: Int = Int.MAX_VALUE
) : AbstractPreference(id, nameKey, needsRestart) {

	private val editor: JFormattedTextField

	private val value: Int get() = panel!!.preferences.getInt(id)

	init {
		val numberFormatter = NumberFormatter(DecimalFormat.getIntegerInstance())
		numberFormatter.minimum = minValue
		numberFormatter.maximum = maxValue
		editor = JFormattedTextField(numberFormatter)
		editor.columns = 5
		editor.addPropertyChangeListener("value") {
			if (it.oldValue != null && it.oldValue != editor.value) {
				panel?.preferences?.customize(this, editor.value)
			}
		}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.value = value
	}
}

/** A [Preference] used for [Float] values.*/
class FloatPreference(
	id: String,
	nameKey: String,
	needsRestart: Boolean = false,
	minValue: Float = Float.MIN_VALUE,
	maxValue: Float = Float.MAX_VALUE
) : AbstractPreference(id, nameKey, needsRestart) {

	private val editor: JFormattedTextField

	init {
		val numberFormatter = NumberFormatter(DecimalFormat.getInstance())
		numberFormatter.minimum = minValue
		numberFormatter.maximum = maxValue
		editor = JFormattedTextField(numberFormatter)
		editor.columns = 5
		editor.addPropertyChangeListener("value") {
			if (it.oldValue != null && it.oldValue != editor.value) {
				panel?.preferences?.customize(this, editor.value)
			}
		}
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.value = panel!!.preferences.getFloat(id)
	}
}

/** A [Preference] used for [String] values.*/
class StringPreference(
	id: String,
	nameKey: String,
	needsRestart: Boolean = false
) : AbstractPreference(id, nameKey, needsRestart) {

	private val editor = JTextField(10)

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.text = panel!!.preferences.getString(id)
	}
}