package ch.scorpion.jabbah.graph.view.scenario

import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty


/**
 * A [BeanInfo] for [ScenarioStepImpl].
 */
class ScenarioStepImplBeanInfo : AbstractBeanInfo<ScenarioStepImpl>() {

    private val name = PropertyImpl("graph.property.scenario.name", String::class.java)
    private val description = PropertyImpl("edit.property.description", TextProperty::class.java)
    private val condition = PropertyImpl("graph.property.scenario.condition", TextProperty::class.java)
    private val highlightIds = PropertyImpl("graph.property.scenario.highlightIds", String::class.java)
    private val onEntry = PropertyImpl("graph.property.scenario.onEntry", TextProperty::class.java)
    private val onExit = PropertyImpl("graph.property.scenario.onExit", TextProperty::class.java)

    override fun addProperties(bean: ScenarioStepImpl, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.name }, { bean.name = it!! })
        description.bind(editor, { bean.description}, { bean.description = it!! })
		condition.bind(editor, { bean.conditionProperty}, { bean.conditionProperty = it!! })
        highlightIds.bind(editor, { bean.highlightIds }, { bean.highlightIds = it })
        onEntry.bind(editor, { bean.onEntryProperty}, { bean.onEntryProperty = it!! })
        onExit.bind(editor, { bean.onExitProperty}, { bean.onExitProperty = it!! })

		properties.add(name)
        properties.add(description)
		properties.add(condition)
        properties.add(highlightIds)
		properties.add(onEntry)
		properties.add(onExit)
    }
}