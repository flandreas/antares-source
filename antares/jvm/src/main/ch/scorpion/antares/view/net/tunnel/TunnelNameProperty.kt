package ch.scorpion.antares.view.net.tunnel

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.net.TunnelName
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.PropertyCommandSwing
import com.l2fprod.common.beans.editor.AbstractPropertyEditor
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import org.apache.commons.beanutils.PropertyUtils
import java.awt.Frame
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JOptionPane
import javax.swing.JTextField

/**
 * If the old [TunnelName] exists in other [TunnelViews][TunnelView] as well, the user is asked
 * whether he also wants to change the name in these other [TunnelViews][TunnelView].
 */
class TunnelNameProperty(
	val graph: DigitalGraph,
	private val propertyName: String,
	baseKey: String,
	beanProvider: BeanProvider
) : CommandPropertySwing<TunnelName>(
	propertyName,
	baseKey,
	TunnelName::class.java,
	beanProvider,
	propertyName,
	propertyName,
	supportMultiSelection = false
) {

	override fun createCommand(newValue: TunnelName?): AbstractPropertyCommand<TunnelName> {
		val oldValue = getOldValue()?.name
		val hasOther = editor!!.drawing
			.getDrawables {
				it is TunnelView &&
				StringUtils.isNotEmpty(it.name) &&
				StringUtils.isNotEmpty(oldValue) &&
				it.name == oldValue
			}
			.count() > 1

		return if (hasOther && JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("antares.tunnelName.question.text"),
			Translations.getString("antares.tunnelName.question.title"),
			JOptionPane.YES_NO_OPTION
		) == JOptionPane.YES_OPTION) {
			ChangeTunnelNamesCommandSwing(editor!!, beanProvider, beanIds, newValue, propertyName)
		} else {
			super.createCommand(newValue)
		}
	}

	private fun getOldValue(): TunnelName? {
		return if (isNested(getterPropertyName)) {
			PropertyUtils.getNestedProperty(beans.iterator().next(), getterPropertyName) as TunnelName?
		} else {
			PropertyUtils.getSimpleProperty(beans.iterator().next(), getterPropertyName) as TunnelName?
		}
	}
}

/**
 * Changes [TunnelName] in all [TunnelViews][TunnelView] having the same old value.
 */
class ChangeTunnelNamesCommandSwing(
	editor: Editor,
	beanProvider: BeanProvider,
	beanIds: Collection<String>,
	newValue: TunnelName?,
	propertyName: String
) : PropertyCommandSwing<TunnelName>(
	editor,
	"antares.tunnelName.command",
	beanProvider,
	beanIds,
	newValue,
	propertyName,
	propertyName
) {
	override val canUndo: Boolean get() = false

	override fun setValue(bean: Bean, value: TunnelName?) {
		editor!!.drawing
			.getDrawables { it is TunnelView && it.name != null && it.name == oldValues?.get((bean as TunnelView).id)?.name }
			.map { it as TunnelView }
			.forEach { it.tunnelName = value }
	}
}

/**
 * Contains a [ComboBoxPropertyEditor] offering all existing [TunnelNames][TunnelName]
 * in the specified [DigitalGraph].
 */
class TunnelNameEditor(
	graph: DigitalGraph
) : AbstractPropertyEditor() {

	private val comboBoxEditor = ComboBoxPropertyEditor()
	private val comboBox: JComboBox<TunnelName> get() = comboBoxEditor.customEditor as JComboBox<TunnelName>

	init {
		comboBox.model = DefaultComboBoxModel(
			graph.tunnelNames
				.sortedBy { RichText.stripToPlainText(it.name) }
				.toTypedArray()
		)
		comboBox.isEditable = true
		editor = comboBox
		(comboBox.editor.editorComponent as JTextField).addActionListener { comboBox.editor.editorComponent.transferFocus() }
	}

	override fun getValue(): Any? {
		val v = comboBox.editor.item
		return if (v is String) {
			TunnelName(v)
		} else {
			v
		}
	}

	override fun setValue(value: Any?) {
		comboBoxEditor.value = value
	}
}