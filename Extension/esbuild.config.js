
import { pnpPlugin } from "@yarnpkg/esbuild-plugin-pnp";
import { build } from "esbuild";

await build({
    entryPoints: ["page.ts", "content.ts", "background.ts"],
    bundle: true,
    minify: true,
    sourcemap: 'inline',
    target: 'es2020',
    platform: 'browser',
    outdir: "build",
    plugins: [pnpPlugin()],
});
