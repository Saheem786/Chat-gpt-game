package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AnimalSpecies
import com.example.data.model.CropType
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.data.model.Season
import com.example.data.model.SettlementTier
import com.example.data.model.WeatherType

class Converters {

    @TypeConverter
    fun fromItemId(value: ItemId?): String? = value?.name

    @TypeConverter
    fun toItemId(value: String?): ItemId? = value?.let { enumValueOf<ItemId>(it) }

    @TypeConverter
    fun fromAnimalSpecies(value: AnimalSpecies): String = value.name

    @TypeConverter
    fun toAnimalSpecies(value: String): AnimalSpecies = enumValueOf<AnimalSpecies>(value)

    @TypeConverter
    fun fromCropType(value: CropType?): String? = value?.name

    @TypeConverter
    fun toCropType(value: String?): CropType? = value?.let { enumValueOf<CropType>(it) }

    @TypeConverter
    fun fromWeatherType(value: WeatherType): String = value.name

    @TypeConverter
    fun toWeatherType(value: String): WeatherType = enumValueOf<WeatherType>(value)

    @TypeConverter
    fun fromSeason(value: Season): String = value.name

    @TypeConverter
    fun toSeason(value: String): Season = enumValueOf<Season>(value)

    @TypeConverter
    fun fromSettlementTier(value: SettlementTier): String = value.name

    @TypeConverter
    fun toSettlementTier(value: String): SettlementTier = enumValueOf<SettlementTier>(value)

    @TypeConverter
    fun fromPricingStrategy(value: PricingStrategy): String = value.name

    @TypeConverter
    fun toPricingStrategy(value: String): PricingStrategy = enumValueOf<PricingStrategy>(value)
}
