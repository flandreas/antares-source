// In-memory file store with write-through persistence of user files to IndexedDB.
// The Kotlin side reads and writes synchronously; IndexedDB writes happen in the background.

const DB_NAME = 'antares-web'
const STORE = 'files'

/** Paths below these prefixes are user data and get persisted; everything else is static. */
const PERSISTED_PREFIXES = ['projects/', 'user/']

/** Delay to batch writes, e.g. a project's library and circuit files saved together. */
const WRITE_DELAY = 250
const MAX_RETRY_DELAY = 30_000

/**
 * - `saved`: all changes are committed to IndexedDB
 * - `saving`: changes are waiting to be written or being written
 * - `failed`: writing failed (it is retried) or the browser can't store data at all
 */
export type SaveStatus = 'saved' | 'saving' | 'failed'

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
	/** Changes not yet committed, including the batch being written. */
	private pending = new Map<string, string | null>()
	/** The write in progress, resolving to whether it succeeded. */
	private writing: Promise<boolean> | null = null
	private flushTimer: number | undefined
	private retryDelay = 1000
	private error: unknown = null
	/** Set when another tab took over: changes then stay in memory only. */
	private suspended = false
	private persistenceRequested = false
	private listeners = new Set<() => void>()

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
			this.error = e
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

	get status(): SaveStatus {
		if (this.error && (!this.db || this.pending.size > 0)) return 'failed'
		return this.pending.size > 0 ? 'saving' : 'saved'
	}

	/** Whether some changes are not committed yet. */
	get hasPendingChanges(): boolean {
		return this.pending.size > 0
	}

	/** Why saving failed, if it did. */
	get errorMessage(): string | null {
		if (!this.error) return null
		return this.error instanceof Error || this.error instanceof DOMException ? this.error.message : String(this.error)
	}

	/** Notifies [listener] whenever [status] may have changed. */
	onStatusChange(listener: () => void): () => void {
		this.listeners.add(listener)
		return () => this.listeners.delete(listener)
	}

	/** Stops persisting changes, because another tab edits the same data now. */
	suspend() {
		this.suspended = true
		window.clearTimeout(this.flushTimer)
	}

	private notify() {
		this.listeners.forEach((l) => l())
	}

	private schedule(path: string, content: string | null) {
		if (!PERSISTED_PREFIXES.some((p) => path.startsWith(p)) || this.suspended) return
		this.pending.set(path, content)
		this.requestPersistence()
		this.notify()
		if (!this.db) return
		// While failing, the retry timer stays in charge
		if (!this.error) {
			window.clearTimeout(this.flushTimer)
			this.flushTimer = window.setTimeout(() => void this.flush(), WRITE_DELAY)
		}
	}

	/** Writes all pending changes now, e.g. before the page is closed. Resolves once they are committed or failed. */
	async flush(): Promise<void> {
		window.clearTimeout(this.flushTimer)
		while (this.db && !this.suspended && this.pending.size > 0) {
			this.writing ??= this.writePending(this.db)
			const written = await this.writing
			this.writing = null
			if (!written) return
		}
	}

	/** Writes the pending changes in one transaction; on failure, schedules a retry. */
	private async writePending(db: IDBDatabase): Promise<boolean> {
		const batch = new Map(this.pending)
		try {
			await writeBatch(db, batch)
			// Keep changes made while writing
			for (const [path, content] of batch) if (this.pending.get(path) === content) this.pending.delete(path)
			this.error = null
			this.retryDelay = 1000
			return true
		} catch (e) {
			console.error('Saving to IndexedDB failed, retrying', e)
			this.error = e
			this.flushTimer = window.setTimeout(() => void this.flush(), this.retryDelay)
			this.retryDelay = Math.min(this.retryDelay * 2, MAX_RETRY_DELAY)
			return false
		} finally {
			this.notify()
		}
	}

	/** Asks the browser not to evict projects when storage runs low (best effort, once). */
	private requestPersistence() {
		if (this.persistenceRequested) return
		this.persistenceRequested = true
		void navigator.storage?.persisted?.().then((persisted) => {
			if (!persisted) return navigator.storage.persist()
		}).catch(() => {})
	}
}

function writeBatch(db: IDBDatabase, batch: Map<string, string | null>): Promise<void> {
	return new Promise((resolve, reject) => {
		// Strict durability: "saved" means written to disk, not just to the OS cache
		const tx = db.transaction(STORE, 'readwrite', { durability: 'strict' })
		const store = tx.objectStore(STORE)
		for (const [path, content] of batch) {
			if (content === null) store.delete(path)
			else store.put(content, path)
		}
		tx.oncomplete = () => resolve()
		tx.onabort = () => reject(tx.error ?? new Error('Saving was aborted'))
	})
}

function request<T>(r: IDBRequest): Promise<T> {
	return new Promise((resolve, reject) => {
		r.onsuccess = () => resolve(r.result as T)
		r.onerror = () => reject(r.error)
	})
}
