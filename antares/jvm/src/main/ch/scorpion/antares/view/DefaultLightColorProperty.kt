package ch.scorpion.antares.view

import ch.scorpion.antares.view.app.AntaresGraphViewService
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.AbstractPropertyCommand
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.edit.properties.PropertyCommandSwing
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
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

        return ChangeLightColorsCommandSwing(
            editor!!,
            beanProvider,
            beanIds,
            newValue!!,
            applyToExistingComponents)
    }
}

class ChangeLightColorsCommandSwing(
    editor: Editor,
    beanProvider: BeanProvider,
    beanIds: Collection<String>,
    newValue: LightColor,
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