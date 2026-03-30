package com.wem.snoozy.domain.entity

data class DayItem(
    val id: Int,
    val name: String,
    val checked: Boolean
)

enum class DaysName {

    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;

    fun getDisplayName(): String {
        return when (this) {
            MONDAY -> "Пн"
            TUESDAY -> "Вт"
            WEDNESDAY -> "Ср"
            THURSDAY -> "Чт"
            FRIDAY -> "Пт"
            SATURDAY -> "Сб"
            SUNDAY -> "Вс"
        }
    }
}
