package ch.scorpion.antares.shaulamock

import ch.scorpion.antares.AntaresApplication
import ch.scorpion.antares.akrabapi.ProjectTO
import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.auth0.Auth0ContextInterface
import ch.scorpion.jabbah.base.auth0.useAuth0
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
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
import kotlinx.coroutines.MainScope
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
import react.router.dom.*
import react.useEffectOnce
import react.useState
import styled.css
import styled.styledDiv

val mainScope = MainScope()

class ShaulaMock {

	fun show() {
		initialize()
		display()
	}

	private fun initialize() {
		console.info("Initializing AntaresPage")

		AntaresModuleJs.require()

		EditAuthModule.userHolder.u = User.developer

		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(AntaresApplication.DEF_LIBRARY_UUID, isSystem = true)

		AntaresThemes.install()
		LogSystem.level = LogLevel.Info
	}

	private fun display() {
		render(document.getElementById("root")) {
			child(shaulaMock)
		}
	}
}

val shaulaMock = fc<Props> {

	var allProjects: Result<List<ProjectTO>>? by useState(null)
	var auth0ContextInterface: Auth0ContextInterface = useAuth0()

	useEffectOnce {
		mainScope.launch {
			allProjects = fetchProjects(auth0ContextInterface)
		}
	}

	mCssBaseline()
	styledDiv {
		mAppBar(position = MAppBarPosition.static) {
			mToolbar {
				mToolbarTitle("Antares")
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
		} ?: run {
			mTypography("Loading..")
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

suspend fun fetchProjects(auth0ContextInterface: Auth0ContextInterface): Result<List<ProjectTO>> {
	println("fetchProjects: getAccessToken")
	var token: String = ""
	try {
		token = auth0ContextInterface.getAccessTokenSilently().await()
	} catch (e: Throwable) {
		println("Running outside Auth0Provider")
	}

	println("fetchProjects: calling Akrab")
	val response = window
		.fetch("http://localhost:8080/api/projects", RequestInit(
			headers = Headers().set("Authorization", "Bearer $token")
		))
		.await()

	println("fetchProjects: Akrab returned ${response.status}")

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
		mCardHeader(props.project.name.getText())
		mCardContent {
			mTypography(props.project.description?.getText())
		}
		mCardActions {
			mButton("Open", size = MButtonSize.small, onClick = { window.location.href = "/desktopMock.html" })
		}
	}
}


