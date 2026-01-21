package com.foodsnap.data.repository

import com.foodsnap.data.local.database.dao.UserIngredientDao
import com.foodsnap.data.local.database.entity.UserIngredientEntity
import com.foodsnap.di.IoDispatcher
import com.foodsnap.domain.model.UserIngredient
import com.foodsnap.domain.repository.UserInventoryRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Implementation of UserInventoryRepository.
 *
 * Manages user's pantry/inventory in the local database.
 */
class UserInventoryRepositoryImpl @Inject constructor(
    private val userIngredientDao: UserIngredientDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserInventoryRepository {

    override fun getUserInventory(): Flow<Resource<List<UserIngredient>>> = flow {
        emit(Resource.Loading())

        try {
            userIngredientDao.getAllUserIngredients()
                .collect { entities ->
                    val ingredients = entities.map { it.toDomain() }
                    emit(Resource.Success(ingredients))
                }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load inventory"))
        }
    }.flowOn(ioDispatcher)

    override suspend fun addIngredient(
        name: String,
        quantity: Double,
        unit: String,
        expirationDate: Long?,
        ingredientId: Long?
    ) {
        val entity = UserIngredientEntity(
            ingredientId = ingredientId,
            name = name,
            quantity = quantity,
            unit = unit,
            expirationDate = expirationDate,
            addedAt = System.currentTimeMillis()
        )
        userIngredientDao.insertUserIngredient(entity)
    }

    override suspend fun removeIngredient(ingredientId: Long) {
        userIngredientDao.deleteUserIngredient(ingredientId)
    }

    override suspend fun updateQuantity(ingredientId: Long, newQuantity: Double) {
        userIngredientDao.updateQuantity(ingredientId, newQuantity)
    }

    override fun getExpiringIngredients(withinDays: Int): Flow<List<UserIngredient>> {
        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + TimeUnit.DAYS.toMillis(withinDays.toLong())

        return userIngredientDao.getExpiringIngredients(currentTime, futureTime)
            .map { entities -> entities.map { it.toDomain() } }
    }

    /**
     * Extension function to convert entity to domain model.
     */
    private fun UserIngredientEntity.toDomain(): UserIngredient {
        return UserIngredient(
            id = this.id,
            ingredientId = this.ingredientId,
            name = this.name,
            quantity = this.quantity,
            unit = this.unit,
            expirationDate = this.expirationDate,
            addedAt = this.addedAt,
            location = this.location
        )
    }
}
