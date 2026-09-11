package org.shareat.app.data.supabase.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import org.shareat.app.data.supabase.model.MenuDto
import org.shareat.app.data.supabase.model.RestaurantDto
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.Weekday

/**
 * Pins the shape PostgREST returns for the embedded selects the repositories issue. These payloads
 * are verbatim responses from the project: a renamed relation or serial name breaks decoding at
 * runtime only, so it is worth asserting against the real thing rather than a hand-written double.
 */
class EmbeddedPayloadTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun aMenuArrivesWithItsCurrencyItemsDishesAndAllergens() {
        val payload = """
            {"id":"c4444444-4444-4444-4444-444444444444",
             "restaurant_id":"a4444444-4444-4444-4444-444444444444",
             "name":"Menú Sakura","description":"Sushi rolls",
             "publication_state":"published","price_minor_units":2950,
             "restaurants":{"currency_code":"EUR"},
             "menu_items":[{"dishes":{"id":"d0000004-0004-0004-0004-000000000001",
               "restaurant_id":"a4444444-4444-4444-4444-444444444444",
               "name":"Dragon Roll","image_path":null,"is_enabled":true,
               "description":"Roll de tempura","allergen_note":null,
               "dish_allergens":[{"allergen_id":"cereals_containing_gluten"},
                                 {"allergen_id":"crustaceans"},
                                 {"allergen_id":"soybeans"}],
               "image_alt_text":null},
              "dish_id":"d0000004-0004-0004-0004-000000000001","position":0,
              "is_enabled":true,"price_minor_units":1350}]}
        """.trimIndent()

        val dto = json.decodeFromString<MenuDto>(payload)

        assertEquals(Currency.Euro, dto.currency())
        assertEquals(2_950, dto.toDomain().price?.minorUnits)

        val item = dto.items.single()
        assertEquals(1_350, item.priceMinorUnits)

        val dish = assertNotNull(item.dish).toDomain { it }
        assertEquals("Dragon Roll", dish.name)
        assertEquals(dto.restaurantId, dish.restaurantId.value)
        assertEquals(
            setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Crustaceans, EuAllergen.Soybeans),
            assertNotNull(dish.allergenDeclaration).allergens,
        )
    }

    @Test
    fun aRestaurantArrivesWithItsWeeklySchedule() {
        val payload = """
            {"id":"a3333333-3333-3333-3333-333333333333",
             "owner_account_id":"b3333333-3333-3333-3333-333333333333",
             "name":"Casa Paco","street_line":"Calle Sierpes 42","locality":"Sevilla",
             "postal_code":"41004","country_code":"ES","currency_code":"EUR",
             "publication_state":"published",
             "restaurant_opening_periods":[
               {"weekday":1,"opens_at":"12:00:00","position":0,"closes_at":"16:00:00"},
               {"weekday":1,"opens_at":"20:00:00","position":1,"closes_at":"23:30:00"},
               {"weekday":2,"opens_at":"12:00:00","position":0,"closes_at":"16:00:00"}]}
        """.trimIndent()

        val restaurant = json.decodeFromString<RestaurantDto>(payload).toDomain { it }

        assertEquals(listOf(Weekday.Monday, Weekday.Tuesday), restaurant.openingHours.days.map { it.day })
        val monday = restaurant.openingHours.days.first()
        assertEquals(2, monday.periods.size)
        assertEquals(12, monday.periods.first().opensAt.hour)
        assertEquals(23, monday.periods.last().closesAt.hour)
        assertEquals(Currency.Euro, restaurant.currency)
    }

    @Test
    fun anEmptyEmbedDegradesToNoScheduleRatherThanFailing() {
        val payload = """
            {"id":"a1","owner_account_id":"b1","name":"Sin horario","street_line":"Calle 1",
             "locality":"Madrid","postal_code":"28001","country_code":"ES",
             "publication_state":"draft","restaurant_opening_periods":[]}
        """.trimIndent()

        val restaurant = json.decodeFromString<RestaurantDto>(payload).toDomain { it }

        assertEquals(emptyList(), restaurant.openingHours.days)
    }
}
