import {
    parseMessage,
    PageConnectMessage,
    RPCRequestMessage,
    RPCResponseMessage,
    EventMessage,
    OpenURLMessage,
} from "./messages"
import { parseEvent, ConnectEvent, EthereumEvent } from "./events"

class Ethereum {
    private readonly contentScriptPort: MessagePort

    private readonly requests: Record<number, [(response: object) => void, (reason?: string) => void]> = {}
    private readonly eventHandlers: Record<string, [(response) => void]> = {}

    private connectEvent?: ConnectEvent

    public constructor() {
        const channel = new MessageChannel()

        const handler = (event) => {
            this.handleContentMessage(event)
        }
        channel.port1.onmessage = handler
        channel.port1.onmessageerror = handler

        this.contentScriptPort = channel.port1
        window.postMessage(new PageConnectMessage(), "*", [channel.port2])
    }

    private handleContentMessage(event: MessageEvent) {
        const message = parseMessage(event.data)
        if (!message) {
            return
        }

        switch (message.type) {
        case RPCResponseMessage.type:
            this.handleRPCResponse(message)
            break
        case EventMessage.type:
            this.handleEventMessage(message)
            break
        case OpenURLMessage.type:
            window.location.assign(message.payload.url)
            break
        }
    }

    private handleRPCResponse(message: RPCResponseMessage) {
        const promise = this.requests[message.payload.request_id]
        if (!promise) {
            return
        }
        const [respond, reject] = promise
        if (message.payload.error) {
            reject(message.payload.error)
        } else {
            respond(message.payload.result)
        }

        delete this.requests[message.payload.request_id]
    }

    private handleEventMessage(message: EventMessage) {
        const handlers = this.eventHandlers[message.payload.name]
        if (!handlers) {
            return
        }

        const event = parseEvent(message.payload)
        if (event) {
            this.handleEvent(event)
        }

        for (const handler of [...handlers]) {
            handler(message.payload.value)
        }
    }

    private handleEvent(event: EthereumEvent) {
        if (event instanceof ConnectEvent) {
            this.connectEvent = event
        }
    }

    isConnected(): boolean {
        return this.connectEvent !== undefined
    }

    on(event: string, handler: (value) => void): Ethereum {
        this.eventHandlers[event] = this.eventHandlers[event] || []
        this.eventHandlers[event].push(handler)

        if (event === ConnectEvent.type && this.connectEvent) {
            handler(this.connectEvent)
        }

        return this
    }

    removeListener(event: string, handler: (value) => void): Ethereum {
        const handlers = this.eventHandlers[event]
        if (!handlers) {
            return this
        }

        const index = handlers.indexOf(handler)
        if (index > -1) {
            handlers.splice(index, 1)
        }
        if (!handlers.length) {
            delete this.eventHandlers[event]
        }

        return this
    }

    async request(request: object): Promise<object> {
        return new Promise((resolve, reject) => {
            const message = new RPCRequestMessage(request)
            this.requests[message.id] = [resolve, reject]
            this.contentScriptPort.postMessage(message)
        })
    }
}

export class LazyEthereum {
    private _ethereum: Ethereum | undefined = undefined

    private get ethereum(): Ethereum {
        return this._ethereum || (this._ethereum = new Ethereum())
    }

    isConnected(): boolean {
        return this.ethereum.isConnected()
    }

    on(event: string, handler: (value) => void) {
        return this.ethereum.on(event, handler)
    }

    removeListener(event: string, handler: (value) => void): Ethereum {
        return this.ethereum.removeListener(event, handler)
    }

    async request(request: object): Promise<object> {
        return this.ethereum.request(request)
    }
}

declare global {
    interface Window { ethereum: LazyEthereum }
}

window.ethereum = new LazyEthereum()
