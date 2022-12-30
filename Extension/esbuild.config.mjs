import { build } from "esbuild"
import { promises as fs } from "fs"
import { dirname, join, relative } from "path"
import { fileURLToPath } from "url"
import esbuildSvelte from "esbuild-svelte"
import sveltePreprocess from "svelte-preprocess"

const path = dirname(fileURLToPath(import.meta.url))

const webExtensionPlugin = {
    name: "web-extension",
    setup(build) {
        const outDir = join(path, build.initialOptions.outdir)

        build.onEnd(async result => {
            for (const build of ["chrome", "safari", "firefox"]) {
                const extras = ["content/shim.js", "transaction/transaction.html"]
                const manifest = JSON.parse(await fs.readFile(join(path, "manifest.json")))
                const buildDir = join(outDir, build)

                manifest["web_accessible_resources"][0]["resources"] = result.outputFiles
                    .map(f => relative(outDir, f.path))

                for (const file of result.outputFiles) {
                    const path = join(buildDir, relative(outDir, file.path))
                    await fs.mkdir(dirname(path), { recursive: true })
                    await fs.writeFile(path, file.contents)
                }


                if (build === "chrome") {
                    delete manifest["browser_specific_settings"]
                    manifest["background"] = {
                        service_worker: "background/background.js",
                        type: "module",
                    }
                } else {
                    delete manifest["key"]
                }

                if (build === "safari") {
                    delete manifest["browser_specific_settings"]["gecko"]
                    manifest["background"] = {
                        page: "background/background.html",
                        persistent: false,
                    }
                }
                if (build === "firefox") {
                    delete manifest["browser_specific_settings"]["safari"]
                    manifest["background"] = {
                        page: "background/background.html",
                    }
                    extras.push("background/background.html")
                }

                await fs.cp(join(path, "resources"), buildDir, { recursive: true })
                for (const file of extras) {
                    await fs.copyFile(join(path, file), join(buildDir, file))
                }

                await fs.writeFile(join(buildDir, "manifest.json"), JSON.stringify(manifest))
            }
        })
    },
}

await build({
    entryPoints: [
        "page/page.ts",
        "content/content.ts",
        "background/background.ts",
        "transaction/transaction.ts",
    ],
    bundle: true,
    minify: true,
    plugins: [
        esbuildSvelte({
            preprocess: sveltePreprocess(),
        }),
        webExtensionPlugin,
    ],
    splitting: true,
    format: "esm",
    target: "es2020",
    platform: "browser",
    outdir: "build",
    write: false,
})
