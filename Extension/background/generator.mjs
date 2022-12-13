import { generate } from "../shared/generator.mjs"

const nativeHostMessage = {
    $id: "validateNativeHostMessage",
    type: "object",
    properties: {
        id: { type: "number" },
        type: { type: "string" },
        payload: {},
        session: {
            type: "object",
            properties: {
                browser_pid: { type: "number" },
                tab_id: { type: "number" },
                frame_id: { type: "number" },
            },
            required: ["browser_pid", "tab_id", "frame_id"],
            additionalProperties: false,
        },
    },
    required: ["id", "type", "payload", "session"],
    additionalProperties: false,
}

generate([nativeHostMessage], import.meta.url)
