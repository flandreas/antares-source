package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.graph.model.param.GraphParamValue
import io.antarescircuit.jabbah.graph.model.param.GraphParamValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class LightColorGraphParamTypeTest {

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldParsePredefined() {
        assertSame(LightColor.RED, LightColorGraphParamType.parse(LightColor.RED.ordinal.toString(), supportExpression = true))
    }

    @Test
    fun shouldParseExpression() {
        val lightColor = LightColorGraphParamType.parse("=LC", supportExpression = true)
        assertIs<LightColorExpression>(lightColor)
        assertEquals("LC", lightColor.expression)
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