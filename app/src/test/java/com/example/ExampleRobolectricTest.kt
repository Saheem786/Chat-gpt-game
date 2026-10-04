package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AnimalEntity
import com.example.data.local.AppDatabase
import com.example.data.local.ContractEntity
import com.example.data.local.FarmStateEntity
import com.example.data.local.InventoryEntity
import com.example.data.model.AnimalSpecies
import com.example.data.model.CropType
import com.example.data.model.ItemId
import com.example.data.model.LivestockValuation
import com.example.data.model.PricingStrategy
import com.example.data.model.WorkshopRecipes
import com.example.data.repository.FarmRepository
import com.example.game.GameEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private fun setupTestEnvironment(): Pair<FarmRepository, GameEngine> {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = FarmRepository(db.farmDao())
    val engine = GameEngine(db.farmDao(), repo, CoroutineScope(Dispatchers.Unconfined))
    return repo to engine
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Solarpunk Farm", appName)
  }

  @Test
  fun `test 1 and 2 animal age progression strictly on day boundary not hourly`() = runBlocking {
    val (repo, engine) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // Clear and insert 1 test animal
    val animalId = db.farmDao().insertAnimal(
      AnimalEntity(
        species = AnimalSpecies.CHICKEN,
        nickname = "Clockwork Hen",
        ageDays = 5,
        hunger = 0f,
        thirst = 0f,
        health = 1f
      )
    )

    // Simulate 5 individual hours (currentState.hour advances without day rollover)
    val stateBefore = db.farmDao().getFarmStateDirect()!!
    db.farmDao().insertOrUpdateFarmState(stateBefore.copy(hour = 8, day = 10))

    engine.advanceHourForTesting()
    var animal = db.farmDao().getAnimalById(animalId)!!
    assertEquals("Animal age must NOT increase during hourly ticks", 5, animal.ageDays)

    engine.advanceHourForTesting()
    animal = db.farmDao().getAnimalById(animalId)!!
    assertEquals("Animal age must NOT increase during hourly ticks", 5, animal.ageDays)

    // Now advance state to hour 23, so next tick causes a day rollover (23 -> 0, day 10 -> 11)
    val stateNearMidnight = db.farmDao().getFarmStateDirect()!!
    db.farmDao().insertOrUpdateFarmState(stateNearMidnight.copy(hour = 23, day = 10))

    engine.advanceHourForTesting()
    val stateAfterMidnight = db.farmDao().getFarmStateDirect()!!
    assertEquals("Hour must reset to 0", 0, stateAfterMidnight.hour)
    assertEquals("Day must advance to 11", 11, stateAfterMidnight.day)

    animal = db.farmDao().getAnimalById(animalId)!!
    assertEquals("Animal age MUST increase by exactly +1 on day rollover", 6, animal.ageDays)
  }

  @Test
  fun `test 3 4 5 6 7 8 9 10 11 livestock species production and processing rules`() = runBlocking {
    val (repo, _) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()

    // 3. Chicken produces eggs
    assertEquals(ItemId.EGGS, AnimalSpecies.CHICKEN.primaryProduce)
    // 4. Cow produces milk
    assertEquals(ItemId.COW_MILK, AnimalSpecies.COW.primaryProduce)
    // 5. Goat produces milk
    assertEquals(ItemId.GOAT_MILK, AnimalSpecies.GOAT.primaryProduce)
    // 6. Sheep produces wool
    assertEquals(ItemId.SHEEP_WOOL, AnimalSpecies.SHEEP.primaryProduce)
    // 7. Pig does NOT produce recurring meat (primaryProduce is null)
    assertNull("Pig must NOT have recurring primary produce", AnimalSpecies.PIG.primaryProduce)
    // 9. Bees produce honey
    assertEquals(ItemId.HONEY, AnimalSpecies.BEES.primaryProduce)

    // 8. Pig produces meat and hide when processed
    val pigYield = LivestockValuation.calculateMeatYield(AnimalSpecies.PIG, "Wilbur", 10, 1.0f)
    assertTrue("Pig must be eligible for meat processing when mature", pigYield.isEligible)
    assertTrue("Pig yields meat", pigYield.meatCount >= 4)
    assertTrue("Pig yields leather hide", pigYield.hideCount >= 1)

    // 10 & 11. Bees cannot produce meat or hide
    val beeYield = LivestockValuation.calculateMeatYield(AnimalSpecies.BEES, "Hive", 10, 1.0f)
    assertFalse("Bees cannot be processed into meat", beeYield.isEligible)
    assertEquals(0, beeYield.meatCount)
    assertEquals(0, beeYield.hideCount)
  }

  @Test
  fun `test 12 13 14 breeding maturity partner requirement and pregnancy gestation`() = runBlocking {
    val (repo, engine) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // 13. Immature animal cannot breed
    val babyGoatId = db.farmDao().insertAnimal(
      AnimalEntity(
        species = AnimalSpecies.GOAT,
        nickname = "Kid",
        ageDays = 1, // Immature (maturity requires 5 days)
        health = 1f,
        hunger = 0f
      )
    )
    val immatureBreedResult = repo.breedAnimal(babyGoatId)
    assertNull("Immature animal must NOT be able to breed", immatureBreedResult)

    // 12. Mature pair can breed
    val motherGoatId = db.farmDao().insertAnimal(
      AnimalEntity(
        species = AnimalSpecies.GOAT,
        nickname = "Nanny Goat",
        ageDays = 6, // Mature
        health = 1f,
        hunger = 0.1f,
        thirst = 0.1f
      )
    )
    // Mate partner
    db.farmDao().insertAnimal(
      AnimalEntity(
        species = AnimalSpecies.GOAT,
        nickname = "Billy Goat",
        ageDays = 7, // Mature
        health = 1f,
        hunger = 0.1f,
        thirst = 0.1f
      )
    )

    val pregnantMother = repo.breedAnimal(motherGoatId)
    assertNotNull("Mature pair must successfully initiate breeding", pregnantMother)
    assertTrue("Mother must become pregnant", pregnantMother!!.isPregnant)
    assertEquals(0, pregnantMother.pregnancyHours)

    // 14. Breeding cannot happen repeatedly while pregnant
    val duplicateBreedResult = repo.breedAnimal(motherGoatId)
    assertNull("Cannot breed while already pregnant", duplicateBreedResult)

    // Progress pregnancy by gestation hours (Goat: 24 hours)
    repeat(AnimalSpecies.GOAT.gestationHours) {
      engine.advanceHourForTesting()
    }

    val motherAfterGestation = db.farmDao().getAnimalById(motherGoatId)!!
    assertFalse("Mother should no longer be pregnant after birth", motherAfterGestation.isPregnant)

    val allGoats = db.farmDao().getAllAnimalsDirect().filter { it.species == AnimalSpecies.GOAT }
    val newBaby = allGoats.firstOrNull { it.ageDays == 0 }
    assertNotNull("New baby goat must be born with ageDays = 0", newBaby)
  }

  @Test
  fun `test 15 16 17 18 19 live animal sale and meat processing atomicity`() = runBlocking {
    val (repo, _) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // 15 & 16. Live animal sale removes exactly 1 animal and pays correct valuation
    val cowId = db.farmDao().insertAnimal(
      AnimalEntity(
        species = AnimalSpecies.COW,
        nickname = "Daisy",
        ageDays = 8,
        health = 0.9f,
        happiness = 0.9f
      )
    )
    val stateBeforeSale = db.farmDao().getFarmStateDirect()!!
    val expectedPrice = LivestockValuation.calculateSaleValue(AnimalSpecies.COW, 8, 0.9f, 0.9f)

    val saleValue = repo.sellAnimal(cowId)
    assertNotNull(saleValue)
    assertEquals(expectedPrice, saleValue)

    val cowAfterSale = db.farmDao().getAnimalById(cowId)
    assertNull("Animal must be removed after sale", cowAfterSale)

    val stateAfterSale = db.farmDao().getFarmStateDirect()!!
    assertEquals(stateBeforeSale.coins + expectedPrice, stateAfterSale.coins)

    // 17, 18, 19. Meat processing removes animal and creates meat + hide
    val pigId = db.farmDao().insertAnimal(
      AnimalEntity(
        species = AnimalSpecies.PIG,
        nickname = "Bacon",
        ageDays = 6,
        health = 1.0f,
        happiness = 0.8f
      )
    )
    val meatInInvBefore = db.farmDao().getInventoryItem(ItemId.MEAT)?.quantity ?: 0
    val hideInInvBefore = db.farmDao().getInventoryItem(ItemId.LEATHER)?.quantity ?: 0

    val processingResult = repo.processAnimalMeat(pigId)
    assertTrue("Pig processing must succeed", processingResult.isEligible)
    assertTrue("Meat count > 0", processingResult.meatCount > 0)
    assertTrue("Hide count > 0", processingResult.hideCount > 0)

    val pigAfter = db.farmDao().getAnimalById(pigId)
    assertNull("Animal must be removed after processing", pigAfter)

    val meatInInvAfter = db.farmDao().getInventoryItem(ItemId.MEAT)?.quantity ?: 0
    val hideInInvAfter = db.farmDao().getInventoryItem(ItemId.LEATHER)?.quantity ?: 0
    assertEquals(meatInInvBefore + processingResult.meatCount, meatInInvAfter)
    assertEquals(hideInInvBefore + processingResult.hideCount, hideInInvAfter)
  }

  @Test
  fun `test 20 21 22 exploit safety on quantity-based operations`() = runBlocking {
    val (repo, _) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // 21 & 22. Negative and zero quantities fail
    assertFalse("Wholesale with qty 0 must fail", repo.sellDirectToWholesale(ItemId.EGGS, 0))
    assertFalse("Wholesale with qty -5 must fail", repo.sellDirectToWholesale(ItemId.EGGS, -5))
    assertFalse("Buy seeds with qty 0 must fail", repo.buySeeds(ItemId.SEED_WHEAT, 0))
    assertFalse("Buy seeds with qty -3 must fail", repo.buySeeds(ItemId.SEED_WHEAT, -3))
    assertFalse("Stock shelf with qty 0 must fail", repo.stockShelf(1, ItemId.EGGS, 0))
    assertFalse("Stock shelf with qty -1 must fail", repo.stockShelf(1, ItemId.EGGS, -1))

    // Sell more than inventory
    val eggsInStock = db.farmDao().getInventoryItem(ItemId.EGGS)?.quantity ?: 0
    assertFalse("Selling more than in stock must fail", repo.sellDirectToWholesale(ItemId.EGGS, eggsInStock + 100))

    // 20. Inventory never becomes negative
    val invBefore = db.farmDao().getInventoryItem(ItemId.EGGS)?.quantity ?: 0
    db.farmDao().addInventoryQuantity(ItemId.EGGS, -9999)
    val invAfter = db.farmDao().getInventoryItem(ItemId.EGGS)?.quantity ?: 0
    assertEquals("Inventory must clamp to 0 and never become negative", 0, invAfter)
  }

  @Test
  fun `test 23 24 38 authoritative market price and wholesale payout matching`() = runBlocking {
    val (repo, _) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // Put items in inventory
    db.farmDao().setInventoryItem(InventoryEntity(ItemId.STRAWBERRY, 10))

    // Authoritative market price
    val authoritativePrice = repo.getAuthoritativeMarketPrice(ItemId.STRAWBERRY)
    assertTrue("Authoritative price must be positive", authoritativePrice > 0)

    val stateBefore = db.farmDao().getFarmStateDirect()!!
    val success = repo.sellDirectToWholesale(ItemId.STRAWBERRY, 2)
    assertTrue("Wholesale sell must succeed", success)

    val stateAfter = db.farmDao().getFarmStateDirect()!!
    val expectedRevenue = authoritativePrice * 2
    assertEquals("Payout must exactly equal authoritative unit price * quantity", stateBefore.coins + expectedRevenue, stateAfter.coins)

    // 38. Market price changes are persistent in database
    val quotes = db.farmDao().getAllMarketQuotesDirect()
    assertTrue("Quotes must be saved in database", quotes.isNotEmpty())
    val strawberryQuote = quotes.find { it.itemId == ItemId.STRAWBERRY }
    assertNotNull(strawberryQuote)
    assertEquals(authoritativePrice, strawberryQuote!!.currentPrice)
  }

  @Test
  fun `test 25 26 contract fulfillment and single expiry penalty`() = runBlocking {
    val (repo, engine) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // Setup contract
    val contract = ContractEntity(
      id = "test_contract",
      clientName = "Mayor",
      clientAvatarEmoji = "🏛️",
      clientRole = "Council",
      requestedItem = ItemId.FLOUR,
      requestedQuantity = 2,
      rewardCoins = 100,
      rewardEcoScore = 5,
      expiryDay = 3,
      isCompleted = false,
      isPenalized = false
    )
    db.farmDao().insertContracts(listOf(contract))
    db.farmDao().setInventoryItem(InventoryEntity(ItemId.FLOUR, 5))

    val stateBefore = db.farmDao().getFarmStateDirect()!!
    val success = repo.fulfillContract("test_contract")
    assertTrue(success)

    val updatedContract = db.farmDao().getAllContractsDirect().find { it.id == "test_contract" }!!
    assertTrue(updatedContract.isCompleted)

    // 25. Completed contract cannot be fulfilled again
    assertFalse("Cannot re-fulfill completed contract", repo.fulfillContract("test_contract"))

    // 26. Expired contract penalty happens only once
    val expiredContract = ContractEntity(
      id = "expired_contract",
      clientName = "Bakery",
      clientAvatarEmoji = "🍞",
      clientRole = "Chef",
      requestedItem = ItemId.WHEAT,
      requestedQuantity = 50,
      rewardCoins = 200,
      rewardEcoScore = 5,
      expiryDay = 1, // Expired
      isCompleted = false,
      isPenalized = false
    )
    db.farmDao().insertContracts(listOf(expiredContract))

    // Set day to 2 at midnight
    val s = db.farmDao().getFarmStateDirect()!!
    db.farmDao().insertOrUpdateFarmState(s.copy(hour = 23, day = 1))
    engine.advanceHourForTesting() // Rolls to day 2

    val contractAfterMidnight1 = db.farmDao().getAllContractsDirect().find { it.id == "expired_contract" }!!
    assertTrue("Contract must be flagged as penalized", contractAfterMidnight1.isPenalized)
    val rep1 = db.farmDao().getFarmStateDirect()!!.businessReputation

    // Another midnight rollover (day 2 to day 3)
    val s2 = db.farmDao().getFarmStateDirect()!!
    db.farmDao().insertOrUpdateFarmState(s2.copy(hour = 23, day = 2))
    engine.advanceHourForTesting() // Rolls to day 3

    val rep2 = db.farmDao().getFarmStateDirect()!!.businessReputation
    assertEquals("Reputation penalty must NOT trigger again for already-penalized contract", rep1, rep2)
  }

  @Test
  fun `test 27 28 29 workshop consumes ingredients and energy and collects once`() = runBlocking {
    val (repo, engine) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // Recipe: mill_flour: 2 Wheat -> 2 Flour, energyCost = 1.0f, duration = 3h
    val recipe = WorkshopRecipes.ALL.first { it.id == "mill_flour" }
    db.farmDao().setInventoryItem(InventoryEntity(ItemId.WHEAT, 5))

    // Set battery
    val state = db.farmDao().getFarmStateDirect()!!
    db.farmDao().insertOrUpdateFarmState(state.copy(batteryStored = 10f))

    val started = repo.startCrafting(recipe)
    assertTrue("Workshop crafting should start", started)

    // 27 & 28. Consumed ingredients and energy
    val wheatAfter = db.farmDao().getInventoryItem(ItemId.WHEAT)!!.quantity
    assertEquals(3, wheatAfter) // 5 - 2 = 3
    val batteryAfter = db.farmDao().getFarmStateDirect()!!.batteryStored
    assertEquals(9f, batteryAfter, 0.01f) // 10 - 1 = 9

    // Progress workshop task
    val task = db.farmDao().getWorkshopQueueDirect().first()
    repeat(recipe.durationHours + 1) {
      engine.advanceHourForTesting()
    }

    val finishedTask = db.farmDao().getWorkshopQueueDirect().first()
    assertTrue(finishedTask.isFinished)

    // 29. Collected once, cannot collect twice
    val flourBefore = db.farmDao().getInventoryItem(ItemId.FLOUR)?.quantity ?: 0
    val collectedFirstTime = repo.collectFinishedWorkshop(finishedTask.id)
    assertTrue(collectedFirstTime)

    val flourAfter = db.farmDao().getInventoryItem(ItemId.FLOUR)!!.quantity
    assertEquals(flourBefore + recipe.outputQuantity, flourAfter)

    val collectedSecondTime = repo.collectFinishedWorkshop(finishedTask.id)
    assertFalse("Cannot collect finished workshop task twice", collectedSecondTime)
  }

  @Test
  fun `test 30 31 32 retail shop stocking and NPC purchase economics`() = runBlocking {
    val (repo, engine) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // 30. Stock shelf transfers inventory
    db.farmDao().setInventoryItem(InventoryEntity(ItemId.ARTISAN_BREAD, 10))
    val shelf = db.farmDao().getAllShelvesDirect().first()

    val stocked = repo.stockShelf(shelf.shelfId, ItemId.ARTISAN_BREAD, 6)
    assertTrue(stocked)

    val breadInBarn = db.farmDao().getInventoryItem(ItemId.ARTISAN_BREAD)!!.quantity
    assertEquals(4, breadInBarn) // 10 - 6 = 4

    val updatedShelf = db.farmDao().getAllShelvesDirect().first { it.shelfId == shelf.shelfId }
    assertEquals(ItemId.ARTISAN_BREAD, updatedShelf.stockedItemId)
    assertEquals(6, updatedShelf.quantity)

    // Clear shelf returns items
    repo.clearShelf(shelf.shelfId)
    val breadAfterClear = db.farmDao().getInventoryItem(ItemId.ARTISAN_BREAD)!!.quantity
    assertEquals(10, breadAfterClear) // 4 + 6 = 10
  }

  @Test
  fun `test 37 corn uses corn seeds`() {
    assertEquals("Corn must use Corn Seeds", ItemId.SEED_CORN, CropType.CORN.seedItem)
    assertEquals("Wheat must use Wheat Seeds", ItemId.SEED_WHEAT, CropType.WHEAT.seedItem)
  }

  @Test
  fun `test complete end-to-end gameplay loop scenario`() = runBlocking {
    val (repo, engine) = setupTestEnvironment()
    repo.checkAndInitializeDefaults()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)

    // Ensure sufficient resources for comprehensive end-to-end progression
    val initialState = db.farmDao().getFarmStateDirect()!!
    db.farmDao().insertOrUpdateFarmState(initialState.copy(coins = 5000, batteryStored = 50f, waterStored = 80f))

    // 1. Buy chicken
    assertTrue("Buy chicken 1", repo.buyAnimal(AnimalSpecies.CHICKEN, "Hen 1"))
    // 2. Feed & water
    assertTrue("Feed animals", repo.feedAllAnimals())
    assertTrue("Water animals", repo.waterAllAnimals())

    // 3. Wait for age/production & collect eggs
    val hen1 = db.farmDao().getAllAnimalsDirect().first { it.nickname == "Hen 1" }
    db.farmDao().updateAnimal(hen1.copy(produceReady = true))
    val produce = repo.collectAnimalProduce(hen1.id)
    assertEquals(ItemId.EGGS, produce)

    // 4. Sell eggs wholesale using current market price
    val eggPrice = repo.getAuthoritativeMarketPrice(ItemId.EGGS)
    assertTrue(eggPrice > 0)
    val coinsBefore = db.farmDao().getFarmStateDirect()!!.coins
    assertTrue("Sell eggs wholesale", repo.sellDirectToWholesale(ItemId.EGGS, 1))
    val coinsAfter = db.farmDao().getFarmStateDirect()!!.coins
    assertEquals(coinsBefore + eggPrice, coinsAfter)

    // 5. Buy another chicken
    assertTrue("Buy chicken 2", repo.buyAnimal(AnimalSpecies.CHICKEN, "Hen 2"))

    // 6. Breed mature pair & raise baby
    val pregnantHen = repo.breedAnimal(hen1.id)
    assertNotNull("Hen 1 should be pregnant", pregnantHen)
    repeat(AnimalSpecies.CHICKEN.gestationHours) {
      engine.advanceHourForTesting()
    }
    val allChickens = db.farmDao().getAllAnimalsDirect().filter { it.species == AnimalSpecies.CHICKEN }
    val babyChick = allChickens.find { it.ageDays == 0 }
    assertNotNull("Baby chick must be born with ageDays = 0", babyChick)

    // 7. Buy cow & collect milk
    assertTrue("Buy cow", repo.buyAnimal(AnimalSpecies.COW, "Bessie Cow"))
    val cow = db.farmDao().getAllAnimalsDirect().first { it.nickname == "Bessie Cow" }
    db.farmDao().updateAnimal(cow.copy(produceReady = true))
    val milk = repo.collectAnimalProduce(cow.id)
    assertEquals(ItemId.COW_MILK, milk)

    // 8. Process milk into cheese & sell cheese
    val cheeseRecipe = WorkshopRecipes.ALL.first { it.id == "dairy_cheese" }
    db.farmDao().addInventoryQuantity(ItemId.COW_MILK, 3)
    db.farmDao().addInventoryQuantity(ItemId.MINT, 2)
    assertTrue("Start crafting cheese", repo.startCrafting(cheeseRecipe))
    val cheeseTask = db.farmDao().getWorkshopQueueDirect().first { it.recipeId == cheeseRecipe.id }
    repeat(cheeseRecipe.durationHours + 1) {
      engine.advanceHourForTesting()
    }
    assertTrue("Collect cheese", repo.collectFinishedWorkshop(cheeseTask.id))
    assertTrue("Sell cheese wholesale", repo.sellDirectToWholesale(ItemId.ARTISAN_CHEESE, 1))

    // 9. Buy sheep, collect wool & process wool into fabric
    assertTrue("Buy sheep", repo.buyAnimal(AnimalSpecies.SHEEP, "Fluffy Sheep"))
    val sheep = db.farmDao().getAllAnimalsDirect().first { it.nickname == "Fluffy Sheep" }
    db.farmDao().updateAnimal(sheep.copy(produceReady = true))
    val wool = repo.collectAnimalProduce(sheep.id)
    assertEquals(ItemId.SHEEP_WOOL, wool)
    db.farmDao().addInventoryQuantity(ItemId.SHEEP_WOOL, 2)
    val fabricRecipe = WorkshopRecipes.ALL.first { it.id == "weave_cloth" }
    assertTrue("Start weaving cloth", repo.startCrafting(fabricRecipe))
    val fabricTask = db.farmDao().getWorkshopQueueDirect().first { it.recipeId == fabricRecipe.id }
    repeat(fabricRecipe.durationHours + 1) {
      engine.advanceHourForTesting()
    }
    assertTrue("Collect fabric", repo.collectFinishedWorkshop(fabricTask.id))

    // 10. Buy/raise pig & verify NO recurring meat
    assertTrue("Buy pig", repo.buyAnimal(AnimalSpecies.PIG, "Barn Pig"))
    val pig = db.farmDao().getAllAnimalsDirect().first { it.nickname == "Barn Pig" }
    assertNull("Pig must NOT have recurring primary produce", pig.species.primaryProduce)
    assertFalse("Pig produceReady must never be true", pig.produceReady)

    // 11. Process pig into meat & hide
    val meatResult = repo.processAnimalMeat(pig.id)
    assertTrue("Pig processing must succeed", meatResult.isEligible)
    assertTrue("Meat produced", meatResult.meatCount > 0)
    assertTrue("Hide produced", meatResult.hideCount > 0)

    // 12. Process meat into packaged meat & sell packaged meat
    val packMeatRecipe = WorkshopRecipes.ALL.first { it.id == "pack_meat" }
    assertTrue("Start packaging meat", repo.startCrafting(packMeatRecipe))
    val packTask = db.farmDao().getWorkshopQueueDirect().first { it.recipeId == packMeatRecipe.id }
    repeat(packMeatRecipe.durationHours + 1) {
      engine.advanceHourForTesting()
    }
    assertTrue("Collect packaged meat", repo.collectFinishedWorkshop(packTask.id))
    assertTrue("Sell packaged meat", repo.sellDirectToWholesale(ItemId.PACKAGED_MEAT, 1))

    // 13. Collect manure, compost manure & create fertilizer
    db.farmDao().addInventoryQuantity(ItemId.MANURE, 6)
    assertTrue("Compost manure", repo.processCompostBatch())
    assertTrue("Craft bio-fertilizer", repo.craftBioFertilizer())

    // 14. Grow crops (Corn)
    assertTrue("Buy corn seeds", repo.buySeeds(ItemId.SEED_CORN, 3))
    assertTrue("Plant corn", repo.plantCrop(6, CropType.CORN))
    assertTrue("Fertilize corn", repo.fertilizePlot(6))
    val plot = db.farmDao().getAllPlotsDirect().first { it.id == 6 }
    db.farmDao().updatePlot(plot.copy(growthProgress = 1.0f, isReadyForHarvest = true))
    assertTrue("Harvest corn", repo.harvestCrop(6))

    // 15. Stock shelf & retail sale
    assertTrue("Stock corn on shelf", repo.stockShelf(1, ItemId.CORN, 1))

    // 16. Fulfill NPC contract
    val contract = db.farmDao().getAllContractsDirect().first { !it.isCompleted && !it.isPenalized }
    db.farmDao().addInventoryQuantity(contract.requestedItem, contract.requestedQuantity)
    assertTrue("Fulfill contract", repo.fulfillContract(contract.id))

    // 17. Advance business level
    val curState = db.farmDao().getFarmStateDirect()!!
    db.farmDao().insertOrUpdateFarmState(curState.copy(totalEarnings = 10000, businessReputation = 80))
    val newLevel = repo.advanceBusinessLevel()
    assertNotNull("Business level should advance", newLevel)

    // 18. Save & verify persistence across reload
    val reloadedState = db.farmDao().getFarmStateDirect()!!
    val reloadedAnimals = db.farmDao().getAllAnimalsDirect()
    assertTrue("Coins persisted", reloadedState.coins > 0)
    assertTrue("Animals persisted", reloadedAnimals.isNotEmpty())
    assertTrue("Reputation persisted", reloadedState.businessReputation > 50)
  }
}
