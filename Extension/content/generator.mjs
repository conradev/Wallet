import { generate } from "../shared/generator.mjs"

const contentPageMessage = {
    $id: "validateContentPageMessage",
    type: "object",
    properties: {
        id: { "type": "number" },
        type: { "type": "string" },
        frame: {
            type: "object",
            properties: {
                "x": { type: "number" },
                "y": { type: "number" },
                "width": { type: "number" },
                "height": { type: "number" },
            },
            required: ["x", "y", "width", "height"],
            additionalProperties: false,
        },
        payload: {},
    },
    required: ["id", "type", "frame", "payload"],
    additionalProperties: false,
}


generate([contentPageMessage], import.meta.url)
