import { runtime, Runtime, tabs } from "webextension-polyfill"
import { TabMessage, NativeMessage } from "./messages"

class NativeHostMessageRelay {
    private static readonly extensionId = "com.conradkramer.wallet"

    private _nativePort: Runtime.Port | undefined = undefined
    private readonly contentScriptPorts: Record<string, Runtime.Port> = {}

    private static senderIdentifier(sender: Runtime.MessageSender | undefined): string | undefined {
        if (!sender) {
            return undefined
        }

        const tab = sender.tab
        if (!tab || tab.id == null || tab.id === tabs.TAB_ID_NONE || !sender.url || sender.frameId == null) {
            return undefined
        }
        return `${tab.id}-${sender.frameId}`
    }

    public constructor() {
        runtime.onConnect.addListener((port) => {
            this.handleTabConnection(port)
        })
    }

    private get nativePort(): Runtime.Port {
        if (this._nativePort) {
            return this._nativePort
        }

        const port = runtime.connectNative(NativeHostMessageRelay.extensionId)
        port.onMessage.addListener((message: object) => {
            this.handleNativeMessage(message)
        })
        port.onDisconnect.addListener(() => {
            this._nativePort = undefined
        })
        this._nativePort = port
        return port
    }

    private handleTabConnection(port: Runtime.Port) {
        const portId = NativeHostMessageRelay.senderIdentifier(port.sender)
        if (!portId) {
            port.disconnect()
            return
        }

        const unwrappedPortId = portId
        port.onMessage.addListener((message: object, port: Runtime.Port) => {
            if (!port.sender) {
                return
            }

            this.handleTabMessage(unwrappedPortId, message, port.sender)
        })
        port.onDisconnect.addListener(() => {
            delete this.contentScriptPorts[unwrappedPortId]
        })
        this.contentScriptPorts[portId] = port
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private handleTabMessage(portId: string, message: any, sender: Runtime.MessageSender) {
        if (!TabMessage.validate(message) || !sender.url) {
            return
        }

        this.nativePort.postMessage(
            new NativeMessage(
                message.id,
                message.type,
                message.frame,
                portId,
                sender.url,
                message.payload,
            ),
        )
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private handleNativeMessage(message: any) {
        if (Object.keys(message).length === 0) {
            console.log("Receiving placeholder message, sending two in response")
            this.nativePort.postMessage({})
            this.nativePort.postMessage({})
            return
        }

        if (!NativeMessage.validate(message)) {
            return
        }

        const port = this.contentScriptPorts[message.frame_id]
        if (!port) {
            return
        }

        port.postMessage(new TabMessage(message.id, message.type, message.payload, message.frame))
    }
}

declare global {
    interface ServiceWorkerGlobalScope { relay: NativeHostMessageRelay; }
}

globalThis.relay = new NativeHostMessageRelay()
