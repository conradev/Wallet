import { generate } from "../shared/generator.mjs"

const pageConnectMessage = {
    $id: "validatePageConnectMessage",
    type: "object",
    properties: {
        type: { const: "page_connect" },
    },
    required: ["type"],
    additionalProperties: false,
}
const messageType = {
    $id: "validateMessageType",
    type: "object",
    properties: {
        type: { type: "string" },
    },
    required: ["type"],
    additionalProperties: true,
}
const pageMessage = {
    $id: "validatePageMessage",
    type: "object",
    properties: {
        id: { type: "number" },
        type: { type: "string" },
        payload: {},
    },
    required: ["id", "type", "payload"],
    additionalProperties: false,
}
const rpcResponseMessage = {
    $id: "validateRPCResponseMessage",
    type: "object",
    properties: {
        id: { type: "number" },
        type: { const: "rpc_response" },
        payload: {
            type: "object",
            properties: {
                request_id: { type: "number" },
                result: {},
                error: {},
            },
            required: ["request_id"],
            additionalProperties: false,
        },
    },
    required: ["id", "type", "payload"],
    additionalProperties: false,
}
const eventMessage = {
    $id: "validateEventMessage",
    type: "object",
    properties: {
        id: { type: "number" },
        type: { const: "event" },
        payload: {
            type: "object",
            properties: {
                event: { type: "string" },
                value: {},
            },
            required: ["event", "value"],
            additionalProperties: false,
        },
    },
    required: ["id", "type", "payload"],
    additionalProperties: false,
}
const openURLMessage = {
    $id: "validateOpenURLMessage",
    type: "object",
    properties: {
        id: { type: "number" },
        type: { const: "open_url" },
        payload: {
            type: "object",
            properties: {
                url: { type: "string" },
            },
            required: ["url"],
            additionalProperties: false,
        },
    },
    required: ["id", "type", "payload"],
    additionalProperties: false,
}

const connectEvent = {
    $id: "validateConnectEvent",
    type: "object",
    properties: {
        chainId: { type: "string" },
    },
    required: ["chainId"],
    additionalProperties: false,
}

generate([
    pageConnectMessage,
    messageType,
    pageMessage,
    rpcResponseMessage,
    eventMessage,
    openURLMessage,
    connectEvent,
], import.meta.url)
