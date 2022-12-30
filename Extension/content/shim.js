(async () => {
    await import(chrome.runtime.getURL("content/content.js"))
})()
