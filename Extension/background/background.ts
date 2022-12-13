import { runtime, Runtime, tabs } from "webextension-polyfill"
import { ContentPageMessage } from "../content/messages"
import { BackgroundPageMessage, NativeHostMessage, Session } from "./messages"

class NativeHostMessageRelay {
    private static readonly extensionId = "com.conradkramer.wallet"

    private _nativePort: Runtime.Port | undefined = undefined
    private readonly contentPagePorts: Record<string, Runtime.Port> = {}

    private static session(sender: Runtime.MessageSender): Session | undefined {
        const frameId = sender.frameId
        if (frameId === null || frameId === undefined) {
            return undefined
        }

        const tab = sender.tab
        if (!tab || tab.id === null || tab.id === undefined || tab.id === tabs.TAB_ID_NONE) {
            return undefined
        }

        return Session.from(tab.id, frameId)
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

        const onMessage = (message) => {
            this.handleNativeMessage(message)
        }
        const onDisconnect = () => {
            this._nativePort = undefined
        }

        const port = runtime.connectNative(NativeHostMessageRelay.extensionId)
        port.onMessage.addListener(onMessage)
        port.onDisconnect.addListener(onDisconnect)
        this._nativePort = port

        return port
    }

    private handleTabConnection(port: Runtime.Port) {
        const sender = port.sender
        if (!sender) {
            port.disconnect()
            return
        }

        const session = NativeHostMessageRelay.session(sender)
        if (!session) {
            port.disconnect()
            return
        }

        const onMessage = (message, port: Runtime.Port) => {
            this.handleTabMessage(message, port)
        }
        const onDisconnect = () => {
            delete this.contentPagePorts[session.id]
        }

        this.contentPagePorts[session.id] = port
        port.onMessage.addListener(onMessage)
        port.onDisconnect.addListener(onDisconnect)
    }

    private handleTabMessage(message, port: Runtime.Port) {
        const sender = port.sender
        if (!sender) {
            port.disconnect()
            return
        }

        const session = NativeHostMessageRelay.session(sender)
        if (!session) {
            port.disconnect()
            return
        }

        const url = sender.url
        if (!url) {
            return
        }

        const contentPageMessage = ContentPageMessage.parse(message)
        if (!contentPageMessage) {
            return
        }

        const backgroundPageMessage = new BackgroundPageMessage(message, url, session)
        this.nativePort.postMessage(backgroundPageMessage)
    }

    private handleNativeMessage(message) {
        if (message && typeof message === "object" && Object.keys(message).length === 0) {
            console.log("Receiving placeholder message from Safari, sending two in response")
            this.nativePort.postMessage({})
            this.nativePort.postMessage({})
            return
        }

        const nativeMessage = NativeHostMessage.parse(message)
        if (!nativeMessage) {
            return
        }

        const port = this.contentPagePorts[nativeMessage.session.id]
        if (!port) {
            return
        }

        port.postMessage(nativeMessage.base)
    }
}

declare global {
    interface ServiceWorkerGlobalScope { relay: NativeHostMessageRelay; }
}

globalThis.relay = new NativeHostMessageRelay()
