package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
  fun `test zero waste composting loop and database initialization`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.FarmRepository(db.farmDao())
    repo.checkAndInitializeDefaults()

    val state = db.farmDao().getFarmStateDirect()
    org.junit.Assert.assertNotNull(state)
    org.junit.Assert.assertTrue(state!!.coins > 0)

    // Test zero waste composting loop: 3 Manure -> 2 Compost + 1 Biogas Canister
    val manureBefore = db.farmDao().getInventoryItem(com.example.data.model.ItemId.MANURE)?.quantity ?: 0
    val compostBefore = db.farmDao().getInventoryItem(com.example.data.model.ItemId.COMPOST)?.quantity ?: 0
    org.junit.Assert.assertTrue("Should have initial manure", manureBefore >= 3)

    val success = repo.processCompostBatch()
    org.junit.Assert.assertTrue(success)

    val manureAfter = db.farmDao().getInventoryItem(com.example.data.model.ItemId.MANURE)?.quantity ?: 0
    val compostAfter = db.farmDao().getInventoryItem(com.example.data.model.ItemId.COMPOST)?.quantity ?: 0
    assertEquals(manureBefore - 3, manureAfter)
    assertEquals(compostBefore + 2, compostAfter)
  }
}
