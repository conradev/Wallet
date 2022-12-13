import { ErrorObject } from "ajv"

type Validator = {
    (input): boolean;
    errors: ErrorObject[];
};

export function parser<T extends object>(
    validator: Validator,
    clone: (T) => T,
    silent = false,
): ((input) => T | undefined) {
    return (input) => {
        const string = JSON.stringify(input)
        if (!string) {
            return undefined
        }
        const facsimile = JSON.parse(string)
        if (!validator(facsimile)) {
            if (!silent) {
                for (const error of validator.errors) {
                    console.error(error.message)
                }
            }
            return undefined
        }
        return clone(facsimile as T)
    }
}
