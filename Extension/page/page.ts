import { PageConnectMessage, RPCResponsePayload, OpenURLPayload, TabMessage, Frame } from "../shared/messages"

class Ethereum {
    private static readonly RPC_REQUEST_MESSAGE = "rpc_request"
    private static readonly RPC_RESPONSE_MESSAGE = "rpc_response"
    private static readonly OPEN_URL_MESSAGE = "open_url"

    private readonly contentScriptPort: MessagePort
    private readonly promises: Record<number, [(response: object) => void, (reason?: string) => void]> = {}

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private readonly eventHandlers: Record<string, [(response) => void]> = {}

    public constructor() {
        const channel = new MessageChannel()
        this.contentScriptPort = channel.port1

        const handler = (event) => {
            this.handleContentMessage(event)
        }
        this.contentScriptPort.onmessage = handler
        this.contentScriptPort.onmessageerror = handler

        window.postMessage(new PageConnectMessage(), "*", [channel.port2])
    }

    private handleContentMessage(event: MessageEvent<TabMessage>) {
        const message = TabMessage.parse(event.data)
        if (!message) {
            return
        }

        if (message.type === Ethereum.RPC_RESPONSE_MESSAGE) {
            const payload = RPCResponsePayload.parse(message.payload)
            if (!payload) {
                return
            }

            const promise = this.promises[payload.request_id]
            if (!promise) {
                return
            }

            const [respond, _reject] = promise
            respond(payload.result)
            delete this.promises[payload.request_id]
            return
        }

        if (message.type === Ethereum.OPEN_URL_MESSAGE) {
            const payload = OpenURLPayload.parse(message.payload)
            if (!payload) {
                return
            }

            window.location.assign(payload.url)
        }
    }

    on(event: string, handler: (value) => void) {
        this.eventHandlers[event] = this.eventHandlers[event] || []
        this.eventHandlers[event].push(handler)
    }

    async request(request: object): Promise<object> {
        const id = Math.floor(Math.random() * 4294967295)
        return new Promise((resolve, reject) => {
            this.promises[id] = [resolve, reject]
            this.contentScriptPort.postMessage(
                new TabMessage(
                    id,
                    Ethereum.RPC_REQUEST_MESSAGE,
                    request,
                    new Frame(window),
                ),
            )
        })
    }
}

class MetaMask {
    async isUnlocked(): Promise<boolean> {
        return Promise.resolve(true)
    }
}

export class LazyEthereum {
    _metamask = new MetaMask()
    private _ethereum: Ethereum | undefined = undefined

    private get ethereum(): Ethereum {
        return this._ethereum || (this._ethereum = new Ethereum())
    }

    isConnected(): boolean {
        return true
    }

    async request(request: object): Promise<object> {
        return this.ethereum.request(request)
    }
}

declare global {
    interface Window { ethereum: LazyEthereum }
}

window.ethereum = new LazyEthereum()
