import fs from "fs"
import path from "path"
import { fileURLToPath } from "url"
import Ajv from "ajv"
import standaloneCode from "ajv/dist/standalone/index.js"

const nativeMessage = {
  "$id": "validateNativeMessage",
  "type": "object",
  "properties": {
    "id": { "type": "number" },
    "type": { "type": "string" },
    "frame": {
      "type": "object",
      "properties": {
          "x": { "type": "number" },
          "y": { "type": "number" },
          "width": { "type": "number" },
          "height": { "type": "number" }
      },
      "required": ["x", "y", "width", "height"],
      "additionalProperties": false
    },
    "frame_id": { "type": "string" },
    "url": { "type": "string" },
    "payload": { "type": "object", "minProperties": 1 }
  },
  "required": ["id", "type", "frame_id", "payload"],
  "additionalProperties": true
}
const openUrlPayload = {
  "$id": "validateOpenURLPayload",
  "type": "object",
  "properties": {
    "url": { "type": "string" }
  },
  "required": ["url"],
  "additionalProperties": false
}
const pageConnectMessage = {
  "$id": "validatePageConnect",
  "type": "object",
  "properties": {
    "type": { "type": "string", "const": "page_connect_message" }
  },
  "required": ["type"],
  "additionalProperties": false
}
const responsePayload = {
  "$id": "validateResponsePayload",
  "type": "object",
  "properties": {
    "request_id": { "type": "number" },
    "result": {}
  },
  "required": ["request_id", "result"],
  "additionalProperties": false
}
const tabMessage = {
  "$id": "validateTabMessage",
  "type": "object",
  "properties": {
    "id": { "type": "number" },
    "type": { "type": "string" },
    "frame": {
      "type": "object",
      "properties": {
        "x": { "type": "number" },
        "y": { "type": "number" },
        "width": { "type": "number" },
        "height": { "type": "number" }
      },
      "required": ["x", "y", "width", "height"],
      "additionalProperties": false
    },
    "payload": { "type": "object", "minProperties": 1 }
  },
  "required": ["id", "type", "payload"],
  "additionalProperties": false
}

const schemas = [nativeMessage, openUrlPayload, pageConnectMessage, responsePayload, tabMessage]
const ajv = new Ajv({schemas: schemas, code: {source: true, esm: true}})
const outputPath = path.join(path.dirname(fileURLToPath(import.meta.url)), "validators.js")
fs.writeFileSync(outputPath, standaloneCode(ajv))
