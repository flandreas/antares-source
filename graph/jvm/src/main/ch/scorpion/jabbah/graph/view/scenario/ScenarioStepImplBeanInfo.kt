package ch.scorpion.jabbah.graph.view.scenario

import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import com.l2fprod.common.propertysheet.Property
import java.beans.BeanInfo

/**
 * A [BeanInfo] for [ScenarioStepImpl].
 */
@Suppress("unused")
class ScenarioStepImplBeanInfo : AbstractBeanInfo<ScenarioStepImpl>() {

    private val name = PropertyImpl("graph.property.scenario.name", TranslatableText::class.java)
    private val description = PropertyImpl("edit.property.description", TranslatableText::class.java)
    private val condition = PropertyImpl("graph.property.scenario.condition", TextProperty::class.java)
    private val highlightIds = PropertyImpl("graph.property.scenario.highlightIds", String::class.java)
    private val onEntry = PropertyImpl("graph.property.scenario.onEntry", TextProperty::class.java)
    private val onExit = PropertyImpl("graph.property.scenario.onExit", TextProperty::class.java)

    override fun addProperties(bean: ScenarioStepImpl, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        name.bind(editor, { bean.translatableName }, { bean.translatableName = it!! })
        description.bind(editor, { bean.translatableDescription}, { bean.translatableDescription = it!! })
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