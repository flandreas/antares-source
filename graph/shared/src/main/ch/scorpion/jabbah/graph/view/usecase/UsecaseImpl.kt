package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.io.*

class UsecaseImpl(
	name: String = ""
) : Usecase {

	/** ---- [Usecase] interface */

	override var id: Int = 0

	override var name: String
		get() = translatableName.getTranslation()
		set(value) {
			if (name != value) {
				translatableName = translatableName.withTranslation(value)
			}
		}

	override var translatableName: TranslatableText = TranslatableText(name)
		set(value) {
			if (field != value) {
				field = value
			}
		}

	override var description: String?
		get() = translatableDescription.getOptionalTranslation()
		set(value) {
			if (description != null && value != null) {
				translatableDescription = translatableDescription.withTranslation(value)
			}
		}

	override var translatableDescription: TranslatableText = TranslatableText()
		set(value) {
			if (field != value) {
				field = value
			}
		}

	override var executionScriptProperty: TextProperty
		get() = TODO("not implemented")
		set(value) {}

	override fun dispose() {
		throw UnsupportedOperationException("not implemented")
	}

	/** ---- [Storable] interface */

	override var storableId: Int
		get() = TODO("not implemented")
		set(value) {}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun write(writer: StoreWriter) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun read(reader: StoreReader) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getStorableChildren(): Iterator<Storable> {
		throw UnsupportedOperationException("not implemented")
	}
}