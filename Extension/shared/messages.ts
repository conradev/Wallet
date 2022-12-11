import {
    validateTabMessage,
    validateNativeMessage,
    validateResponsePayload,
    validateOpenURLPayload,
    validatePageConnect,
} from "./validators"

function parser<T>(validator: (input) => boolean): ((input) => T | undefined) {
    return (input) => {
        const string = JSON.stringify(input)
        if (!string) {
            return undefined
        }
        const clone = JSON.parse(string)
        if (!validator(clone)) {
            return undefined
        }
        return clone as T
    }
}

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
    static parse = parser<TabMessage>(validateTabMessage)

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
    static parse = parser<NativeMessage>(validateNativeMessage)

    id: number
    type: string
    frame?: Frame
    frame_id: string
    url?: string
    payload: object

    public constructor(
        id: number,
        type: string,
        frame: Frame | undefined,
        frame_id: string,
        url: string,
        payload: object,
    ) {
        this.id = id
        this.type = type
        this.frame = frame
        this.frame_id = frame_id
        this.url = url
        this.payload = payload
    }
}

export class RPCResponsePayload {
    static parse = parser<RPCResponsePayload>(validateResponsePayload)

    request_id: string
    result: any // eslint-disable-line @typescript-eslint/no-explicit-any
}

export class OpenURLPayload {
    static parse = parser<OpenURLPayload>(validateOpenURLPayload)

    url: string
}

export class PageConnectMessage {
    static parse = parser<PageConnectMessage>(validatePageConnect)

    type = "page_connect_message"
}
