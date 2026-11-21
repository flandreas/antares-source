// Typed wrapper around the Kotlin `AntaresWebEditor`, plus a tiny observable store for React.
import * as lib from 'jabbah-antares'
import { FileStore, type SaveStatus } from './fileStore'
import { TabLock } from './tabLock'
import type {
	ActionName,
	EditorState,
	Message,
	PaletteNode,
	ProjectNode,
	ProjectEntry,
	PropertySheet,
} from './types'

const web = lib.io.antarescircuit.antares.web
type KotlinEditor = InstanceType<typeof web.AntaresWebEditor>

const DATA_URL = `${import.meta.env.BASE_URL}data`

interface DataIndex {
	system: Array<{ uuid: string; circuits: string[] }>
	samples: Array<{ uuid: string; circuits: string[] }>
}

/** How wires are drawn: drag from pin to pin, or click pins and waypoints. */
export type ConnectMethod = 'autoLayout' | 'setPoints'

export interface LibraryEntry {
	uuid: string
	name: string
	description: string
	/** Imported directly by the open project */
	imported: boolean
	/** Available through another imported library */
	indirect: boolean
}

export interface SampleInfo {
	uuid: string
	name: string
	description: string
}

/** Prefix of HTML drag data types carrying palette keys (see Kotlin `WebLibrary.bindDropTarget`). */
export const DRAG_TYPE_PREFIX = 'application/x-antares-'

/** Drag data types are lower-cased by browsers, so keys are hex-encoded. */
export function dragType(key: string): string {
	return DRAG_TYPE_PREFIX + [...key].map((c) => c.charCodeAt(0).toString(16).padStart(2, '0')).join('')
}

type Listener = () => void

export class Engine {
	readonly files = new FileStore()
	readonly tabLock = new TabLock()
	private kotlin!: KotlinEditor
	private index!: DataIndex
	private listeners = new Set<Listener>()
	private messageListeners = new Set<(m: Message) => void>()
	private messageId = 0
	private cache: {
		state?: EditorState
		items?: ProjectNode[] | null
		properties?: PropertySheet | null
		projects?: ProjectEntry[]
	} = {}

	/** Answers yes/no questions from the editor; the UI replaces this with its own dialog. */
	confirmHandler: (question: string) => Promise<boolean> = (q) => Promise.resolve(window.confirm(q))

	/** Increments with every change notification, so React can subscribe via useSyncExternalStore. */
	version = 0

	private starting: Promise<void> | null = null

	/** Starts the editor once; repeated calls return the same promise. */
	start(dark: boolean): Promise<void> {
		return (this.starting ??= this.startImpl(dark))
	}

	private async startImpl(dark: boolean): Promise<void> {
		const json = async <T,>(name: string) => (await (await fetch(`${DATA_URL}/${name}`)).json()) as T
		const [index, system] = await Promise.all([
			json<DataIndex>('index.json'),
			json<Record<string, string>>('system.json'),
			this.files.open(),
		])
		this.index = index
		this.files.addStatic(system)

		const store = new web.WebFileStore(this.files.read, this.files.write, this.files.remove)
		this.kotlin = new web.AntaresWebEditor(
			store,
			DATA_URL,
			() => this.changed(),
			(type, title, text) => this.message(type as Message['type'], title, text),
			(question, answer) => void this.confirmHandler(question).then(answer),
		)
		await this.kotlin.start(dark)
		let lastStatus: SaveStatus = this.files.status
		this.files.onStatusChange(() => {
			const status = this.files.status
			if (status === 'failed' && lastStatus !== 'failed') {
				this.message(
					'error',
					'Changes are not saved',
					`The browser refused to store them (${this.files.errorMessage ?? 'unknown error'}). Antares keeps trying; export the project to keep a copy.`,
				)
			}
			lastStatus = status
			this.changed()
		})
		// Another tab takes over: save, then leave the stored files to it
		this.tabLock.onTakeover = async () => {
			this.save()
			await this.files.flush()
			this.files.suspend()
		}
	}

	private changed() {
		this.cache = {}
		this.version++
		this.listeners.forEach((l) => l())
	}

	subscribe = (listener: Listener): (() => void) => {
		this.listeners.add(listener)
		return () => this.listeners.delete(listener)
	}

	onMessage(listener: (m: Message) => void): () => void {
		this.messageListeners.add(listener)
		return () => this.messageListeners.delete(listener)
	}

	message(type: Message['type'], title: string, text: string) {
		const m: Message = { id: ++this.messageId, type, title, text }
		this.messageListeners.forEach((l) => l(m))
	}

