import { defineConfig, type Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { readFileSync } from 'node:fs'

// The Kotlin/JS build is a set of UMD modules linked as `jabbah-antares`.
// They must be pre-bundled (CommonJS -> ESM) in dev and transformed in production builds.
// Set ANTARES_KOTLIN_DEV=1 to use the unminified development build of the Kotlin code for debugging.
const kotlinDev = process.env.ANTARES_KOTLIN_DEV === '1'

const version = (JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf8')) as { version: string }).version

const KOTLIN_JS = /dist[\\/]js[\\/].*\.js$/

// Kotlin/JS creates a property reference on every read of a delegated property (`by lazy`, `by logger`) and then
// discards it. Creating one copies the whole KProperty prototype, which costs a large share of simulation and
// drawing time, so these dead statements are removed. Property references that are actually used are kept.
const DEAD_PROPERTY_REF = /^[ \t]*getPropertyCallableRef\((['"])[\w$]*\1, \d, tmp(?:_\d+)?, [\w$]+\([\w$]*\), null\);$/gm

function stripDeadPropertyRefs(code: string): string {
	return code.includes('getPropertyCallableRef') ? code.replace(DEAD_PROPERTY_REF, '') : code
}

function kotlinOptimizations(): Plugin {
	return {
		name: 'antares-kotlin-optimizations',
		enforce: 'pre',
		transform(code, id) {
			return KOTLIN_JS.test(id) ? { code: stripDeadPropertyRefs(code), map: null } : null
		},
	}
}

export default defineConfig({
	plugins: [react(), kotlinOptimizations()],
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
	optimizeDeps: {
		include: ['jabbah-antares'],
		esbuildOptions: {
			plugins: [
				{
					name: 'antares-kotlin-optimizations',
					setup(build) {
						build.onLoad({ filter: KOTLIN_JS }, async ({ path }) => ({
							contents: stripDeadPropertyRefs(readFileSync(path, 'utf8')),
							loader: 'js',
						}))
					},
				},
			],
		},
	},
	build: {
		commonjsOptions: { include: [/jabbah-antares/, /dist[\\/]js/, /node_modules/] },
		chunkSizeWarningLimit: 8000,
	},
	server: { port: 5317 },
})
