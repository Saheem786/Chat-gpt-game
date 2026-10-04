package com.example.data.model

import androidx.compose.ui.graphics.Color

// Available items in the Solarpunk Eco-Economy
enum class ItemId(
    val displayName: String,
    val category: ItemCategory,
    val iconEmoji: String,
    val basePrice: Int,
    val description: String,
    val isOrganic: Boolean = true
) {
    // Livestock Products
    EGGS("Fresh Farm Eggs", ItemCategory.LIVESTOCK_PRODUCT, "🥚", 8, "Organic eggs from free-range hens."),
    DUCK_EGGS("Duck Eggs", ItemCategory.LIVESTOCK_PRODUCT, "🥚", 12, "Rich, nutrient-dense eggs from foraging ducks."),
    COW_MILK("Fresh Cow Milk", ItemCategory.LIVESTOCK_PRODUCT, "🥛", 14, "Creamy raw milk from pasture-fed cows."),
    GOAT_MILK("Artisan Goat Milk", ItemCategory.LIVESTOCK_PRODUCT, "🥛", 16, "Easy-to-digest goat milk with delicate flavor."),
    SHEEP_WOOL("Fine Wool", ItemCategory.LIVESTOCK_PRODUCT, "🧶", 22, "Soft, clean wool shorn with animal care."),
    HONEY("Wildflower Honey", ItemCategory.LIVESTOCK_PRODUCT, "🍯", 25, "Pure golden honey from solar apiaries."),
    BEESWAX("Natural Beeswax", ItemCategory.LIVESTOCK_PRODUCT, "🕯️", 18, "Pristine wax used for salves and waterproofing."),
    LEATHER("Eco-Hide", ItemCategory.LIVESTOCK_PRODUCT, "👞", 30, "Ethically harvested durable leather hide."),
    MEAT("Pasture Meat", ItemCategory.LIVESTOCK_PRODUCT, "🥩", 28, "Nutritious meat from healthy livestock."),
    MANURE("Raw Manure", ItemCategory.WASTE_RESOURCE, "💩", 3, "High-nitrogen animal waste. Key to organic compost!"),

    // Agricultural Crops
    WHEAT("Golden Wheat", ItemCategory.RAW_CROP, "🌾", 6, "Organically grown heritage wheat stalks."),
    CORN("Sweet Corn", ItemCategory.RAW_CROP, "🌽", 8, "Vibrant yellow solar-ripened corn ears."),
    TOMATO("Vine Tomato", ItemCategory.RAW_CROP, "🍅", 10, "Sweet and juicy heirloom tomatoes."),
    CARROT("Crisp Carrots", ItemCategory.RAW_CROP, "🥕", 7, "Rich in beta-carotene, grown in compost loam."),
    LETTUCE("Crisp Greens", ItemCategory.RAW_CROP, "🥬", 8, "Tender vertical-farmed crisp greens."),
    STRAWBERRY("Ruby Strawberries", ItemCategory.RAW_CROP, "🍓", 15, "Fragrant strawberries ripened under the sun."),
    MINT("Wild Mint", ItemCategory.RAW_CROP, "🌿", 12, "Aromatic herb used for teas, balms, and tinctures."),
    LAVENDER("French Lavender", ItemCategory.RAW_CROP, "🪻", 16, "Calming purple blooms loved by honeybees."),

    // Seeds
    SEED_WHEAT("Wheat Seeds", ItemCategory.SEEDS, "🌱", 3, "Heritage seed packet for 1 crop plot."),
    SEED_TOMATO("Tomato Seeds", ItemCategory.SEEDS, "🌱", 4, "High-yield heirloom tomato seeds."),
    SEED_CARROT("Carrot Seeds", ItemCategory.SEEDS, "🌱", 3, "Fast-growing sweet carrot seeds."),
    SEED_STRAWBERRY("Berry Seeds", ItemCategory.SEEDS, "🌱", 6, "Perennial berry runners."),
    SEED_HERB("Herb Seeds", ItemCategory.SEEDS, "🌱", 5, "Mixed mint and lavender aromatic seeds."),

    // Sustainable Fishery
    TILAPIA("Fresh Tilapia", ItemCategory.FISH, "🐟", 18, "Clean farmed fish from solar aquaponics."),
    RIVER_TROUT("River Trout", ItemCategory.FISH, "🐟", 26, "Cold-water trout caught sustainably."),
    SEAWEED("Nutrient Seaweed", ItemCategory.FISH, "🌿", 10, "Rich green algae full of trace minerals."),

    // Ecological Soil & Energy Loop
    COMPOST("Rich Compost", ItemCategory.ECOLOGY, "🍂", 8, "Decomposed manure and crop residue."),
    BIO_FERTILIZER("Solar Bio-Fertilizer", ItemCategory.ECOLOGY, "🧪", 20, "Boosts soil fertility +50% and doubles yield."),
    BIOGAS_CANISTER("Biogas Fuel", ItemCategory.ECOLOGY, "⚡", 25, "Clean renewable energy generated from manure."),

    // Processed Artisan Goods
    FLOUR("Stoneground Flour", ItemCategory.PROCESSED_GOOD, "🥡", 14, "Milled in the community wind turbine mill."),
    ARTISAN_BREAD("Solar Sourdough", ItemCategory.PROCESSED_GOOD, "🍞", 32, "Baked in clean solar-thermal brick ovens."),
    ARTISAN_CHEESE("Aged Herb Cheese", ItemCategory.PROCESSED_GOOD, "🧀", 42, "Handcrafted aged cheese infused with garden herbs."),
    ORGANIC_BUTTER("Golden Butter", ItemCategory.PROCESSED_GOOD, "🧈", 24, "Cultured butter from pasture-fed cow cream."),
    BERRY_JAM("Solar Berry Jam", ItemCategory.PROCESSED_GOOD, "🫙", 38, "Slow-simmered strawberry jam sweetened with honey."),
    SOLAR_JUICE("Cold-Pressed Juice", ItemCategory.PROCESSED_GOOD, "🧃", 28, "Fresh fruit and mint juice pressed using solar power."),
    ECO_FABRIC("Woven Eco-Cloth", ItemCategory.PROCESSED_GOOD, "🧵", 50, "Breathable natural fabric from sheep wool."),
    HANDMADE_BLANKET("Artisan Blanket", ItemCategory.PROCESSED_GOOD, "🧣", 95, "Warm patterned throw blanket for chilly winters."),
    LEATHER_TOOLBELT("Crafted Toolbelt", ItemCategory.PROCESSED_GOOD, "👝", 85, "Hand-stitched vegetable-tanned leather gear."),
    HERBAL_BALM("Soothing Herb Balm", ItemCategory.PROCESSED_GOOD, "🧴", 48, "Beeswax and lavender medicinal skin salve."),
    SMOKED_FISH("Solar Smoked Trout", ItemCategory.PROCESSED_GOOD, "🍱", 55, "Oak-smoked sustainably harvested fish.")
}

