import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinInitIosKt.doInitKoinIos()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    AuthCallback_iosKt.handleAuthCallback(url: url.absoluteString)
                }
        }
    }
}
