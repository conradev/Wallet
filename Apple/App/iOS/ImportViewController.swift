import SwiftUI
import UIKit

class ImportViewController: UIViewController, UITextViewDelegate {
    weak var delegate: ImportViewControllerDelegate?

    let headerTitle: String
    let placeholder: String
    let action: String

    var string: String {
        textView.text
    }

    lazy var scrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.preservesSuperviewLayoutMargins = true
        scrollView.layoutMargins = .init(top: 50, left: 0, bottom: 50, right: 0)
        return scrollView
    }()

    lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.alignment = .fill
        stackView.distribution = .equalSpacing
        stackView.spacing = UIStackView.spacingUseSystem
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.preservesSuperviewLayoutMargins = true
        return stackView
    }()

    lazy var iconView: UIImageView = {
        let imageView = UIImageView(image: .init(systemName: "square.and.arrow.down.on.square.fill"))
        imageView.preferredSymbolConfiguration = .init(pointSize: 48, weight: .bold)
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()

    lazy var titleLabel: UILabel = {
        let titleLabel = UILabel()
        titleLabel.text = headerTitle
        titleLabel.numberOfLines = 0
        titleLabel.textAlignment = .center
        titleLabel.font = OnboardingView.titleFont
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        return titleLabel
    }()

    lazy var textView: UITextView = {
        let cornerRadius = 15.0
        let inset = 10.0

        let textView = UITextView()
        textView.layer.cornerCurve = .continuous
        textView.layer.cornerRadius = cornerRadius
        textView.textContainerInset = .init(top: inset, left: inset, bottom: inset, right: inset)
        textView.backgroundColor = .systemGroupedBackground
        textView.autocorrectionType = .no
        textView.autocapitalizationType = .none
        textView.spellCheckingType = .yes
        textView.keyboardType = .asciiCapable
        textView.textContentType = .password
        textView.translatesAutoresizingMaskIntoConstraints = false
        textView.returnKeyType = .continue
        textView.delegate = self
        textView.font = .init(
            descriptor: .preferredFontDescriptor(withTextStyle: .title2)
                .withSymbolicTraits([.traitBold, .traitMonoSpace])!,
            size: UIFont.preferredFont(forTextStyle: .title2).pointSize
        )
        return textView
    }()

    lazy var button: UIButton = {
        let buttonAction = UIAction(
            title: action,
            image: nil,
            identifier: nil,
            discoverabilityTitle: nil,
            attributes: [],
            state: .on) { [weak self] action in
                guard let self = self else { return }
                self.delegate?.action(string: self.textView.text)
        }
        let button = UIButton(type: .custom, primaryAction: buttonAction)
        button.setAttributedTitle(
            .init(
                string: action,
                attributes: [
                    .font: UIFont.systemFont(
                        ofSize: UIFont.preferredFont(forTextStyle: .body).pointSize,
                        weight: .semibold
                    ),
                    .foregroundColor: UIColor.white
                ]
            ),
            for: .normal
        )
        button.setBackgroundColor(.tintColor, cornerRadius: 15, for: .normal)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.isEnabled = false
        return button
    }()

    init(headerTitle: String, placeholder: String, action: String) {
        self.headerTitle = headerTitle
        self.placeholder = placeholder
        self.action = action
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        view.addSubview(scrollView)

        scrollView.addSubview(stackView)
        stackView.addArrangedSubview(iconView)
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(textView)
        stackView.addArrangedSubview(button)

        NSLayoutConstraint.activate([
            scrollView.leadingAnchor.constraint(equalTo: view.layoutMarginsGuide.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.layoutMarginsGuide.trailingAnchor),
            scrollView.topAnchor.constraint(equalTo: view.layoutMarginsGuide.topAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.layoutMarginsGuide.bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: view.layoutMarginsGuide.leadingAnchor).priority(.defaultHigh),
            stackView.trailingAnchor.constraint(equalTo: view.layoutMarginsGuide.trailingAnchor).priority(.defaultHigh),
            stackView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            stackView.widthAnchor.constraint(lessThanOrEqualToConstant: 700),
            stackView.topAnchor.constraint(equalTo: scrollView.layoutMarginsGuide.topAnchor),
            stackView.bottomAnchor.constraint(equalTo: scrollView.layoutMarginsGuide.bottomAnchor),
            textView.heightAnchor.constraint(lessThanOrEqualTo: view.safeAreaLayoutGuide.heightAnchor, multiplier: 0.4)
                .priority(.defaultHigh),
            textView.heightAnchor.constraint(greaterThanOrEqualToConstant: 250),
            button.heightAnchor.constraint(equalToConstant: 48)
        ])
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        textView.becomeFirstResponder()
    }

    // MARK: UITextViewDelegate

    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        guard !text.isEmpty else {
            return true
        }

        if text.rangeOfCharacter(from: .newlines.inverted) == nil {
            delegate?.action(string: string)
            return false
        }

        if let delegate = delegate, text.count == 1, !delegate.accept(character: text[text.startIndex]) {
            return false
        }

        return true
    }

    func textViewDidChange(_ textView: UITextView) {
        if let cleaned = delegate?.clean(string: textView.text), cleaned != textView.text {
            textView.text = cleaned
        }

        if let delegate = delegate {
            button.isEnabled = delegate.validate(string: textView.text)
        }
    }
}

extension UIButton {
    func setBackgroundColor(_ color: UIColor?, cornerRadius: CGFloat, for state: UIControl.State) {
        guard let color = color else {
            setBackgroundImage(nil, for: state)
            return
        }

        let length = 20 + cornerRadius * 2
        let size = CGSize(width: length, height: length)
        let backgroundImage = UIGraphicsImageRenderer(size: size).image { context in
            color.setFill()
            UIBezierPath(roundedRect: context.format.bounds, cornerRadius: cornerRadius).fill()
        }
            .resizableImage(
                withCapInsets: .init(top: cornerRadius, left: cornerRadius, bottom: cornerRadius, right: cornerRadius)
            )
            .withTintColor(
                color,
                renderingMode: .alwaysOriginal
            )

        setBackgroundImage(backgroundImage, for: state)
    }
}
