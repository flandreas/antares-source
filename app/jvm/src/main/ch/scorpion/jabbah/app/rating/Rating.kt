package ch.scorpion.jabbah.app.rating

data class Rating(
	val overallRating: Int,
	val likeMost: RatingAspect,
	val likeLeast: RatingAspect,
	val remark: String?
)