package ch.scorpion.jabbah.base.auth0

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.*
import react.Props
import react.fc

val loginButton = fc<MButtonProps> {
	val auth0 = useAuth0()
	mButton("Login") {
		attrs {
			color = MColor.inherit
			variant = MButtonVariant.outlined
			size = MButtonSize.small
			//disabled = auth0.isAuthenticated
			onClick = { auth0.loginWithRedirect() }
		}
	}
}

val logoutButton = fc<MButtonProps> {
	val auth0 = useAuth0()
	mButton("Logout") {
		attrs {
			color = MColor.inherit
			variant = MButtonVariant.outlined
			size = MButtonSize.small
			//disabled = !auth0.isAuthenticated
			onClick = { auth0.logout(object : LogoutOptions {
				//override val returnTo = window.location.origin
				override val returnTo: String = "/"
			}) }
		}
	}
}

val loginLogout = fc<Props> {
	child(loginButton)
	child(logoutButton)
	/*
	val auth0 = useAuth0()
	if (auth0.isAuthenticated) {
		child(logoutButton)
	} else {
		child(loginButton)
	}
	*/
}