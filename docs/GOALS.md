## Motivation

The motivation behind creating this app is to create a digital equivalent to a physical wallet that holds cash.

In today's world, most of the ways to send money or transact online involve a central party that can both see the full details of your transaction and control if it goes through. Sometimes this is desirable – to protect you from fraud, for example. Sometimes you'd rather not have anyone see your transactions or have control over them. This wallet exists to support those cases.

Currently, the wallet only supports Ethereum, which satisfies some of that – the transactions cannot be controlled. But the app does not currently do anything to hide your transactions. Support for that will come soon.

## Goals

The term "wallet" has many meanings these days, but this app focuses on the definition that most people know – replacing the thing that you currently carry around in your pocket. It is easier to start with what this app is _not_ for, rather than what it _is_ for:

- **The app is not for storing hundreds of thousands of dollars**. Even though having the app is way more secure than carrying around cash, it is still not a good idea to carry around that much money in your pocket. It is not a replacement for a bank – there is no insurance policy, no cold storage, and no multi-signature scheme.

- **The app is not for DeFi investing**. It will support as many tokens as possible and decentralized exchanges to exchange them, but it will not track value over time, show you coins you don't know anything about, or promote financial products.

The app is going to focus on:

- **Navigating the web**. It is going to support all major browsers (Chrome, Safari, Firefox) on all of the platforms it supports. It will use native extensions when possible for a seamless browsing experience, and will fall back to WalletConnect when not.

- **Paying your friends**. It is going to make sending and receiving money easier than ever.

- **Security**. This app will aim to be the most secure wallet around. Security is much more than storing your keys in the hardware of the device (which this app does). Security includes preventing you from being phished and avoiding placing too much trust in centralized authorities.

- **Being cross-platform**. This app aims to eventually work anywhere that someone would like it to work.

- **Being free**. This app is free, open source and will never profit off of anything you do. Right now, it does not cost anything to maintain, but may accept donations in the future.
