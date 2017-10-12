package ch.scorpion.jabbah.graph.view.scenario

import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.graph.view.scenario.ScenarioImpl


/**
 * A [BeanInfo] for [ScenarioImpl].
 */
class ScenarioImplBeanInfo : AbstractBeanInfo<ScenarioImpl>() {

    private val name = PropertyImpl("graph.property.scenario.name", String::class.java)
    private val description = PropertyImpl("edit.property.description", TextProperty::class.java)
    private val condition = PropertyImpl("graph.property.scenario.condition", TextProperty::class.java)

    override fun addProperties(bean: ScenarioImpl, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.name}, { bean.name = it!! })
        description.bind(editor, { bean.description}, { bean.description = it!! })
		condition.bind(editor, { bean.conditionProperty }, { bean.conditionProperty = it!! })

		properties.add(name)
        properties.add(description)
		properties.add(condition)
    }
}