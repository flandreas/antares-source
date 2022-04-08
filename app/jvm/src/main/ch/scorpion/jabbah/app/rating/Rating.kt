package ch.scorpion.jabbah.app.rating

data class Rating(
	val overallRating: Int,
	val likeMost: String,
	val likeLeast: String,
	val remark: String?
)