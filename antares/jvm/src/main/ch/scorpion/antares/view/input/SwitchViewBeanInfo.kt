package ch.scorpion.antares.view.input

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.container.ControlViewComponent
import ch.scorpion.jabbah.graph.container.ControlViewComponentBeanInfo
import ch.scorpion.jabbah.graph.view.ControlViewBeanInfo
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SwitchViewBeanInfo : DigitalComponentBeanInfo<SwitchView>(), ControlViewBeanInfo {

    companion object {
	    private val name = EditProperties.untranslatableName()
	    private val toggle = CommandPropertySwing("toggle", SwitchView.BASE_KEY_TOGGLE, Boolean::class.java, componentBeanProvider)
	    private val labelPosition = GraphProperties.verticalLabelPosition()
		private val controlViewLabelPosition = GraphProperties.verticalLabelPosition(name = "${ControlViewComponentBeanInfo.aggregatePropertyName}.labelPosition")
    }

    override fun addProperties(bean: SwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, beanIdProvider(bean.id)))
	    properties.add(toggle.bind(editor, beanIdProvider(bean.id)))
	    properties.add(labelPosition.bind(editor, beanIdProvider(bean.id)))
    }

	override fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		properties.add(controlViewLabelPosition.bind(editor, beanIdProvider(bean.id)))
	}
}
