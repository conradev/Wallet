import { writeFileSync } from "fs"
import { join, dirname } from "path"
import { fileURLToPath } from "url"
import Ajv from "ajv"
import standaloneCode from "ajv/dist/standalone/index.js"

export function generate(schemas, base) {
    const ajv = new Ajv({
        schemas: schemas,
        code: {
            source: true,
            esm: true,
        },
    })
    const outputPath = join(dirname(fileURLToPath(base)), "validators.js")
    writeFileSync(outputPath, standaloneCode(ajv))
}
