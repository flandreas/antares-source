package ch.scorpion.antares.shaulamock

import ch.scorpion.antares.AntaresApplication
import ch.scorpion.antares.akrabapi.ProjectTO
import ch.scorpion.antares.mainScope
import ch.scorpion.antares.module.AntaresAkrabProtectedModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.auth0.Auth0ContextInterface
import ch.scorpion.jabbah.base.auth0.Auth0Provider
import ch.scorpion.jabbah.base.auth0.loginLogout
import ch.scorpion.jabbah.base.auth0.useAuth0
import ch.scorpion.jabbah.base.util.encodeURI
import ch.scorpion.jabbah.edit.auth.AnonymousWebUserHolder
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.library.LibraryIdentification
import ch.scorpion.jabbah.graph.library.LibraryModule
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonSize
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.card.mCard
import com.ccfraser.muirwik.components.card.mCardActions
import com.ccfraser.muirwik.components.card.mCardContent
import com.ccfraser.muirwik.components.card.mCardHeader
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import react.Props
import react.dom.render
import react.fc
import react.useEffectOnce
import react.useState
import styled.css
import styled.styledDiv

class ShaulaMock {

	fun show() {
		initialize()
		display()
	}

	private fun initialize() {
		console.info("Initializing AntaresPage")

		EditAuthModule.require()
		EditAuthModule.userHolder = AnonymousWebUserHolder

		AntaresAkrabProtectedModuleJs.require()
		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(
			LibraryIdentification(LibraryModule.DEF_LIBRARY_UUID, null),
			isSystem = true)

		AntaresThemes.install()
		LogSystem.level = LogLevel.Info
	}

	private fun display() {
		render(document.getElementById("root")) {
			child(Auth0Provider) {
				attrs {
					domain = "dev-wq7i977v.eu.auth0.com"
					clientId = "mYdmErbSZxQUtlr9BW2UHUOmxtHN8WNO"
					audience = "https://antarescircuit.io/api"
					redirectUri = window.location.origin
					onRedirectCallback = {
						if (it.returnTo != null) {
							window.location.replace(it.returnTo!!)
						} else {
							window.location.replace("/")
						}
					}
				}
				child(shaulaMock)
			}
		}
	}
}

val shaulaMock = fc<Props> {

	var auth0 = useAuth0()
	var allProjects: Result<List<ProjectTO>>? by useState(null)

	//if (auth0.isAuthenticated) {
		useEffectOnce {
			mainScope.launch {
				allProjects = fetchProjects(auth0)
			}
		}
	//}

	mCssBaseline()
	styledDiv {
		mAppBar(position = MAppBarPosition.static) {
			mToolbar {
				mToolbarTitle("Antares")
				child(loginLogout)
			}
		}

		allProjects?.let {
			if (it.isError) {
				mTypography("Error: ${it.errorCode}")
			} else {
				child(projectList) {
					attrs {
						projects = it.data!!
					}
				}
			}
		}
	}
}

data class Result<T>(
	val data: T?,
	val errorCode: Short? = null
) {
	companion object {
		fun <T> error(code: Short) = Result<T>(null, code)
	}

	val isError: Boolean get() = errorCode != null
}

suspend fun fetchProjects(auth0: Auth0ContextInterface): Result<List<ProjectTO>> {
	var token =auth0.getAccessTokenSilently().await()
	val headers = Headers()
	headers.set("Authorization", "Bearer $token")
	val response = window
		.fetch("/api/projects", RequestInit(
			headers = headers
		))
		.await()

	if (!response.ok) {
		return Result.error(response.status)
	}

	val text = response.text().await()
	val data: List<ProjectTO> = Json.decodeFromString(text)

	return Result(data)
}

external interface ProjectListProps : Props {
	var projects: List<ProjectTO>
}

val projectList = fc<ProjectListProps> { props ->
	styledDiv {
		css {
			margin(5.px)
		}
		mGridContainer(MGridSpacing.spacing2) {
			for (p in props.projects) {
				mGridItem {
					child(projectCard) {
						attrs { project = p }
					}
				}
			}
		}
	}
}

external interface ProjectCardProps : Props {
	var project: ProjectTO
}

val projectCard = fc<ProjectCardProps> { props ->
	mCard {
		val href = """
			/desktop.html?
			project=${props.project.uuid}
			&returnUrl=${encodeURI(window.location.href)}
		""".trimIndent()
		mCardHeader(props.project.name.getText())
		mCardContent {
			mTypography(props.project.description?.getText())
		}
		mCardActions {
			mButton("Open", size = MButtonSize.small,
				onClick = { window.location.href = href })
		}
	}
}


