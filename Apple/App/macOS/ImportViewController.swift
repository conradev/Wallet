import SwiftUI

class ImportViewController: NSViewController, NSTextViewDelegate {
    weak var delegate: ImportViewControllerDelegate?

    let headerTitle: String
    let placeholder: String

    var string: String {
        textView.string
    }

    lazy var stackView: NSStackView = {
        let stackView = NSStackView()
        stackView.orientation = .vertical
        stackView.alignment = .centerX
        stackView.distribution = .equalSpacing
        stackView.spacing = 24
        stackView.translatesAutoresizingMaskIntoConstraints = false
        return stackView
    }()

    lazy var iconView: NSImageView = {
        let imageView = NSImageView(
            image: .init(systemSymbolName: "square.and.arrow.down.on.square.fill", accessibilityDescription: "Import")!
        )
        imageView.symbolConfiguration = .init(pointSize: 24, weight: NSFont.Weight.bold)
            .applying(.init(hierarchicalColor: .controlAccentColor))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.imageScaling = .scaleProportionallyUpOrDown
        return imageView
    }()

    lazy var titleLabel: NSTextField = {
        let titleLabel = NSTextField()
        titleLabel.stringValue = headerTitle
        titleLabel.backgroundColor = .clear
        titleLabel.isBezeled = false
        titleLabel.isEditable = false
        titleLabel.font = OnboardingView.titleFont
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        return titleLabel
    }()

    lazy var textView: NSTextView = {
        let cornerRadius = 15.0
        let inset = 10.0

        let textView = NSTextView()
        textView.delegate = self
        textView.wantsLayer = true
        textView.layer?.cornerCurve = .continuous
        textView.layer?.cornerRadius = cornerRadius
        textView.layer?.borderColor = NSColor.separatorColor.cgColor
        textView.layer?.borderWidth = 1
        textView.textContainerInset = .init(width: inset, height: inset)
        textView.font = .init(
            descriptor: .preferredFontDescriptor(forTextStyle: .title2)
                .withSymbolicTraits([.bold, .monoSpace]),
            size: NSFont.preferredFont(forTextStyle: .title2).pointSize
        )
        textView.translatesAutoresizingMaskIntoConstraints = false
        textView.isRichText = false
        textView.isGrammarCheckingEnabled = false
        textView.isContinuousSpellCheckingEnabled = false
        textView.setAccessibilityIdentifier(.importPhraseField)
        return textView
    }()

    init(headerTitle: String, placeholder: String) {
        self.headerTitle = headerTitle
        self.placeholder = placeholder
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func loadView() {
        view = NSView()

        view.addSubview(stackView)

        stackView.addArrangedSubview(iconView)
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(textView)

        NSLayoutConstraint.activate([
            stackView.leadingAnchor.constraint(equalTo: view.layoutMarginsGuide.leadingAnchor).priority(.defaultHigh),
            stackView.trailingAnchor.constraint(equalTo: view.layoutMarginsGuide.trailingAnchor).priority(.defaultHigh),
            stackView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            stackView.widthAnchor.constraint(lessThanOrEqualToConstant: 700),
            stackView.topAnchor.constraint(equalTo: view.layoutMarginsGuide.topAnchor),
            stackView.bottomAnchor.constraint(equalTo: view.layoutMarginsGuide.bottomAnchor),
            textView.heightAnchor.constraint(greaterThanOrEqualToConstant: 200)
        ])
    }

    func action() {
        delegate?.action(string: string)
    }

    // MARK: NSTextViewDelegate

    func textDidChange(_ notification: Notification) {
        guard let delegate = delegate, let textView = notification.object as? NSTextView else { return }
        textView.string = delegate.clean(string: textView.string)
        _ = delegate.validate(string: textView.string)
    }

    func textView(
        _ textView: NSTextView,
        shouldChangeTextIn affectedCharRange: NSRange,
        replacementString: String?
    ) -> Bool {
        guard let replacementString = replacementString, !replacementString.isEmpty else {
            return true
        }

        if replacementString.rangeOfCharacter(from: .newlines.inverted) == nil {
            delegate?.action(string: string)
            return false
        }

        if
            let delegate = delegate,
            replacementString.count == 1,
            !delegate.accept(character: replacementString[replacementString.startIndex]) {
            return false
        }

        return true
    }

    func textView(_ view: NSTextView, menu: NSMenu, for event: NSEvent, at charIndex: Int) -> NSMenu? {
        menu
            .items
            .enumerated()
            .filter { _, item in item.action == #selector(NSMenu.submenuAction(_:)) }
            .map { index, _ in index }
            .sorted { $0 > $1 }
            .forEach { menu.removeItem(at: $0) }

        return menu
    }
}
