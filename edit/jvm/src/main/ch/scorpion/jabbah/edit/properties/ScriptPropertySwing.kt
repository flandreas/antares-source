package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.dsl.DslParser
import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.text.ScriptProperty

/**
 * A [CommandPropertySwing] for script properties.
 * @param parserFactory creates the [DslParser] used in the "check" function, or `null` if "check" is not supported
 */
class ScriptPropertySwing(
	propertyName: String,
	baseKey: String,
	beanProvider: BeanProvider = componentBeanProvider,
	val parserFactory: ParserFactory? = BaseModule.parserFactory,
	val helpId: HelpId? = null
) : CommandPropertySwing<ScriptProperty>(
	propertyName,
	baseKey,
	ScriptProperty::class.java,
	beanProvider,
	interactive = true) {

	override fun isEditable(): Boolean = true
}