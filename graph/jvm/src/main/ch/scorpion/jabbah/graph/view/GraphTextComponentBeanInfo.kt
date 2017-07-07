package ch.scorpion.jabbah.graph.view

import com.l2fprod.common.propertysheet.Property
import ch.scorpion.jabbah.edit.AbstractBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.PropertyImpl

/**
 * A [BeanInfo] for [GraphTextComponent].
 */
class GraphTextComponentBeanInfo : AbstractBeanInfo<GraphTextComponent>(){

    private val id = PropertyImpl("edit.property.id", Int::class.java)
    private val scenario = PropertyImpl("graph.property.GraphTextComponent.scenario", Scenario::class.java)
    private val scenarioStep = PropertyImpl("graph.property.GraphTextComponent.scenarioStep", ScenarioStep::class.java)

    override fun addProperties(bean: GraphTextComponent, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        id.bind(editor, { bean.id }, null, false)
        scenario.bind(editor, { bean.scenario }) { bean.scenario = it }
        scenarioStep.bind(editor, { bean.scenarioStep }) { bean.scenarioStep = it }

        properties.add(id)
        properties.add(scenario)
        properties.add(scenarioStep)
    }
}