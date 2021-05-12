package ch.scorpion.antares.property

import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyPageRenderer
import ch.scorpion.jabbah.edit.properties.jmTextField
import ch.scorpion.jabbah.edit.properties.propertyRow
import com.ccfraser.muirwik.components.MGridAlignItems
import com.ccfraser.muirwik.components.MGridSpacing
import com.ccfraser.muirwik.components.mGridContainer
import com.ccfraser.muirwik.components.spacingUnits
import kotlinx.css.margin
import kotlinx.css.maxWidth
import kotlinx.css.minWidth
import kotlinx.css.px
import react.RBuilder
import styled.StyleSheet

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
			mGridContainer(MGridSpacing.spacing1, alignItems = MGridAlignItems.center) {

				propertyRow("edit.property.id.name") {
					it.jmTextField {
						this.editor = editor
						beanProvider = componentBeanProvider
						beanIds = listOf(bean.id)
						getter = { bean.id.toString() }
						disabled = true
					}
				}

				propertyRow("edit.property.name.name") {
					it.jmTextField {
						this.editor = editor
						beanProvider = componentBeanProvider
						beanIds = listOf(bean.id)
						getter = { bean.name }
						setter = { _, value -> bean.name = value }
					}
				}

				propertyRow("element.property.bitWidth.name") {
					it.jmBitWidthField {
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
}