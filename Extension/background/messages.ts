import { Frame, ContentPageMessage } from "../content/messages"
import { BaseMessage } from "../page/messages"
import { parser } from "../shared/parser"
import { validateNativeHostMessage } from "./validators"

export class Session {
    readonly browser_pid: number
    readonly tab_id: number
    readonly frame_id: number

    static from(tabId: number, frameId: number): Session {
        return new Session({
            id: "",
            browser_pid: 0,
            tab_id: tabId,
            frame_id: frameId,
        })
    }

    get id(): string {
        return `${this.tab_id}-${this.frame_id}`
    }

    public constructor(session: Session) {
        this.browser_pid = session.browser_pid
        this.tab_id = session.tab_id
        this.frame_id = session.frame_id
    }
}

export class BackgroundPageMessage {
    readonly id: number
    readonly type: string
    readonly payload: object
    readonly frame: Frame
    readonly url: string
    readonly session: Session

    public constructor(message: ContentPageMessage, url: string, session: Session) {
        this.id = message.id
        this.type = message.type
        this.payload = message.payload
        this.frame = message.frame
        this.url = url
        this.session = session
    }
}

export class NativeHostMessage {
    static parse = parser<NativeHostMessage>(validateNativeHostMessage, (m) => new NativeHostMessage(m))

    readonly id: number
    readonly type: string
    readonly payload: object
    readonly session: Session

    get base(): BaseMessage {
        return new BaseMessage({
            id: this.id,
            type: this.type,
            payload: this.payload,
        })
    }

    public constructor(message: NativeHostMessage) {
        this.id = message.id
        this.type = message.type
        this.payload = message.payload
        this.session = new Session(message.session)
    }
}