	/** ---- Reads (cached until the next change) */

	get state(): EditorState {
		return (this.cache.state ??= this.readState())
	}

	private readState(): EditorState {
		const state = JSON.parse(this.kotlin.state()) as Omit<EditorState, 'saveStatus'> & { saving: boolean }
		const files = this.files.status
		const saveStatus: SaveStatus = files === 'failed' ? 'failed' : state.saving ? 'saving' : files
		return { ...state, saveStatus }
	}

	/** The palette rarely changes; keep the parsed one (and its identity for React) while its JSON is the same. */
	private paletteJson = ''
	private paletteNodes: PaletteNode[] = []

	get palette(): PaletteNode[] {
		const json = this.kotlin.palette()
		if (json !== this.paletteJson) {
			this.paletteJson = json
			this.paletteNodes = JSON.parse(json) as PaletteNode[]
		}
		return this.paletteNodes
	}

	/** All items of the open project (circuits, truth tables, …), or null without project. */
	get items(): ProjectNode[] | null {
		if (this.cache.items === undefined) this.cache.items = JSON.parse(this.kotlin.items()) as ProjectNode[] | null
		return this.cache.items
	}

	get properties(): PropertySheet | null {
		if (this.cache.properties === undefined) this.cache.properties = JSON.parse(this.kotlin.properties()) as PropertySheet | null
		return this.cache.properties
	}

	get projects(): ProjectEntry[] {
		return (this.cache.projects ??= JSON.parse(this.kotlin.projects()) as ProjectEntry[])
	}

	/** ---- Canvas */

	bindCanvas(canvas: HTMLCanvasElement) {
		this.kotlin.bindCanvas(canvas)
	}

	bindSymbolCanvas(canvas: HTMLCanvasElement) {
		this.kotlin.bindSymbolCanvas(canvas)
	}

	showSymbol(symbol: boolean) {
		this.kotlin.showSymbol(symbol)
		this.changed()
	}

	/** ---- Editing */

	perform(action: ActionName): boolean {
		return this.kotlin.perform(action)
	}

	pasteText(text: string): boolean {
		return this.kotlin.pasteText(text)
	}

	zoomIn() {
		this.kotlin.zoomIn()
		this.changed()
	}

	zoomOut() {
		this.kotlin.zoomOut()
		this.changed()
	}

	zoomAt(factor: number, x: number, y: number) {
		this.kotlin.zoomAt(factor, x, y)
	}

	panBy(dx: number, dy: number) {
		this.kotlin.panBy(dx, dy)
	}

	zoomToFit() {
		this.kotlin.zoomToFit()
		this.changed()
	}

	zoomToNormal() {
		this.kotlin.zoomToNormal()
		this.changed()
	}

	navigateTo(index: number) {
		this.kotlin.navigateTo(index)
		this.changed()
	}

	placeAtCenter(key: string) {
		this.kotlin.placeAtCenter(key)
	}

	/** Returns a PNG data URL preview of a palette item, or '' if it cannot be rendered. */
	thumbnail(key: string, width: number, height: number): string {
		if (!this.palette) return ''
		return this.kotlin.thumbnail(key, width, height, window.devicePixelRatio || 1)
	}

	paletteItemInfo(key: string): { name: string; description: string } | null {
		return JSON.parse(this.kotlin.paletteItemInfo(key)) as { name: string; description: string } | null
	}

	setProperty(key: string, value: unknown) {
		this.kotlin.setProperty(key, JSON.stringify(value))
	}

	/** Saves the open document; the files are written to IndexedDB shortly after, see [flush]. */
	save() {
		this.kotlin.saveNow()
	}

	/** Saves the open document and writes all changes to IndexedDB now. */
	flush(): Promise<void> {
		this.save()
		return this.files.flush()
	}

	/** ---- Simulation */

	setSimulating(simulating: boolean) {
		this.kotlin.setSimulating(simulating)
		this.changed()
	}

	togglePause() {
		this.kotlin.togglePause()
	}

	step() {
		this.kotlin.step()
	}

	setSpeed(speed: number) {
		this.kotlin.setSpeed(speed)
	}

	/** ---- Projects & circuits */

	createProject(name: string): string {
		const uuid = this.kotlin.createProject(name)
		this.changed()
		return uuid
	}

	openProject(uuid: string) {
		this.kotlin.openProject(uuid)
		localStorage.setItem('antares.lastProject', uuid)
		this.changed()
	}

	renameProject(uuid: string, name: string) {
		this.kotlin.renameProject(uuid, name)
		this.changed()
	}

