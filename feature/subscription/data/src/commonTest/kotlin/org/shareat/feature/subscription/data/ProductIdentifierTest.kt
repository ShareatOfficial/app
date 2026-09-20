package org.shareat.feature.subscription.data

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductIdentifierTest {
    @Test
    fun extractsGooglePlayBasePlanId() {
        assertEquals(
            "quarterly",
            "shareat_unlimited:quarterly".toSubscriptionProductId(),
        )
    }

    @Test
    fun preservesSingleStoreProductId() {
        assertEquals("lifetime", "lifetime".toSubscriptionProductId())
    }
}
