package com.foodsnap.domain.usecase.inventory

import com.foodsnap.domain.model.UserIngredient
import com.foodsnap.domain.repository.UserInventoryRepository
import com.foodsnap.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the user's ingredient inventory.
 */
class GetUserInventoryUseCase @Inject constructor(
    private val repository: UserInventoryRepository
) {
    operator fun invoke(): Flow<Resource<List<UserIngredient>>> {
        return repository.getUserInventory()
    }
}

/**
 * Use case for adding an ingredient to the user's inventory.
 */
class AddIngredientToInventoryUseCase @Inject constructor(
    private val repository: UserInventoryRepository
) {
    suspend operator fun invoke(
        name: String,
        quantity: Double,
        unit: String,
        expirationDate: Long? = null,
        ingredientId: Long? = null
    ) {
        repository.addIngredient(name, quantity, unit, expirationDate, ingredientId)
    }
}

/**
 * Use case for removing an ingredient from the user's inventory.
 */
class RemoveIngredientFromInventoryUseCase @Inject constructor(
    private val repository: UserInventoryRepository
) {
    suspend operator fun invoke(ingredientId: Long) {
        repository.removeIngredient(ingredientId)
    }
}

/**
 * Use case for updating an ingredient's quantity in the inventory.
 */
class UpdateIngredientQuantityUseCase @Inject constructor(
    private val repository: UserInventoryRepository
) {
    suspend operator fun invoke(ingredientId: Long, newQuantity: Double) {
        repository.updateQuantity(ingredientId, newQuantity)
    }
}
