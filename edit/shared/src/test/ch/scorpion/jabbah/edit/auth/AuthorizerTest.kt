package ch.scorpion.jabbah.edit.auth

import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.auth.Operation.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthorizerTest {

	companion object {
		init {
			// The current user is the developer
			EditTestRule.configure()
		}
	}

	init {
		Authorizer.reset()
	}

	@Test
	fun test() {
		Authorizer.authorize().currentUser().to(Change).data(::ownedByHim)

		assertTrue(Authorizer.isCurrentUserAuthorizedTo(Change, TestData(owner = DesktopUser.developer)))
		assertFalse(Authorizer.isCurrentUserAuthorizedTo(Change, TestData(owner = DesktopUser.anybody)))
	}

	class TestData(val owner: User)

	private fun ownedByHim(data: Any): Boolean =
		if (data is TestData ) data.owner == EditAuthModule.userHolder.user else false
}

