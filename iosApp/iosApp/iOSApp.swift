import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinInitIosKt.doInitKoinIos(useFakeData: false)
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
