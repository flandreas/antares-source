package io.antarescircuit.antares.view.input

import com.l2fprod.common.propertysheet.Property
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.container.ControlViewComponent
import io.antarescircuit.jabbah.graph.container.ControlViewComponentBeanInfo
import io.antarescircuit.jabbah.graph.view.ControlViewBeanInfo
import io.antarescircuit.jabbah.graph.view.GraphProperties

@Suppress("unused")
class SwitchViewBeanInfo : AbstractAntaresInteractableVerticeBeanInfo<SwitchView>(), ControlViewBeanInfo {

    companion object {
	    private val name = EditProperties.untranslatableName()
	    private val toggle = CommandPropertySwing("toggle", SwitchView.BASE_KEY_TOGGLE, Boolean::class.java, componentBeanProvider)
		private val minOnTime = CommandPropertySwing("minOnTime", SwitchView.MIN_ON_TIME, Long::class.java, componentBeanProvider)
		private val closedOnStart = CommandPropertySwing("closedOnStart", SwitchView.CLOSED_ON_START, Boolean::class.java, componentBeanProvider)
	    private val labelPosition = GraphProperties.verticalLabelPosition()
		private val controlViewLabelPosition = GraphProperties.verticalLabelPosition(name = "${ControlViewComponentBeanInfo.aggregatePropertyName}.labelPosition")
    }

    override fun addProperties(bean: SwitchView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    properties.add(name.bind(editor, beanIdProvider(bean.id)))
	    properties.add(toggle.bind(editor, beanIdProvider(bean.id)))
	    properties.add(labelPosition.bind(editor, beanIdProvider(bean.id)))

		if (!bean.toggle) {
			properties.add(minOnTime.bind(editor, beanIdProvider(bean.id)))
		}
		properties.add(closedOnStart.bind(editor, beanIdProvider(bean.id)))
    }

	override fun addControlViewProperties(bean: ControlViewComponent, editor: Editor, properties: MutableList<Property>) {
		properties.add(controlViewLabelPosition.bind(editor, beanIdProvider(bean.id)))
	}
}
