package org.shareat.shared.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Marker for a [NavKey] whose destination requires an authenticated session.
 *
 * `Navigator` redirects any route implementing this interface through the login flow before
 * navigating to it. This lives in its own module (rather than in `:shared:ui` or a feature
 * module) so that any feature can declare a key that requires login without creating a
 * circular dependency back on the app/composition module.
 */
interface RequiresLogin : NavKey
