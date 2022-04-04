# Contributing

Right now, the project is currently moving very quickly and most of the plan is not yet written down. If you are interested in contributing, reach out to one of the [maintainers](../.github/CODEOWNERS), and they can set you up with something to work on!

There is a wide variety of things that the project would love help with – writing new features, writing unit tests, writing UI tests, documenting classes, etc.

Before you get started, you should read a brief overview of the [architecture](ARCHITECTURE.md).

## Conventions

There are a few general conventions that the project tries to adhere to when writing new code:

- **If code can be written in Kotlin, write it in Kotlin**. If it is not written in Kotlin, it will have to be duplicated across platforms.

- **Support all platforms in parallel**. If you are going to write a new feature for iOS, take the time to write the code for macOS and Android as well. It is okay to put changes up for review with a single platform, but it will only be merged once full platform support is done. Letting platforms become out of sync on the main branch is a bad precedent to set.

- **Code should be simple, well-designed and easy to understand**. Google's [Code Review Guidelines](https://google.github.io/eng-practices/review/reviewer/looking-for.html) are a great reference here. If any point from that should be emphasized, it is simplicity: do not over-engineer code.

- **Be thoughtful when introducing dependencies**. There is a cost to integrating external code into the repository, and it needs to be weighed against the alternative cost of writing it yourself. When possible, choose dependencies that have a long history of correctness and reliability.

## Testing

Testing is very important to this project because it is cross-platform. It is only possible to build and run the code for a single platform at a time, so you will often develop locally against only a single platform.

Before putting code up for review, it is important that you test it on all of the other supported platforms. Not only that the code builds, but also that it runs correctly. Both can be issues for Kotlin Multiplatform, because there is a big divergence between Kotlin Native and Kotlin on the JVM.

To make sure that the code continues to work long after it is initially contributed, it is strongly encouraged that you write unit tests for new classes, or UI tests for new screens.

## Commit Messages

Try to structure commit messages using this [template](COMMIT.md) (adapted from the [Git Book](https://www.git-scm.com/book/en/v2/Distributed-Git-Contributing-to-a-Project#_commit_guidelines)).

## Code Style

As much as possible, the project tries to incoporate all stylistic preferences into the linter configuration. There are linters for Kotlin, Swift and Git, and you can run them using the commands below:

```bash
$ ./gradlew lintKotlin
```

```bash
$ brew install swiftlint
$ swiftlint
```

```bash
$ pip install gitlint
$ gitlint
```
