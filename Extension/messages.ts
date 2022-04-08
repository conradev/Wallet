import validateTabMessage from "./schemas/tab-message"
import validateNativeMessage from "./schemas/native-message"
import validateResponsePayload from "./schemas/response-payload"
import validateOpenURLPayload from "./schemas/openurl-payload"
import validatePageConnect from "./schemas/page-connect-message"

export class Frame {
    x: number
    y: number
    width: number
    height: number

    public constructor(window: Window) {
        this.x = window.screenLeft
        this.y = window.screenTop
        this.width = window.outerWidth
        this.height = window.outerHeight
    }
}

export class TabMessage {
    static validate = (object) => {
        return validateTabMessage(JSON.parse(JSON.stringify(object)))
    }

    id: number
    type: string
    frame?: Frame
    payload: object

    public constructor(id: number, type: string, payload: object, frame?: Frame) {
        this.id = id
        this.type = type
        this.frame = frame
        this.payload = payload
    }
}

export class NativeMessage implements TabMessage {
    static validate = (object) => {
        return validateNativeMessage(JSON.parse(JSON.stringify(object)))
    }

    id: number
    type: string
    frame?: Frame
    frame_id: string
    url?: string
    payload: object

    public constructor(id: number, type: string, frame: Frame, frame_id: string, url: string, payload: object) {
        this.id = id
        this.type = type
        this.frame = frame
        this.frame_id = frame_id
        this.url = url
        this.payload = payload
    }
}

export class RPCResponsePayload {
    static validate = (object) => {
        return validateResponsePayload(JSON.parse(JSON.stringify(object)))
    }

    request_id: string
    result: any
}

export class OpenURLPayload {
    static validate = (object) => {
        return validateOpenURLPayload(JSON.parse(JSON.stringify(object)))
    }

    url: string
}

export class PageConnectMessage {
    static validate = (object) => {
        return validatePageConnect(JSON.parse(JSON.stringify(object)))
    }

    type = "page_connect_message"
}
