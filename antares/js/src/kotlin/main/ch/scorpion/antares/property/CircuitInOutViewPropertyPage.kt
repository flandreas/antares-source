package ch.scorpion.antares.property

import ch.scorpion.antares.property.CircuitInOutViewPropertyPage.ComponentStyles.formControl
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyPageRenderer
import ch.scorpion.jabbah.edit.properties.jmTextField
import com.ccfraser.muirwik.components.form.mFormControl
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.spacingUnits
import kotlinx.css.*
import react.RBuilder
import styled.StyleSheet
import styled.css

@Suppress("unused")
class CircuitInOutViewPropertyPage : PropertyPageRenderer<CircuitInOutView> {

	private object ComponentStyles : StyleSheet("ComponentStyles", isStatic = true) {
		val formControl by css {
			margin(1.spacingUnits)
			minWidth = 120.px
			maxWidth = 300.px
		}
	}

	override fun render(bean: CircuitInOutView, editor: Editor, builder: RBuilder) {
		builder.run {
			mFormControl {
				css(formControl)
				mTextField(Translations.getString("edit.property.id"), value = bean.id.toString(), disabled = true)
			}
			mFormControl {
				css(formControl)
				jmTextField {
					this.editor = editor
					beanProvider = componentBeanProvider
					beanIds = listOf(bean.id)
					getter = { bean.name }
					setter = { _, value -> bean.name = value }
				}
			}
			mFormControl {
				css(formControl)
				jmBitWidthField {
					this.editor = editor
					beanProvider = componentBeanProvider
					beanIds = listOf(bean.id)
					getter = { bean.bitWidth }
					setter = { _, value -> bean.bitWidth = value!! }
				}
			}
		}
	}
}