package com.floki.gxpence.expenses


import android.util.Log
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.smithy.kotlin.runtime.time.Instant
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.datastore.DataStoreException
import com.amplifyframework.datastore.generated.model.Expense
import com.amplifyframework.datastore.generated.model.ExpenseCategory
import com.amplifyframework.datastore.generated.model.Income
import com.amplifyframework.datastore.generated.model.IncomeCategory
import com.amplifyframework.datastore.generated.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ExpenseViewModel(
    private val username: String,
) : ViewModel() {


    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _income = MutableStateFlow<List<Income>>(emptyList())
    val income: StateFlow<List<Income>> = _income

    init {
        observeExpenses()
        observeIncome()
    }




    fun getExpenseById(id: String): Expense? {
        return _expenses.value.firstOrNull { it.id == id }
    }

    fun getIncomeById(id: String): Income? {
        return _income.value.firstOrNull { it.id == id }
    }
    suspend fun stopDataStore() = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            Amplify.DataStore.stop(
                {
                    Log.i("MyAmplifyApp", "DataStore stopped")
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                },
                { error ->
                    Log.e("MyAmplifyApp", "Error stopping DataStore", error)
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                }
            )
        } catch (error: DataStoreException) {
            Log.e("MyAmplifyApp", "Error stopping DataStore", error)
            if (continuation.isActive) {
                continuation.resumeWithException(error)
            }
        }
    }



    suspend fun clearDataStore() = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            Amplify.DataStore.clear(
                {
                    Log.i("MyAmplifyApp", "DataStore cleared")
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                },
                { error ->
                    Log.e("MyAmplifyApp", "Error clearing DataStore", error)
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                }
            )
        } catch (error: DataStoreException) {
            Log.e("MyAmplifyApp", "Error clearing DataStore", error)
            if (continuation.isActive) {
                continuation.resumeWithException(error)
            }
        }
    }
    fun clearData() {
        _expenses.value = emptyList()
        _income.value = emptyList()
    }

    fun signOut(onComplete: () -> Unit) {
        Amplify.Auth.signOut(
            AuthSignOutOptions.builder()
                .globalSignOut(true)
                .build()
        ) { result ->
            when (result) {
                is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                    Log.i("AuthQuickStart", "Signed out successfully")
                    viewModelScope.launch {
                        stopDataStore()
                        clearDataStore()
                        clearData()
                        onComplete()
                    }
                }
                is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                    viewModelScope.launch {
                        stopDataStore()
                        clearDataStore()
                        clearData()
                        onComplete()
                    }
                }
                is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                    // handle failed sign out
                }
            }
        }
    }



    fun deleteExpense(id: String) {
        // Get the existing item
        val existingExpense = _expenses.value.firstOrNull { it.id == id }

        if (existingExpense != null) {
            // Delete the item
            Amplify.DataStore.delete(
                existingExpense,
                {
                    Log.i("AmplifyQuickstart", "Expense deleted: $id")

                },
                {
                    error: DataStoreException -> Log.e("AmplifyQuickstart", "Error deleting expense", error)

                }
            )
        }
    }

    fun deleteIncome(id: String) {

        // Get the existing item
        val existingIncome = _income.value.firstOrNull { it.id == id }

        if (existingIncome != null) {
            // Delete the item
            Amplify.DataStore.delete(
                existingIncome,
                {
                    Log.i("AmplifyQuickstart", "Income deleted: $id")

                },
                {
                    error: DataStoreException -> Log.e("AmplifyQuickstart", "Error deleting income", error)

                }
            )
        }
    }

    fun addExpense(amount: String, category: String, notes: String, date: LocalDate? = null, time: LocalTime? = null) {
        val dateTime = if (date != null && time != null) {
            val localDateTime = LocalDateTime.of(date, time)
            val zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"))
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(zonedDateTime)
        } else {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(ZonedDateTime.now(ZoneId.of("UTC")))
        }



        val expense = Expense.builder()
            .amount(amount.toDouble())
            .category(ExpenseCategory.valueOf(category))
            .createdAt(dateTime)
            .notes(notes)
            .build()

        Amplify.DataStore.save(
            expense,
            {
                Log.i("AmplifyQuickstart", "Expense added: $expense")

            },
            {
                error: DataStoreException -> Log.e("AmplifyQuickstart", "Error adding expense", error)

            }
        )
    }

    fun addIncome(amount: String, category: String, notes: String, date: LocalDate? = null, time: LocalTime? = null) {
        val dateTime = if (date != null && time != null) {
            val localDateTime = LocalDateTime.of(date, time)
            val zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"))
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(zonedDateTime)
        } else {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(ZonedDateTime.now(ZoneId.of("UTC")))
        }


        val income = Income.builder()
            .amount(amount.toDouble())
            .category(IncomeCategory.valueOf(category))
            .createdAt(dateTime)
            .notes(notes)
            .build()

        Amplify.DataStore.save(
            income,
            {
                Log.i("AmplifyQuickstart", "Income added: $income")

            },
            {
                error: DataStoreException -> Log.e("AmplifyQuickstart", "Error adding income", error)

            }
        )
    }


    fun updateExpense(id: String, amount: String, category: String, notes: String) {

        // Get the existing item
        val existingExpense = _expenses.value.firstOrNull { it.id == id }

        if (existingExpense != null) {
            // Create a copy of the existing item with the updated values
            val updatedExpense = existingExpense.copyOfBuilder()
                .amount(amount.toDouble())
                .category(ExpenseCategory.valueOf(category))
                .notes(notes)
                .build()

            // Save the updated item
            Amplify.DataStore.save(
                updatedExpense,
                {
                    Log.i("AmplifyQuickstart", "Expense updated: $updatedExpense")

                },
                {
                    error: DataStoreException -> Log.e("AmplifyQuickstart", "Error updating expense", error)

                }
            )
        }
    }



    fun updateIncome(id: String, amount: String, category: String, notes: String) {

        // Get the existing item
        val existingIncome = _income.value.firstOrNull { it.id == id }

        if (existingIncome != null) {
            // Create a copy of the existing item with the updated values
            val updatedExpense = existingIncome.copyOfBuilder()
                .amount(amount.toDouble())
                .category(IncomeCategory.valueOf(category))
                .notes(notes)
                .build()

            // Save the updated item
            Amplify.DataStore.save(
                updatedExpense,
                {
                    Log.i("AmplifyQuickstart", "Expense updated: $updatedExpense")

                },
                {
                    error: DataStoreException -> Log.e("AmplifyQuickstart", "Error updating expense", error)

                }
            )
        }
    }


    fun observeExpenses() {
        val options = ObserveQueryOptions()
            .matches(Expense.OWNER.eq(username))

        Amplify.DataStore.observeQuery(
            Expense::class.java,
            options,
            { Log.i("MyAmplifyApp", "Observation began.") },
            { snapshot ->
                val expenses = snapshot.items.toList()
                _expenses.value = expenses
            },
            { failure -> Log.e("MyAmplifyApp", "Observation failed.", failure) },
            { Log.i("MyAmplifyApp", "Observation complete.") }
        )
    }

    fun observeIncome() {
        val options = ObserveQueryOptions()
            .matches(Income.OWNER.eq(username))

        Amplify.DataStore.observeQuery(
            Income::class.java,
            options,
            { Log.i("MyAmplifyApp", "Observation began.") },
            { snapshot ->
                val incomes = snapshot.items.toList()
                _income.value = incomes
            },
            { failure -> Log.e("MyAmplifyApp", "Observation failed.", failure) },
            { Log.i("MyAmplifyApp", "Observation complete.") }
        )
    }

    override fun onCleared() {
        super.onCleared()
        clearData()
        scope.cancel()
    }
}

