window.ethereum = {
    request: (request) => {
        return new Promise((resolve, reject) => {
            const channel = new MessageChannel();
            channel.port1.onmessage = (message) => {
                resolve(message.data);
            };
            channel.port1.onmessageerror = () => {
                reject(undefined);
            };
            window.postMessage({ type: "eth_request", message: request }, "*", [channel.port2]);
        });
    }
};
