import "./transaction.css"
import SendTransaction from "./SendTransaction.svelte"

const app = new SendTransaction({
    target: document.getElementById("app"),
})

export default app
