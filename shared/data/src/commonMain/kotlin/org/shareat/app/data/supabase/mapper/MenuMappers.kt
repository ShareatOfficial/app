package org.shareat.app.data.supabase.mapper

import org.shareat.app.data.supabase.model.MenuDto
import org.shareat.app.data.supabase.model.MenuItemDto
import org.shareat.app.data.supabase.model.MenuItemUpdateDto
import org.shareat.app.data.supabase.model.SaveRestaurantMenuRpc
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuItemDraft
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantMenuDraft

internal fun MenuDto.currency(): Currency = restaurant?.currencyCode?.toCurrency() ?: Currency.Euro

internal fun MenuDto.toDomain(currency: Currency = currency()): Menu = Menu(
    id = MenuId(id),
    restaurantId = RestaurantId(restaurantId),
    name = name,
    description = description,
    publicationState = when (publicationState) {
        "draft" -> MenuPublicationState.Draft
        "published" -> MenuPublicationState.Published
        "unpublished" -> MenuPublicationState.Unpublished
        "disabled" -> MenuPublicationState.Disabled
        else -> error("Unsupported menu state: $publicationState")
    },
    price = priceMinorUnits?.let { Money(it, currency) },
)

internal fun MenuItemDto.toDomain(dish: Dish, currency: Currency): MenuDish = MenuDish(
    dish = dish,
    price = Money(priceMinorUnits, currency),
    position = position,
    isEnabled = isEnabled,
)

internal fun RestaurantMenuDraft.toSaveRpc(): SaveRestaurantMenuRpc = SaveRestaurantMenuRpc(
    restaurantId = restaurantId.value,
    menuId = menuId?.value,
    name = name.trim(),
    description = description?.trim()?.ifEmpty { null },
    publicationState = publicationState.toDatabaseValue(),
    priceMinorUnits = price?.minorUnits,
    items = items.sortedBy(MenuItemDraft::position).mapIndexed { position, item ->
        MenuItemUpdateDto(
            dishId = item.dishId.value,
            priceMinorUnits = item.price.minorUnits,
            position = position,
            isEnabled = item.isEnabled,
        )
    },
)

private fun MenuPublicationState.toDatabaseValue(): String = when (this) {
    MenuPublicationState.Draft -> "draft"
    MenuPublicationState.Published -> "published"
    MenuPublicationState.Unpublished -> "unpublished"
    MenuPublicationState.Disabled -> "disabled"
}
