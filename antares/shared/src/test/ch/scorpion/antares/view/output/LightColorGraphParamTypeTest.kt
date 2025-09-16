package ch.scorpion.antares.view.output

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class LightColorGraphParamTypeTest {

    companion object {
        init {
            AntaresTestRule.configure()
        }
    }

    @Test
    fun shouldParsePredefined() {
        assertSame(LightColor.RED, LightColorGraphParamType.parse(LightColor.RED.ordinal.toString(), supportExpression = true))
    }

    @Test
    fun shouldParseExpression() {
        val lightColor = LightColorGraphParamType.parse("=LC", supportExpression = true)
        assertIs<LightColorExpression>(lightColor)
        assertEquals("LC", (lightColor as LightColorExpression).expression)
    }

    @Test
    fun shouldEvaluateExpressionInGraph() {
        val graph = GraphModelModule.graphFactory.create(TranslatableText("test"), AntaresGraphTypes.Digital)
        graph.parameterDefinitions = graph.parameterDefinitions.withDefinition(
            GraphParamDefinition.create(
                name = "LC",
                type = LightColorGraphParamType,
                defaultValue = LightColor.RED)
        )
        val paramValue = GraphParamValue.create("LC", LightColorGraphParamType, LightColor.YELLOW, null)
        graph.parameterValues = GraphParamValues().withValue(paramValue)

        val expression = LightColorExpression("=LC")
        val result = LightColorGraphParamType.evaluateIn(graph, expression)

        assertIs<LightColorExpression>(result)
        assertSame(LightColor.YELLOW, result.value)
    }
}