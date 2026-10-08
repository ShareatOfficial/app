package org.shareat.feature.lastactivity.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LastActivityFabActionTest {
    @Test
    fun fabIsUnavailableUntilActivityHasResolved() {
        assertEquals(LastActivityFabAction.Hidden, LastActivityUiState.Initializing.fabAction())
        assertEquals(LastActivityFabAction.Hidden, LastActivityUiState.Loading.fabAction())
    }

    @Test
    fun guestFabLeadsToLoginAndResolvedActivityFabAddsAReview() {
        assertEquals(LastActivityFabAction.Login, LastActivityUiState.Guest.fabAction())
        assertEquals(LastActivityFabAction.AddReview, LastActivityUiState.Empty.fabAction())
        assertEquals(
            LastActivityFabAction.AddReview,
            LastActivityUiState.Content(emptyList()).fabAction(),
        )
        assertEquals(
            LastActivityFabAction.AddReview,
            LastActivityUiState.Error(LastActivityError.UNKNOWN).fabAction(),
        )
    }
}
