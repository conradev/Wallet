import { PageMessage } from "../page/messages"
import { parser } from "../shared/parser"
import { validateContentPageMessage } from "./validators"

export class Frame {
    readonly x: number
    readonly y: number
    readonly width: number
    readonly height: number

    public constructor(window: Window) {
        this.x = window.screenLeft
        this.y = window.screenTop
        this.width = window.outerWidth
        this.height = window.outerHeight
    }
}

export class ContentPageMessage {
    static parse = parser<ContentPageMessage>(validateContentPageMessage, (m) => new ContentPageMessage(m))

    static from(message: PageMessage, window: Window): ContentPageMessage {
        return new ContentPageMessage({
            id: message.id,
            type: message.type,
            payload: message.payload,
            frame: new Frame(window),
        })
    }

    readonly id: number
    readonly type: string
    readonly payload: object
    readonly frame: Frame

    public constructor(message: ContentPageMessage) {
        this.id = message.id
        this.type = message.type
        this.payload = message.payload
        this.frame = message.frame
    }
}
