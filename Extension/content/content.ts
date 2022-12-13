import { Runtime, runtime } from "webextension-polyfill"
import { BaseMessage, PageConnectMessage, StartSessionMessage } from "../page/messages"
import { ContentPageMessage } from "./messages"

class BackgroundWorkerMessageRelay {
    private _backgroundWorkerPort?: Runtime.Port = undefined
    private _pageScriptPort?: MessagePort = undefined

    public constructor() {
        const listener = (event) => {
            this.handlePageConnection(event)
        }
        window.addEventListener("message", listener)
    }

    private get backgroundWorkerPort(): Runtime.Port {
        if (this._backgroundWorkerPort) {
            return this._backgroundWorkerPort
        }

        const onMessage = (message) => {
            this.handleBackgroundMessage(message)
        }
        const onDisconnect = () => {
            this._backgroundWorkerPort = undefined
        }

        const port = runtime.connect()
        port.onMessage.addListener(onMessage)
        port.onDisconnect.addListener(onDisconnect)
        this._backgroundWorkerPort = port

        return port
    }

    private get pageScriptPort(): MessagePort | undefined {
        return this._pageScriptPort
    }

    private set pageScriptPort(pageScriptPort: MessagePort | undefined) {
        const existing = this.pageScriptPort
        if (existing) {
            existing.onmessage = null
            existing.onmessageerror = null
        }
        if (pageScriptPort) {
            const handler = (event: MessageEvent) => {
                this.handlePageMessage(event)
            }
            pageScriptPort.onmessage = handler
            pageScriptPort.onmessageerror = handler
        }
        this._pageScriptPort = pageScriptPort
    }

    private handlePageConnection(event: MessageEvent) {
        if (event.source !== window) {
            return
        }
        const message = PageConnectMessage.parse(event.data)
        if (!message) {
            return
        }

        this.pageScriptPort = event.ports[0]

        this.backgroundWorkerPort.postMessage(ContentPageMessage.from(new StartSessionMessage(), window))
    }

    private handlePageMessage(event: MessageEvent) {
        const pageMessage = BaseMessage.parse(event.data)
        if (!pageMessage) {
            return
        }

        const contentPageMessage = ContentPageMessage.from(pageMessage, window)
        this.backgroundWorkerPort.postMessage(contentPageMessage)
    }

    private handleBackgroundMessage(data) {
        const pageMessage = BaseMessage.parse(data)
        if (!pageMessage) {
            return
        }

        this.pageScriptPort?.postMessage(pageMessage)
    }
}

declare global {
    interface Window { relay: BackgroundWorkerMessageRelay; }
}

window.relay = new BackgroundWorkerMessageRelay()

const script = document.createElement("script")
script.src = runtime.getURL("page.js")
script.onload = () => {
    script.remove()
};
(document.head || document.documentElement).appendChild(script)
