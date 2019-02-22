package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText

/**
 * A [Usecase] is a representation of a single way the user can use a [GraphView].
 * The author of a [GraphView] programs the individual interactions that should occur with the [GraphView]
 * while a [Usecase] is executed. The user can then start the execution of a [Usecase] and observe
 * how the [GraphView] behaves. During [Usecase] execution, the user cannot interact with the [GraphView].
 * <p>
 * In addition, a [Usecase] can contain assertion code that checks certain conditions after execution of
 * a [Usecase], which can be used to automatically testing a [GraphView].
 */
interface Usecase : Storable {

	/** Returns the identification of this [Usecase] that is unique within a [GraphView].*/
	var id: Int

	/**
	 * Contains the displayable name of this [Usecase] in the current system [Language].
	 * Note taht this name is internationalized and should not be used for technical identification.
	 */
	var name: String

	/** Contains translations of the [name] property.*/
	var translatableName: TranslatableText

	/**
	 * The text to be displayed above the explained [GraphView] when this [Scenario] is active.
	 * Reflects the value in [translatableDescription] corresponding with the current system [Language].
	 */
	var description: String?

	/** Contains translations for the [description] property.*/
	var translatableDescription: TranslatableText

	/** The JavaScript script to be executed when this [Usecase] is executed.*/
	var executionScriptProperty: TextProperty

	fun dispose()
}