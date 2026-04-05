package io.antarescircuit.antares.view

import io.antarescircuit.antares.view.app.AntaresGraphViewService
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.AbstractPropertyCommand
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.drawingBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.edit.properties.PropertyCommandSwing
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import java.awt.Frame
import javax.swing.JOptionPane

/**
 * If the user changes the [DigitalGraphView.defaultLightColor] property, he is asked whether he also
 * wants to apply thew new [LightColor] to all [VerticeView VerticeViews] in the [DigitalGraphView]
 * having a [LightColor] property.
 */
class DefaultLightColorProperty : CommandPropertySwing<LightColor>(
    "defaultLightColor",
    "element.property.DigitalGraphView.lightColor",
    LightColor::class.java,
    drawingBeanProvider
) {
    override fun createCommand(newValue: LightColor?): AbstractPropertyCommand<LightColor> {
        val applyToExistingComponents = JOptionPane.showConfirmDialog(
            Frame.getFrames()[0],
            Translations.getString("antares.action.replaceLightColor.question"),
            Translations.getString("antares.action.replaceLightColor.name"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION

        return ChangeDefaultLightColorCommandSwing(
            editor!!,
            beanProvider,
            beanIds,
            newValue,
            applyToExistingComponents)
    }
}

class ChangeDefaultLightColorCommandSwing(
    editor: Editor,
    beanProvider: BeanProvider,
    beanIds: Collection<String>,
    newValue: LightColor?,
    private val applyToExistingComponents: Boolean,
    private val service: AntaresGraphViewService = GraphViewModule.graphViewAppService as AntaresGraphViewService
) : PropertyCommandSwing<LightColor>(
    editor,
    "element.property.DigitalGraphView.lightColor",
    beanProvider,
    beanIds,
    newValue,
    "defaultLightColor",
    "defaultLightColor"
) {

    override fun setValue(bean: Bean, value: LightColor?) {
        super.setValue(bean, value)
        if (applyToExistingComponents) {
            service.replaceLightColor(bean as DigitalGraphView)
        }
    }
}