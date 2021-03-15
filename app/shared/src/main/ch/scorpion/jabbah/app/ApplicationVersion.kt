package ch.scorpion.jabbah.app

/**
 * An application version identification according to the "Semantic versioning" scheme.
 */
data class ApplicationVersion(
	val major: Int,
	val minor: Int,
	val patch: Int,
	val additionalLabel: String? = null
) : Comparable<ApplicationVersion> {

	constructor(version: ApplicationVersion): this(version.major, version.minor, version.patch, version.additionalLabel)
	constructor(version: String): this(parse(version))

	companion object {

		private const val NORMAL_DELIMITER = '.'
		private const val ADDITIONAL_DELIMITER = '-'

		fun parse(version: String): ApplicationVersion {
			val normalVersion = version.substringBefore(ADDITIONAL_DELIMITER)
			val additionalLabel = version.substringAfter(ADDITIONAL_DELIMITER, "")

			val normalParts = normalVersion.split(NORMAL_DELIMITER)

			if (normalParts.size < 3) {
				formatError(normalVersion, "expecting at least 3 parts")
			}

			return ApplicationVersion(
				parsePart(normalVersion, normalParts[0], "major"),
				parsePart(normalVersion, normalParts[1], "minor"),
				parsePart(normalVersion, normalParts[2], "patch"),
				if (additionalLabel.isEmpty()) null else additionalLabel
			)
		}

		private fun parsePart(version: String, partValue: String, partName: String): Int {
			try {
				return partValue.toInt()
			} catch (e: NumberFormatException) {
				formatError(version, "illegal $partName '$partValue'")
			}
		}

		private fun formatError(version: String, reason: String): Nothing {
			throw IllegalArgumentException("Illegal format of version string '$version': $reason")
		}
	}

	override fun toString(): String {
		val normalVersion = "$major$NORMAL_DELIMITER$minor$NORMAL_DELIMITER$patch"
		return if (additionalLabel != null) {
			"$normalVersion$ADDITIONAL_DELIMITER$additionalLabel"
		} else {
			normalVersion
		}
	}

	override fun compareTo(other: ApplicationVersion): Int {
		this.major.compareTo(other.major).let {
			if (it != 0) {
				return it
			}
		}

		this.minor.compareTo(other.minor).let {
			if (it != 0) {
				return it
			}
		}

		this.patch.compareTo(other.patch).let {
			if (it != 0) {
				return it
			}
		}

		if (this.additionalLabel == null && other.additionalLabel != null) {
			return 1
		}

		if (this.additionalLabel != null && other.additionalLabel == null) {
			return -1
		}

		if (this.additionalLabel != null && other.additionalLabel != null) {
			// The spec would require to split additionalLabel by '.' and compare separately,
			// but we don't care for the moment
			return this.additionalLabel.compareTo(other.additionalLabel)
		}

		return 0
	}
}
