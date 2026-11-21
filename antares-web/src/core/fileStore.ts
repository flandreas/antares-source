// In-memory file store with write-through persistence of user files to IndexedDB.
// The Kotlin side reads and writes synchronously; IndexedDB writes happen in the background.

const DB_NAME = 'antares-web'
const STORE = 'files'

/** Paths below these prefixes are user data and get persisted; everything else is static. */
const PERSISTED_PREFIXES = ['projects/', 'user/']

function openDb(): Promise<IDBDatabase> {
	return new Promise((resolve, reject) => {
		const request = indexedDB.open(DB_NAME, 1)
		request.onupgradeneeded = () => request.result.createObjectStore(STORE)
		request.onsuccess = () => resolve(request.result)
		request.onerror = () => reject(request.error)
	})
}

export class FileStore {
	private files = new Map<string, string>()
	private db: IDBDatabase | null = null
	private pending = new Map<string, string | null>()
	private flushTimer: number | undefined
	private listeners = new Set<(saving: boolean) => void>()

	/** Loads all persisted user files into memory. */
	async open(): Promise<void> {
		try {
			this.db = await openDb()
			const tx = this.db.transaction(STORE, 'readonly')
			const store = tx.objectStore(STORE)
			const [keys, values] = await Promise.all([
				request<IDBValidKey[]>(store.getAllKeys()),
				request<string[]>(store.getAll()),
			])
			keys.forEach((key, i) => this.files.set(String(key), values[i]))
		} catch (e) {
			console.warn('IndexedDB unavailable, user projects will not persist', e)
		}
	}

	/** Fetches static files into memory (not persisted). */
	async preload(entries: Array<[path: string, url: string]>): Promise<void> {
		await Promise.all(
			entries.map(async ([path, url]) => {
				const response = await fetch(url)
				if (response.ok) this.files.set(path, await response.text())
			}),
		)
	}

	/** Adds static (not persisted) files. */
	addStatic(files: Record<string, string>) {
		for (const [path, content] of Object.entries(files)) this.files.set(path, content)
	}

	read = (path: string): string | null => this.files.get(path) ?? null

	write = (path: string, content: string): void => {
		this.files.set(path, content)
		this.schedule(path, content)
	}

	remove = (path: string): void => {
		this.files.delete(path)
		this.schedule(path, null)
	}

	list(prefix: string): string[] {
		return [...this.files.keys()].filter((key) => key.startsWith(prefix))
	}

	onSavingChange(listener: (saving: boolean) => void): () => void {
		this.listeners.add(listener)
		return () => this.listeners.delete(listener)
	}

	private schedule(path: string, content: string | null) {
		if (!PERSISTED_PREFIXES.some((p) => path.startsWith(p)) || !this.db) return
		this.pending.set(path, content)
		this.listeners.forEach((l) => l(true))
		window.clearTimeout(this.flushTimer)
		this.flushTimer = window.setTimeout(() => void this.flush(), 250)
	}

	private async flush() {
		if (!this.db || this.pending.size === 0) return
		const batch = [...this.pending]
		this.pending.clear()
		const tx = this.db.transaction(STORE, 'readwrite')
		const store = tx.objectStore(STORE)
		for (const [path, content] of batch) {
			if (content === null) store.delete(path)
			else store.put(content, path)
		}
		await new Promise<void>((resolve) => {
			tx.oncomplete = () => resolve()
			tx.onerror = () => {
				console.error('Saving to IndexedDB failed', tx.error)
				resolve()
			}
		})
		if (this.pending.size === 0) this.listeners.forEach((l) => l(false))
	}
}

function request<T>(r: IDBRequest): Promise<T> {
	return new Promise((resolve, reject) => {
		r.onsuccess = () => resolve(r.result as T)
		r.onerror = () => reject(r.error)
	})
}
