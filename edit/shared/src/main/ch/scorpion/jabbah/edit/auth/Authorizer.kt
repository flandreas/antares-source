package ch.scorpion.jabbah.edit.auth

typealias UserCondition = (User) -> Boolean

typealias DataCondition = (Any) -> Boolean

/**
 * Decides whether the current [User] can execute an [Operation] on particular data.
 */
object Authorizer {

	private val authorizations = mutableListOf<Authorization>()

	fun reset() {
		authorizations.clear()
	}

	fun authorize(): AuthorizationBuilder {
		return AuthorizationBuilder()
	}

	fun isCurrentUserAuthorizedTo(operation: Operation, data: Any): Boolean {
		return authorizations.firstOrNull {
			it.userCondition.invoke(EditAuthModule.userHolder.user)
				&& it.operation == operation
				&& it.dataCondition.invoke(data)
		} != null
	}

	private fun addAuthorization(authorization: Authorization) {
		authorizations.add(authorization)
	}

	class AuthorizationBuilder {

		private lateinit var userCondition: UserCondition
		private lateinit var operation: Operation
		private lateinit var dataCondition: DataCondition

		fun currentUser(): AuthorizationBuilder {
			userCondition = ::currentUserCondition
			return this
		}

		fun to(operation: Operation): AuthorizationBuilder {
			this.operation = operation
			return this
		}

		fun data(data: DataCondition) {
			this.dataCondition = data
			addAuthorization(Authorization(userCondition, operation, dataCondition))
		}
	}

	internal data class Authorization(
		val userCondition: UserCondition,
		val operation: Operation,
		val dataCondition: DataCondition
	)
}

private fun currentUserCondition(user: User) =
	user.identity == EditAuthModule.userHolder.user.identity



