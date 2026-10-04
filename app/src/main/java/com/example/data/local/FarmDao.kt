package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ItemId
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {

    // Farm State
    @Query("SELECT * FROM farm_state WHERE id = 1 LIMIT 1")
    fun getFarmState(): Flow<FarmStateEntity?>

    @Query("SELECT * FROM farm_state WHERE id = 1 LIMIT 1")
    suspend fun getFarmStateDirect(): FarmStateEntity?

    @Query("UPDATE farm_state SET playerX = :x, playerY = :y, playerZ = :z, playerYaw = :yaw WHERE id = 1")
    suspend fun updatePlayerPosition(x: Float, y: Float, z: Float, yaw: Float)

    @Query("UPDATE farm_state SET playerX = :x, playerY = :y, playerZ = :z, playerYaw = :yaw, cameraYaw = :camYaw, cameraPitch = :camPitch, cameraDistance = :camDist WHERE id = 1")
    suspend fun updatePlayerAndCameraState(x: Float, y: Float, z: Float, yaw: Float, camYaw: Float, camPitch: Float, camDist: Float)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFarmState(state: FarmStateEntity)

    // Animals
    @Query("SELECT * FROM animals ORDER BY id ASC")
    fun getAllAnimals(): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals ORDER BY id ASC")
    suspend fun getAllAnimalsDirect(): List<AnimalEntity>

    @Query("SELECT * FROM animals WHERE id = :id LIMIT 1")
    suspend fun getAnimalById(id: Long): AnimalEntity?

    @Query("UPDATE animals SET worldX = :x, worldY = :y, worldZ = :z, worldYaw = :yaw WHERE id = :id")
    suspend fun updateAnimalPosition(id: Long, x: Float, y: Float, z: Float, yaw: Float)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity): Long

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Update
    suspend fun updateAnimals(animals: List<AnimalEntity>)

    @Delete
    suspend fun deleteAnimal(animal: AnimalEntity)

    // Crop Plots
    @Query("SELECT * FROM crop_plots ORDER BY id ASC")
    fun getAllPlots(): Flow<List<CropPlotEntity>>

    @Query("SELECT * FROM crop_plots ORDER BY id ASC")
    suspend fun getAllPlotsDirect(): List<CropPlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlots(plots: List<CropPlotEntity>)

    @Update
    suspend fun updatePlot(plot: CropPlotEntity)

    @Update
    suspend fun updatePlots(plots: List<CropPlotEntity>)

    // Inventory
    @Query("SELECT * FROM inventory WHERE quantity > 0 ORDER BY quantity DESC")
    fun getInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE itemId = :itemId LIMIT 1")
    suspend fun getInventoryItem(itemId: ItemId): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setInventoryItem(item: InventoryEntity)

    suspend fun addInventoryQuantity(itemId: ItemId, delta: Int) {
        val existing = getInventoryItem(itemId)
        val current = existing?.quantity ?: 0
        setInventoryItem(InventoryEntity(itemId = itemId, quantity = (current + delta).coerceAtLeast(0)))
    }

    // Workshop Queue
    @Query("SELECT * FROM workshop_queue ORDER BY id ASC")
    fun getWorkshopQueue(): Flow<List<WorkshopQueueEntity>>

    @Query("SELECT * FROM workshop_queue ORDER BY id ASC")
    suspend fun getWorkshopQueueDirect(): List<WorkshopQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkshopTask(task: WorkshopQueueEntity): Long

    @Update
    suspend fun updateWorkshopTask(task: WorkshopQueueEntity)

    @Delete
    suspend fun deleteWorkshopTask(task: WorkshopQueueEntity)

    // Shop Shelves
    @Query("SELECT * FROM shop_shelves ORDER BY shelfId ASC")
    fun getAllShelves(): Flow<List<ShopShelfEntity>>

    @Query("SELECT * FROM shop_shelves ORDER BY shelfId ASC")
    suspend fun getAllShelvesDirect(): List<ShopShelfEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelves(shelves: List<ShopShelfEntity>)

    @Update
    suspend fun updateShelf(shelf: ShopShelfEntity)

    // Contracts
    @Query("SELECT * FROM npc_contracts ORDER BY isCompleted ASC, expiryDay ASC")
    fun getAllContracts(): Flow<List<ContractEntity>>

    @Query("SELECT * FROM npc_contracts ORDER BY isCompleted ASC, expiryDay ASC")
    suspend fun getAllContractsDirect(): List<ContractEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContracts(contracts: List<ContractEntity>)

    @Update
    suspend fun updateContract(contract: ContractEntity)

    @Query("DELETE FROM npc_contracts WHERE id = :id")
    suspend fun deleteContract(id: String)

    // Market Quotes
    @Query("SELECT * FROM market_quotes")
    fun getAllMarketQuotes(): Flow<List<MarketQuoteEntity>>

    @Query("SELECT * FROM market_quotes")
    suspend fun getAllMarketQuotesDirect(): List<MarketQuoteEntity>

    @Query("SELECT * FROM market_quotes WHERE itemId = :itemId LIMIT 1")
    suspend fun getMarketQuoteDirect(itemId: ItemId): MarketQuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMarketQuotes(quotes: List<MarketQuoteEntity>)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY id DESC LIMIT 40")
    fun getRecentLogs(): Flow<List<LogMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogMessageEntity)
}