enum class ItemCategory(val label: String) {
    LIVESTOCK_PRODUCT("Livestock"),
    RAW_CROP("Farm Crops"),
    SEEDS("Seeds"),
    FISH("Fishery"),
    WASTE_RESOURCE("Eco Waste"),
    ECOLOGY("Soil & Loop"),
    PROCESSED_GOOD("Artisan Goods")
}

// Animal Types & Biology
enum class AnimalSpecies(
    val displayName: String,
    val emoji: String,
    val purchaseCost: Int,
    val feedType: String,
    val shelterName: String,
    val primaryProduce: ItemId,
    val produceFrequencyHours: Int,
    val producesManure: Boolean,
    val breedingMaturityDays: Int
) {
    CHICKEN("Chicken", "🐔", 50, "Grains / Seeds", "Solar Coop", ItemId.EGGS, 12, true, 3),
    COW("Dairy Cow", "🐄", 280, "Hay / Pasture", "Lush Barn", ItemId.COW_MILK, 16, true, 7),
    GOAT("Alpine Goat", "🐐", 160, "Grass / Shrubs", "Solar Paddock", ItemId.GOAT_MILK, 14, true, 5),
    SHEEP("Merino Sheep", "🐑", 190, "Clover Pasture", "Meadow Pen", ItemId.SHEEP_WOOL, 28, true, 6),
    PIG("Pasture Pig", "🐖", 140, "Crop Scraps", "Mud & Shade Pen", ItemId.MEAT, 36, true, 5),
    DUCK("Water Duck", "🦆", 70, "Pond Weeds / Grain", "Duck Pond Shelter", ItemId.DUCK_EGGS, 14, true, 4),
    BEES("Honeybee Swarm", "🐝", 90, "Wildflowers", "Solar Smart Apiary", ItemId.HONEY, 18, false, 2)
}

