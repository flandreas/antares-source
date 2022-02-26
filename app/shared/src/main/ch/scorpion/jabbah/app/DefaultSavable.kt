package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations

/**
 * A [Savable] implementation that used an [ApplicationDataViewController]'s [ApplicationDataRepository] for saving.
 */
open class DefaultSavable(val identification: String?) : Savable {

	companion object {

		fun undefined(): Savable {
			return DefaultSavable(null)
		}

		fun withIdentification(identification: String): Savable {
			return DefaultSavable(identification)
		}
	}

	/** ---- [DefaultSavable] interface */

	override val description: String
		get() {
			val sb = StringBuilder(Translations.getString("application.fileSavable.prefix"))
			if (identification == null) {
				sb.append(" <")
				sb.append(Translations.getString("application.title.unknown"))
				sb.append(">")
			} else {
				sb.append(" ")
				sb.append(identification)
			}
			return sb.toString()
		}

	override val defined: Boolean get() = StringUtils.isNotEmpty(identification)

	override val supportsMostRecent: Boolean get() = true

	override val editable: Boolean get() = true

	override fun open(application: Application): Boolean {
		// TODO
		throw UnsupportedOperationException("not implemented")
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		return if (defined) {
			appDataViewController.saveWithSavable()
			true
		} else {
			appDataViewController.saveAs()
		}
	}
}