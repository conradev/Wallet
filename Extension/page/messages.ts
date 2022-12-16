import { parser } from "../shared/parser"
import {
    validatePageMessage,
    validatePageConnectMessage,
    validateMessageType,
    validateRPCResponseMessage,
    validateEventMessage,
    validateOpenURLMessage,
} from "./validators"

export class PageConnectMessage {
    static readonly type = "page_connect"
    static parse = parser<PageConnectMessage>(validatePageConnectMessage, (_m) => new PageConnectMessage(), true)

    readonly type = PageConnectMessage.type
}

export class MessageType {
    static parse = parser<MessageType>(validateMessageType, (m) => new MessageType(m))

    type: string

    public constructor(message: MessageType) {
        this.type = message.type
    }
}

export class PageMessage {
    readonly id: number
    readonly type: string
    readonly payload: object
}

export class BaseMessage implements PageMessage {
    static parse = parser<BaseMessage>(validatePageMessage, (m) => new BaseMessage(m))

    readonly id: number
    readonly type: string
    readonly payload: object

    public constructor(message: PageMessage) {
        this.id = message.id
        this.type = message.type
        this.payload = message.payload
    }
}

export class StartSessionMessage extends BaseMessage {
    static readonly type = "start_session"

    readonly id: number
    readonly type = StartSessionMessage.type
    readonly payload: object

    public constructor() {
        super({
            id: Math.floor(Math.random() * 4294967295),
            type: StartSessionMessage.type,
            payload: {},
        })
    }
}

export class RPCRequestMessage extends BaseMessage {
    static readonly type = "rpc_request"

    readonly id: number
    readonly type = RPCRequestMessage.type
    readonly payload: object

    public constructor(payload: object) {
        super({
            id: Math.floor(Math.random() * 4294967295),
            type: RPCRequestMessage.type,
            payload: payload,
        })
    }
}

class RPCResponsePayload {
    readonly request_id: string
    readonly result?: any // eslint-disable-line @typescript-eslint/no-explicit-any
    readonly error?: object

    constructor(payload: RPCResponsePayload) {
        this.request_id = payload.request_id
        this.result = payload.result
        this.error = payload.error
    }
}

export class RPCResponseMessage extends BaseMessage {
    static readonly type = "rpc_response"
    static parse = parser<RPCResponseMessage>(validateRPCResponseMessage, (m) => new RPCResponseMessage(m))

    readonly id: number
    readonly type = RPCResponseMessage.type
    readonly payload: RPCResponsePayload

    public constructor(message: RPCResponseMessage) {
        super({
            id: message.id,
            type: message.type,
            payload: new RPCResponsePayload(message.payload),
        })
    }
}

export class EventPayload {
    readonly name: string
    readonly value: any // eslint-disable-line @typescript-eslint/no-explicit-any

    constructor(payload: EventPayload) {
        this.name = payload.name
        this.value = payload.value
    }
}

export class EventMessage extends BaseMessage {
    static readonly type = "event"
    static parse = parser<EventMessage>(validateEventMessage, (m) => new EventMessage(m))

    readonly id: number
    readonly type = EventMessage.type
    readonly payload: EventPayload

    public constructor(message: EventMessage) {
        super({
            id: message.id,
            type: message.type,
            payload: new EventPayload(message.payload),
        })
    }
}

export class OpenURLPayload {
    readonly url: string

    constructor(payload: OpenURLPayload) {
        this.url = payload.url
    }
}

export class OpenURLMessage extends BaseMessage {
    static readonly type = "open_url"
    static parse = parser<OpenURLMessage>(validateOpenURLMessage, (m) => new OpenURLMessage(m))

    readonly id: number
    readonly type = OpenURLMessage.type
    readonly payload: OpenURLPayload

    public constructor(message: OpenURLMessage) {
        super({
            id: message.id,
            type: message.type,
            payload: new OpenURLPayload(message.payload),
        })
    }
}

export type Message =
    | RPCResponseMessage
    | EventMessage
    | OpenURLMessage

export function parseMessage(value): Message | undefined {
    const message = MessageType.parse(value)
    if (!message) {
        return undefined
    }

    switch (message.type) {
    case RPCResponseMessage.type:
        return RPCResponseMessage.parse(value)
    case EventMessage.type:
        return EventMessage.parse(value)
    case OpenURLMessage.type:
        return OpenURLMessage.parse(value)
    default:
        return undefined
    }
}
