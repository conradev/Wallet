import { Runtime, runtime } from "webextension-polyfill"
import { PageConnectMessage } from "./messages"

class BackgroundWorkerMessageRelay {
    private _backgroundWorkerPort?: Runtime.Port = undefined
    private _pageScriptPort?: MessagePort = undefined

    public constructor() {
        window.addEventListener("message", (event) => {
            this.handlePageConnection(event)
        })
    }

    private get backgroundWorkerPort(): Runtime.Port {
        if (this._backgroundWorkerPort) {
            return this._backgroundWorkerPort
        }

        const port = runtime.connect()
        port.onMessage.addListener((message: object) => {
            this.handleBackgroundMessage(message)
        })
        port.onDisconnect.addListener(() => {
            this._backgroundWorkerPort = undefined
        })
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
            const handler = (event: MessageEvent<object>) => {
                this.handlePageMessage(event)
            }
            pageScriptPort.onmessage = handler
            pageScriptPort.onmessageerror = handler
        }
        this._pageScriptPort = pageScriptPort
    }

    private handlePageConnection(event: MessageEvent<object>) {
        if (event.source !== window || !PageConnectMessage.validate(event.data)) {
            return
        }

        this.pageScriptPort = event.ports[0]
    }

    private handlePageMessage(event: MessageEvent<object>) {
        this.backgroundWorkerPort.postMessage(event.data)
    }

    private handleBackgroundMessage(message: object) {
        this.pageScriptPort?.postMessage(message)
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
