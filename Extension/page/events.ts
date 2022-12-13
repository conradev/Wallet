import { parser } from "../shared/parser"
import { EventPayload } from "./messages"
import {
    validateConnectEvent,
} from "./validators"

export class ConnectEvent {
    static parse = parser<ConnectEvent>(validateConnectEvent, (e) => new ConnectEvent(e))
    static readonly type = "connect"

    readonly chainId: string

    constructor(event: ConnectEvent) {
        this.chainId = event.chainId
    }
}

export type EthereumEvent =
    | ConnectEvent

export function parseEvent(value: EventPayload): EthereumEvent | undefined {
    switch (value.name) {
    case ConnectEvent.type:
        return ConnectEvent.parse(value)
    default:
        return undefined
    }
}
