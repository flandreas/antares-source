package ch.scorpion.antares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.antares.view.theme.AntaresThemes
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystem
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.GraphViewerJs
import ch.scorpion.jabbah.graph.ui.graphViewer
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mContainer
import com.ccfraser.muirwik.components.mCssBaseline
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.styles.Breakpoint
import kotlinx.browser.document
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import react.dom.render
import styled.*

/** A React application displaying possible multiple [GraphViewerJs].*/
class AntaresPage {

	fun show() {
		initialize()
		display()
	}

	private fun initialize() {
		console.info("Initializing AntaresPage")

		AntaresModuleJs.require()

		EditAuthModule.userHolder.u = User.developer

		LibraryModule.libraryHolder.l = LibraryModule.libraryService.loadLibrary(AntaresApplication.DEF_LIBRARY_UUID, isSystem = true)
		loadProject()

		AntaresThemes.install()

		LogSystem.level = LogLevel.Info
	}

	private fun loadProject() {
		val projectUuid = UUID("532f0477-722c-4c88-ada3-c419a386d06a")
		ProjectModule.projectHolder.p = ProjectModule.projectManagementService.load(projectUuid)
	}

	private fun display() {
		render(document.getElementById("root")) {
			mCssBaseline()

			mContainer(maxWidth = Breakpoint.md) {
				css {
					backgroundColor = Color.white
				}
				mTypography("Binary Addition", variant = MTypographyVariant.h3)

				mTypography("""
					|To start thinking about addition in binary terms, let's look at the binary addition
					|of two 1-bit values - the simplest form of addition we can imagine.
					""".trimMargin(),
					paragraph = true)

				styledTable {
					styledThead {
						styledTr {
							styledTh { mTypography("Addition") }
							styledTh { mTypography("Result") }
							styledTh { mTypography("Carry") }
						}
					}
					styledTbody {
						styledTr {
							styledTd { mTypography("0 + 0") }
							styledTd { mTypography("0") }
							styledTd { mTypography("0") }
						}
						styledTr {
							styledTd { mTypography("0 + 1") }
							styledTd { mTypography("1") }
							styledTd { mTypography("0") }
						}
						styledTr {
							styledTd { mTypography("1 + 0") }
							styledTd { mTypography("1") }
							styledTd { mTypography("0") }
						}
						styledTr {
							styledTd { mTypography("1 + 1") }
							styledTd { mTypography("0") }
							styledTd { mTypography("1") }
						}
					}
				}

				mTypography("""
					|Let's first look at calculating the 'Result' value. You notice immediately that the result value
					|is 1 only if the two added values are different. This is exactly what is the calculation result
					|of an XOR gate.
					""".trimMargin(),
					paragraph = true)

				mTypography("""
					|Play around with the XOR gate in the circuit below and try different input values.
					""".trimMargin(),
					paragraph = true)

				graphViewer {
					canvasId = "canvas0"
					metaGraphUuid = UUID("440b10dc-0999-4426-aa0f-c22c5221f641")
					size = Dimension2D(500, 400)
				}

				mTypography("""
					|Now let's look at calculating the 'Carry' value. The truth table tells you that the carry value
					|is 1 only if both added values are 1. This is exactly what is the calculation result
					|of an AND gate.
					""".trimMargin(),
					paragraph = true)

				graphViewer {
					canvasId = "canvas1"
					metaGraphUuid = UUID("ae8e1d4c-5201-489d-9494-8eec9f54380d")
					size = Dimension2D(500, 400)
				}

				mTypography("""
					|Now let's combine the calculation of the result bit and the carry into a single circuit.
					|We call this combined circuit "Half Adder". It produces a "Sum" (or result) bit and a "Carry" bit.
					""".trimMargin(),
					paragraph = true)

				graphViewer {
					canvasId = "canvas2"
					metaGraphUuid = UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82")
					size = Dimension2D(500, 400)
				}

				mTypography("""
					|Up to now, we can only add 2 bits. If you want to add numbers consisting of multiple bits,
					|you add the two corresponding bits of each number, but you also have to respect the carry
					|bit from the previous addition. This is done by a circuit called "Full Adder", which consists
					|of two "Half Adders". The second "Half Adder" adds the result of the addition to the carry
					|of the previous addition.
					""".trimMargin(),
					paragraph = true)

				graphViewer {
					canvasId = "canvas3"
					metaGraphUuid = UUID("08aba425-96c2-4c43-b10b-2e0c72ce8300")
					size = Dimension2D(500, 400)
				}

				mTypography("""
					Now that we've represented binary 1-bit addition using a full adder, we can chain multiple
					full adders together to implement n-bit addition. Note how the "carry out" of each less
					significant bit gets forwarded to the "carry in" of the next more significant bit.
				""".trimIndent(),
					paragraph = true)

				mTypography("""
					The circuit below implements 4-bit addition using 4 full adders. Start the simulation,
					click on the inputs "A", "B" and "CI, enter values using the keyboard, and observe
					the calculated results at the outputs "S" and "CO".
				""".trimIndent(),
					paragraph = true)

				graphViewer {
					canvasId = "canvas4"
					metaGraphUuid = UUID("e2252451-b870-4541-ac11-1ffe2aca72ca")
					size = Dimension2D(800, 400)
				}
			}
		}
	}
}