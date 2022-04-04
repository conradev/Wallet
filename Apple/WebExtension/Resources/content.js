const browser = window.browser || window.chrome;

const script = document.createElement('script');
script.src = browser.runtime.getURL('page.js');
script.onload = () => { script.remove(); };
(document.head || document.documentElement).appendChild(script);

window.addEventListener("message", (event) => {
    if (event.source == window &&
        event.data &&
        event.data.type == "eth_request") {
        let port = event.ports[0];
        let callback = (response) => {
            port.postMessage(response);
        };

        if (window.chrome) {
            browser.runtime.sendMessage(event.data.message, callback);
        } else {
            browser.runtime.sendMessage(event.data.message).then(callback, callback);
        }
    }
});
