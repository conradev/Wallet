import { runtime, Runtime, tabs } from "webextension-polyfill"
import { TabMessage, NativeMessage } from "../shared/messages"

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
        port.onMessage.addListener((message, port: Runtime.Port) => {
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

    private handleTabMessage(portId: string, message, sender: Runtime.MessageSender) {
        const tabMessage = TabMessage.parse(message)
        if (!tabMessage || !sender.url) {
            return
        }

        this.nativePort.postMessage(
            new NativeMessage(
                tabMessage.id,
                tabMessage.type,
                tabMessage.frame,
                portId,
                sender.url,
                tabMessage.payload,
            ),
        )
    }

    private handleNativeMessage(message) {
        if (message && typeof message === "object" && Object.keys(message).length === 0) {
            console.log("Receiving placeholder message, sending two in response")
            this.nativePort.postMessage({})
            this.nativePort.postMessage({})
            return
        }

        const nativeMessage = NativeMessage.parse(message)
        if (!nativeMessage) {
            return
        }

        const port = this.contentScriptPorts[nativeMessage.frame_id]
        if (!port) {
            return
        }

        port.postMessage(new TabMessage(
            nativeMessage.id,
            nativeMessage.type,
            nativeMessage.payload,
            nativeMessage.frame,
        ))
    }
}

declare global {
    interface ServiceWorkerGlobalScope { relay: NativeHostMessageRelay; }
}

globalThis.relay = new NativeHostMessageRelay()
