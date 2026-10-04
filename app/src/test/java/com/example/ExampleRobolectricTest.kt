package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AnimalEntity
import com.example.data.local.AppDatabase
import com.example.data.model.AnimalSpecies
import com.example.data.model.ItemId
import com.example.data.model.PricingStrategy
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Solarpunk Farm", appName)
  }

  @Test
  fun `test zero waste composting loop and database initialization`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = FarmRepository(db.farmDao())
    repo.checkAndInitializeDefaults()

    val state = db.farmDao().getFarmStateDirect()
    assertNotNull(state)
    assertTrue(state!!.coins > 0)

    // Test zero waste composting loop: 3 Manure -> 2 Compost + 1 Biogas Canister
    val manureBefore = db.farmDao().getInventoryItem(ItemId.MANURE)?.quantity ?: 0
    val compostBefore = db.farmDao().getInventoryItem(ItemId.COMPOST)?.quantity ?: 0
    assertTrue("Should have initial manure", manureBefore >= 3)

    val success = repo.processCompostBatch()
    assertTrue(success)

    val manureAfter = db.farmDao().getInventoryItem(ItemId.MANURE)?.quantity ?: 0
    val compostAfter = db.farmDao().getInventoryItem(ItemId.COMPOST)?.quantity ?: 0
    assertEquals(manureBefore - 3, manureAfter)
    assertEquals(compostBefore + 2, compostAfter)
  }

  @Test
  fun `test livestock adoption, valuation, and produce collection`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = FarmRepository(db.farmDao())
    repo.checkAndInitializeDefaults()

    // Adopt a Dairy Cow
    val bought = repo.buyAnimal(AnimalSpecies.COW, "Buttercup")
    assertTrue("Should be able to buy cow with initial funds", bought)

    val animals = db.farmDao().getAllAnimalsDirect()
    val cow = animals.firstOrNull { it.nickname == "Buttercup" }
    assertNotNull(cow)
    assertEquals(AnimalSpecies.COW, cow!!.species)

    // Pet animal improves happiness
    val initialHappiness = cow.happiness
    repo.petAnimal(cow.id)
    val pettedCow = db.farmDao().getAnimalById(cow.id)
    assertNotNull(pettedCow)
    assertTrue(pettedCow!!.happiness >= initialHappiness)

    // Valuation test: Selling returns positive coins
    val sellValue = repo.sellAnimal(cow.id)
    assertNotNull(sellValue)
    assertTrue("Sale value should be positive", sellValue!! > 0)
  }

  @Test
  fun `test bees produce honey and wax and are not treated as meat livestock`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = FarmRepository(db.farmDao())
    repo.checkAndInitializeDefaults()

    // Adopt Bees
    val bought = repo.buyAnimal(AnimalSpecies.BEES, "Sunny Hive")
    assertTrue(bought)

    val animals = db.farmDao().getAllAnimalsDirect()
    val bees = animals.first { it.species == AnimalSpecies.BEES }

    // Verify Bees produce honey
    assertEquals(ItemId.HONEY, bees.species.primaryProduce)

    // Crucial rule: Bees must NOT be treated as meat livestock
    val meatResult = repo.processAnimalMeat(bees.id)
    assertFalse("Bees must not be eligible for meat processing", meatResult.isEligible)
    assertEquals(0, meatResult.meatCount)
    assertEquals(0, meatResult.hideCount)
    assertNotNull(meatResult.rejectionReason)
  }

  @Test
  fun `test cow meat and leather processing`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = FarmRepository(db.farmDao())
    repo.checkAndInitializeDefaults()

    // Add mature Cow
    val cowId = db.farmDao().insertAnimal(
      AnimalEntity(
        species = AnimalSpecies.COW,
        nickname = "Bessie",
        ageDays = 15, // Mature
        health = 0.95f,
        happiness = 0.9f
      )
    )

    val meatResult = repo.processAnimalMeat(cowId)
    assertTrue(meatResult.isEligible)
    assertTrue("Cow should produce at least 4 meat", meatResult.meatCount >= 4)
    assertTrue("Cow should produce leather hide", meatResult.hideCount >= 2)

    val meatInInv = db.farmDao().getInventoryItem(ItemId.MEAT)?.quantity ?: 0
    val hideInInv = db.farmDao().getInventoryItem(ItemId.LEATHER)?.quantity ?: 0
    assertTrue(meatInInv >= meatResult.meatCount)
    assertTrue(hideInInv >= meatResult.hideCount)
  }

  @Test
  fun `test retail store pricing and inventory fulfillment`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = FarmRepository(db.farmDao())
    repo.checkAndInitializeDefaults()

    // Ensure we have some items in inventory
    db.farmDao().addInventoryQuantity(ItemId.EGGS, 10)
    val shelf = db.farmDao().getAllShelvesDirect().first()

    // Stock shelf
    val stocked = repo.stockShelf(shelf.shelfId, ItemId.EGGS, 5)
    assertTrue(stocked)

    // Update pricing strategy to PREMIUM_ORGANIC
    repo.updateShelfPricing(shelf.shelfId, PricingStrategy.PREMIUM_ORGANIC)
    val updatedShelf = db.farmDao().getAllShelvesDirect().first { it.shelfId == shelf.shelfId }
    assertEquals(PricingStrategy.PREMIUM_ORGANIC, updatedShelf.pricingStrategy)
    val calculatedPrice = (ItemId.EGGS.basePrice * updatedShelf.pricingStrategy.priceMultiplier).toInt()
    assertTrue(calculatedPrice > ItemId.EGGS.basePrice)
  }
}