	deleteProject(uuid: string) {
		this.kotlin.deleteProject(uuid)
		if (localStorage.getItem('antares.lastProject') === uuid) localStorage.removeItem('antares.lastProject')
		this.changed()
	}

	exportProject(): string {
		return this.kotlin.exportProject()
	}

	importProject(json: string): string {
		const uuid = this.kotlin.importProject(json)
		this.changed()
		return uuid
	}

	get lastProject(): string | null {
		const uuid = localStorage.getItem('antares.lastProject')
		return uuid && this.projects.some((p) => p.uuid === uuid) ? uuid : null
	}

	/** System libraries and whether the open project uses them. */
	libraries(): LibraryEntry[] {
		return JSON.parse(this.kotlin.libraries()) as LibraryEntry[]
	}

	addLibrary(uuid: string) {
		this.kotlin.addLibrary(uuid)
		this.changed()
	}

	removeLibrary(uuid: string) {
		this.kotlin.removeLibrary(uuid)
		this.changed()
	}

	createCircuit(name: string, type: 'digital' | 'analog') {
		this.kotlin.createCircuit(name, type)
		this.changed()
	}

	openItem(uuid: string) {
		this.kotlin.openItem(uuid)
		this.changed()
	}

	renameItem(uuid: string, name: string) {
		this.kotlin.renameItem(uuid, name)
		this.changed()
	}

	duplicateCircuit(uuid: string) {
		this.kotlin.duplicateCircuit(uuid)
		this.changed()
	}

	deleteItem(uuid: string) {
		this.kotlin.deleteItem(uuid)
		this.changed()
	}

	closeDialog() {
		this.kotlin.closeDialog()
	}

	/**
	 * The Kotlin editor API, for feature modules (`api.memory`, `api.truthTables`, `api.fsm`, `api.testcases`, `api.hdl`).
	 * Structured results are JSON strings. Call [changed] after mutations that the UI should reflect immediately.
	 */
	get api(): KotlinEditor {
		return this.kotlin
	}

	/** Invalidates cached reads and re-renders the UI. */
	refresh() {
		this.changed()
	}

	setDefaultCircuit(uuid: string) {
		this.kotlin.setDefaultCircuit(uuid)
		this.changed()
	}

	/** ---- Samples */

	private sampleInfos: SampleInfo[] | null = null

	/** Reads names and descriptions of the bundled sample projects. */
	async samples(): Promise<SampleInfo[]> {
		if (this.sampleInfos) return this.sampleInfos
		const infos = await Promise.all(
			this.index.samples.map(async ({ uuid, circuits }) => {
				const xml = await (await fetch(`${DATA_URL}/samples/${uuid}/library.xml`)).text()
				const root = new DOMParser().parseFromString(xml, 'text/xml').documentElement
				const translated = (selector: string) => {
					const el =
						root.querySelector(`:scope > ${selector} > translation[lang='en']`) ??
						root.querySelector(`:scope > ${selector} > translation`)
					return el?.getAttribute('text')?.trim() ?? ''
				}
				const count = `${circuits.length} ${circuits.length === 1 ? 'circuit' : 'circuits'}`
				const description = translated('desc')
				return {
					uuid,
					name: translated('folder > libraryFolder > name') || 'Example',
					description: description ? `${description} · ${count}` : count,
				}
			}),
		)
		this.sampleInfos = infos.sort((a, b) => a.name.localeCompare(b.name))
		return this.sampleInfos
	}

	/** Fetches a sample project and imports it as an editable project. Returns the project UUID. */
	async importSample(uuid: string): Promise<string> {
		const sample = this.index.samples.find((s) => s.uuid === uuid)
		if (!sample) throw new Error('Unknown sample')
		await this.files.preload([
			[`samples/${uuid}/library.xml`, `${DATA_URL}/samples/${uuid}/library.xml`],
			...sample.circuits.map((c): [string, string] => [`samples/${uuid}/${c}.cir`, `${DATA_URL}/samples/${uuid}/${c}.cir`]),
		])
		const projectUuid = this.kotlin.importSample(uuid)
		this.changed()
		return projectUuid
	}

	/** ---- Preferences */

	setConnectMethod(method: ConnectMethod) {
		this.kotlin.setConnectMethod(method)
	}

	/** ---- Appearance */

	setDark(dark: boolean) {
		this.kotlin.setDark(dark)
	}
}

export const engine = new Engine()

// Exposed for debugging and browser tests in development builds
if (import.meta.env.DEV) (window as unknown as { engine: Engine }).engine = engine
