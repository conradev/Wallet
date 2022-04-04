const browser = window.browser || window.chrome;
const applicationId = "com.conradkramer.wallet";

browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (window.chrome) {
        browser.runtime.sendNativeMessage(applicationId, message, (response) => {
            console.log(response);
            sendResponse(response);
        });
    } else {
        browser.runtime.sendNativeMessage(applicationId, message).then(sendResponse, sendResponse);
    }

    return true;
});