// Crop Species
enum class CropType(
    val displayName: String,
    val emoji: String,
    val seedItem: ItemId,
    val harvestItem: ItemId,
    val growthHours: Int,
    val baseYield: Int,
    val preferredSeason: Season?,
    val waterNeedsPerHour: Float
) {
    WHEAT("Golden Wheat", "🌾", ItemId.SEED_WHEAT, ItemId.WHEAT, 18, 4, Season.SUMMER, 0.4f),
    CORN("Sweet Corn", "🌽", ItemId.SEED_WHEAT, ItemId.CORN, 22, 5, Season.AUTUMN, 0.5f),
    TOMATO("Vine Tomato", "🍅", ItemId.SEED_TOMATO, ItemId.TOMATO, 20, 5, Season.SUMMER, 0.6f),
    CARROT("Crisp Carrot", "🥕", ItemId.SEED_CARROT, ItemId.CARROT, 14, 4, Season.SPRING, 0.3f),
    STRAWBERRY("Strawberry", "🍓", ItemId.SEED_STRAWBERRY, ItemId.STRAWBERRY, 26, 6, Season.SPRING, 0.5f),
    HERBS("Garden Herbs", "🌿", ItemId.SEED_HERB, ItemId.MINT, 12, 3, null, 0.2f)
}

// Weather & Seasons
enum class WeatherType(val displayName: String, val emoji: String, val solarMultiplier: Float, val windMultiplier: Float, val isRaining: Boolean) {
    SUNNY("Bright Sunshine", "☀️", 1.25f, 0.8f, false),
    PARTLY_CLOUDY("Gentle Breeze", "⛅", 0.9f, 1.1f, false),
    RAINY("Nutrient Rain", "🌧️", 0.4f, 1.2f, true),
    HEATWAVE("Dry Heatwave", "🔥", 1.4f, 0.5f, false),
    DROUGHT("Severe Drought", "🏜️", 1.3f, 0.7f, false),
    WIND_STORM("High Winds", "💨", 0.5f, 2.2f, false)
}

enum class Season(val displayName: String, val emoji: String, val perkText: String) {
    SPRING("Spring", "🌸", "+25% Animal breeding & fast sprout growth"),
    SUMMER("Summer", "☀️", "+30% Solar generation; cold drink demand spikes"),
    AUTUMN("Autumn", "🍂", "+25% Crop harvest yields; wholesale market bonus"),
    WINTER("Winter", "❄️", "Freezing fields! Greenhouses & wool blankets surge")
}

// Settlement Progression Tiers
enum class SettlementTier(
    val level: Int,
    val title: String,
    val subtitle: String,
    val requiredCoins: Int,
    val requiredEcoScore: Int,
    val perks: String
) {
    HOMESTEAD(1, "Off-Grid Homestead", "Small garden & cozy coop", 0, 0, "Basic crafting and local barter."),
    FARMSTEAD(2, "Sustainable Farmstead", "Dairy cows & composting loop", 500, 30, "Unlocks Composter, Dairy, and Windmill."),
    ARTISAN_SETTLEMENT(3, "Artisan Workshop Village", "Solar bakery, loom & retail store", 1600, 55, "Unlocks Retail Eco-Shop and Smokehouse."),
    GREENHOUSE_HUB(4, "Solar Greenhouse & Aquaponics", "Closed loop vertical aquaculture", 3800, 75, "Unlocks Biogas Plant, Aquaponics, and Solar Greenhouses."),
    SOLARPUNK_UTOPIA(5, "Solarpunk Eco-Enterprise", "Self-sustaining circular paradise", 8000, 90, "Maximum solar grid, certified organic +50% price bonus!")
}

