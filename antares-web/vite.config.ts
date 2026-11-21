import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { readFileSync } from 'node:fs'

// The Kotlin/JS build is a set of UMD modules linked as `jabbah-antares`.
// They must be pre-bundled (CommonJS -> ESM) in dev and transformed in production builds.
// Set ANTARES_KOTLIN_DEV=1 to use the unminified development build of the Kotlin code for debugging.
const kotlinDev = process.env.ANTARES_KOTLIN_DEV === '1'

const version = (JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf8')) as { version: string }).version

export default defineConfig({
	plugins: [react()],
	define: { __APP_VERSION__: JSON.stringify(version) },
	resolve: kotlinDev
		? {
				alias: {
					'jabbah-antares': fileURLToPath(
						new URL('../build/antares/dist/js/developmentLibrary/jabbah-antares.js', import.meta.url),
					),
				},
			}
		: undefined,
	optimizeDeps: { include: ['jabbah-antares'] },
	build: {
		commonjsOptions: { include: [/jabbah-antares/, /dist[\\/]js/, /node_modules/] },
		chunkSizeWarningLimit: 8000,
	},
	server: { port: 5317 },
})
