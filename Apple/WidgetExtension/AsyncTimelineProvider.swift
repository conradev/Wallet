import WidgetKit

protocol AsyncTimelineProvider: TimelineProvider {
    func snapshot(in context: Context) async -> Entry
    func timeline(in context: Context) async -> Timeline<Entry>
}

extension AsyncTimelineProvider {
    func getSnapshot(in context: TimelineProviderContext, completion: @escaping (Entry) -> Void) {
        Task { completion(await snapshot(in: context)) }
    }

    func getTimeline(in context: TimelineProviderContext, completion: @escaping (Timeline<Entry>) -> Void) {
        Task { completion(await timeline(in: context)) }
    }
}

protocol AsyncIntentTimelineProvider: IntentTimelineProvider {
    func snapshot(for configuration: Intent, in context: Context) async -> Entry
    func timeline(for configuration: Intent, in context: Context) async -> Timeline<Entry>
}

extension AsyncIntentTimelineProvider {
    func getSnapshot(for configuration: Intent, in context: Context, completion: @escaping (Entry) -> Void) {
        Task { completion(await snapshot(for: configuration, in: context)) }
    }

    func getTimeline(for configuration: Intent, in context: Context, completion: @escaping (Timeline<Entry>) -> Void) {
        Task { completion(await timeline(for: configuration, in: context)) }
    }
}