// Workshop Crafting Recipe
data class CraftingRecipe(
    val id: String,
    val name: String,
    val building: String,
    val inputItem: ItemId,
    val inputQuantity: Int,
    val secondaryInput: ItemId? = null,
    val secondaryQuantity: Int = 0,
    val outputItem: ItemId,
    val outputQuantity: Int,
    val durationHours: Int,
    val energyCost: Float
)

object WorkshopRecipes {
    val ALL = listOf(
        // Compost & Bio-Refinery
        CraftingRecipe("compost_basic", "Compost Batch", "Bio-Composter", ItemId.MANURE, 3, null, 0, ItemId.COMPOST, 2, 4, 0f),
        CraftingRecipe("bio_fertilizer", "Solar Bio-Fertilizer", "Bio-Composter", ItemId.COMPOST, 2, ItemId.SEAWEED, 1, ItemId.BIO_FERTILIZER, 2, 6, 2f),
        CraftingRecipe("biogas_fuel", "Manure Biogas Extraction", "Biogas Digester", ItemId.MANURE, 5, null, 0, ItemId.BIOGAS_CANISTER, 1, 8, 0f),

        // Windmill & Bakery
        CraftingRecipe("mill_flour", "Mill Grain into Flour", "Wind Turbine Mill", ItemId.WHEAT, 2, null, 0, ItemId.FLOUR, 2, 3, 1f),
        CraftingRecipe("bake_bread", "Solar Sourdough Bread", "Solar Bakery", ItemId.FLOUR, 2, ItemId.COW_MILK, 1, ItemId.ARTISAN_BREAD, 2, 5, 3f),

        // Dairy & Creamery
        CraftingRecipe("dairy_cheese", "Aged Herb Cheese", "Solar Creamery", ItemId.COW_MILK, 2, ItemId.MINT, 1, ItemId.ARTISAN_CHEESE, 1, 6, 2f),
        CraftingRecipe("dairy_butter", "Churn Golden Butter", "Solar Creamery", ItemId.COW_MILK, 2, null, 0, ItemId.ORGANIC_BUTTER, 2, 4, 1.5f),

        // Food Processing & Juicer
        CraftingRecipe("solar_juice", "Cold-Pressed Solar Juice", "Solar Juicer", ItemId.STRAWBERRY, 2, ItemId.MINT, 1, ItemId.SOLAR_JUICE, 2, 3, 1f),
        CraftingRecipe("berry_jam", "Artisan Berry Honey Jam", "Preserve Kitchen", ItemId.STRAWBERRY, 2, ItemId.HONEY, 1, ItemId.BERRY_JAM, 2, 5, 2f),

        // Textiles & Craft
        CraftingRecipe("weave_cloth", "Spin & Weave Eco-Cloth", "Solar Loom", ItemId.SHEEP_WOOL, 2, null, 0, ItemId.ECO_FABRIC, 1, 5, 2f),
        CraftingRecipe("craft_blanket", "Handwoven Winter Blanket", "Solar Loom", ItemId.ECO_FABRIC, 2, null, 0, ItemId.HANDMADE_BLANKET, 1, 8, 3f),
        CraftingRecipe("leather_gear", "Handcrafted Toolbelt", "Leathercraft Bench", ItemId.LEATHER, 2, null, 0, ItemId.LEATHER_TOOLBELT, 1, 6, 1.5f),
        CraftingRecipe("herbal_balm", "Medicinal Herb Balm", "Apothecary Lab", ItemId.BEESWAX, 1, ItemId.LAVENDER, 2, ItemId.HERBAL_BALM, 2, 4, 1f),

        // Smokehouse
        CraftingRecipe("smoked_trout", "Solar Smoked Trout", "Eco-Smokehouse", ItemId.TILAPIA, 2, null, 0, ItemId.SMOKED_FISH, 2, 5, 1f)
    )
}

// Pricing Strategy for Eco-Shop
enum class PricingStrategy(val label: String, val priceMultiplier: Float, val customerAppealMultiplier: Float) {
    COMMUNITY_DISCOUNT("Community Budget (-20%)", 0.8f, 1.5f),
    FAIR_TRADE("Fair Eco Price (Standard)", 1.0f, 1.0f),
    PREMIUM_ORGANIC("Certified Organic (+35%)", 1.35f, 0.75f),
    LUXURY_ARTISAN("Artisan Solarpunk (+75%)", 1.75f, 0.45f)
}
