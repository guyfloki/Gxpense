package com.floki.gxpence

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.DataStoreException
import com.amplifyframework.datastore.generated.model.User
import com.floki.gxpence.expenses.ExpenseAddData
import com.floki.gxpence.expenses.ExpenseEdit
import com.floki.gxpence.expenses.ExpenseScreen
import com.floki.gxpence.expenses.ExpenseViewModel
import com.floki.gxpence.expenses.IncomeEdit
import com.floki.gxpence.ui.theme.GxpenceTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.collections.isEmpty
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            // Add AWS Amplify Plugins
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.addPlugin(AWSApiPlugin())

            // Configure Amplify
            Amplify.configure(applicationContext)

            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (e: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", e)
        }
    }
}

// MainActivity.kt
class MainActivity : AppCompatActivity() {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            GxpenceTheme{
                val navController = rememberNavController()
                var isSignedIn by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(true) }
                var username by remember { mutableStateOf<String?>(null) }
                val chatViewModel: ChatViewModel = viewModel()
                var isNetworkAvailable by remember { mutableStateOf(isNetworkAvailable()) }

                LaunchedEffect(key1 = "authCheck") {
                    try {
                        val result = fetchAuthSession()
                        isSignedIn = result.isSignedIn
                        isLoading = false
                        if (result.isSignedIn) {
                            startDataStore()
                        }
                        Log.i("AmplifyQuickstart", "User is signed in: ${result.isSignedIn}")
                    } catch (error: Throwable) {
                        isLoading = false
                        Log.e("AmplifyQuickstart", "Could not get auth session.", error)
                    }
                }

                LaunchedEffect(key1 = isSignedIn) {
                    if (isSignedIn) {
                        try {
                            val authUser = getCurrentUser()
                            username = authUser.username
                            isLoading = false
                        } catch (error: Throwable) {
                            Log.e("AuthQuickStart", "Error getting the current user", error)
                        }
                    }
                }



                when {
                    !isNetworkAvailable -> {
                        // Display the "No Internet Connection" screen
                        NoInternetScreen(
                            retry = { isNetworkAvailable = isNetworkAvailable() }
                        )
                    }
                    isLoading -> {
                        Log.i("MainActivity", "Loading...")
                        Text(text = "Loading...")
                    }
                    isSignedIn -> {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Main.route
                        ) {
                            composable(route = Screen.Main.route) {
                                username?.let { user ->
                                    val viewModelFactory = ExpenseViewModelFactory(user)
                                    val viewModel: ExpenseViewModel =
                                        viewModel(factory = viewModelFactory)
                                    MainScreen(
                                        navigateToExpenseScreen = { navController.navigate(Screen.Expense.route) },
                                        navigateToSettingsScreen = { navController.navigate(Screen.Settings.route) }, // Pass this to MainScreen
                                        chatViewModel = chatViewModel,
                                        username = user

                                    )




                                }
                            }


                            composable(route = Screen.Expense.route) {
                                username?.let { user ->
                                    val viewModelFactory = ExpenseViewModelFactory(user)
                                    val viewModel: ExpenseViewModel =
                                        viewModel(factory = viewModelFactory)
                                    ExpenseScreen(
                                        username = user,
                                        navigateBack = { navController.popBackStack() },
                                        navigateToMainScreen = { navController.navigate(Screen.Main.route) },
                                        navigateToAddExpense = { navController.navigate(Screen.ExpenseAddData.route) },
                                        navigateToSettingsScreen = { navController.navigate(Screen.Settings.route) }, // Pass this to MainScreen
                                        navigateToExpenseEdit = { expenseId ->
                                            navController.navigate(
                                                Screen.ExpenseEdit.createRoute(
                                                    expenseId
                                                )
                                            )
                                        },
                                        navigateToIncomeEdit = { incomeId ->
                                            navController.navigate(
                                                Screen.IncomeEdit.createRoute(
                                                    incomeId
                                                )
                                            )
                                        },
                                        chatViewModel = chatViewModel,
                                    )



                                }
                            }



                            composable(route = Screen.ExpenseAddData.route) {
                                username?.let { user ->
                                    val viewModelFactory = ExpenseViewModelFactory(user)
                                    val viewModel: ExpenseViewModel =
                                        viewModel(factory = viewModelFactory)
                                    ExpenseAddData(
                                        username = user,
                                        navigateToExpenseScreen = { navController.navigate(Screen.Expense.route) },
                                    )


                                }
                            }

                            composable(route = Screen.Settings.route) {
                                username?.let { user ->
                                    val viewModelFactory = ExpenseViewModelFactory(user)
                                    val viewModel: ExpenseViewModel =
                                        viewModel(factory = viewModelFactory)
                                    val isSignedInState = remember { mutableStateOf(isSignedIn) }
                                    SettingsScreen(
                                        username = user,
                                        isSignedIn = isSignedInState,
                                        navigateToLoginScreen = { isSignedIn = false },
                                        navController = navController,
                                    )
                                }
                            }

                            composable(route = Screen.ExpenseEdit.route) { backStackEntry ->
                                username?.let { user ->
                                    val viewModelFactory = ExpenseViewModelFactory(user)
                                    val viewModel: ExpenseViewModel =
                                        viewModel(factory = viewModelFactory)
                                    val expenseId = backStackEntry.arguments?.getString("id")
                                    if (expenseId != null) {
                                        val expense = viewModel.getExpenseById(expenseId)
                                        if (expense != null) {
                                            ExpenseEdit(
                                                username = user,
                                                expense = expense,
                                                onExpenseUpdate = { updatedExpense ->
                                                    viewModel.updateExpense(
                                                        updatedExpense.id,
                                                        updatedExpense.amount.toString(),
                                                        updatedExpense.category.name,
                                                        updatedExpense.notes
                                                    )
                                                },
                                                onExpenseDelete = { expenseId ->
                                                    viewModel.deleteExpense(expense.id)
                                                },
                                                navigateToExpenseScreen = { navController.navigate(Screen.Expense.route) },
                                            )
                                        } else {
                                            // Handle case where expense is null, e.g., navigate back or show a message to the user
                                        }
                                    } else {
                                        // Handle case where expenseId is null, e.g., navigate back or show a message to the user
                                    }


                                }
                            }
                            composable(route = Screen.IncomeEdit.route) { backStackEntry ->
                                username?.let { user ->
                                    val viewModelFactory = ExpenseViewModelFactory(user)
                                    val viewModel: ExpenseViewModel =
                                        viewModel(factory = viewModelFactory)
                                    val incomeId = backStackEntry.arguments?.getString("id")
                                    if (incomeId != null) {
                                        val income = viewModel.getIncomeById(incomeId)
                                        if (income != null) {
                                            IncomeEdit(
                                                username = user,
                                                income = income,
                                                onIncomeUpdate = { updatedIncome ->
                                                    viewModel.updateIncome(
                                                        updatedIncome.id,
                                                        updatedIncome.amount.toString(),
                                                        updatedIncome.category.name,
                                                        updatedIncome.notes
                                                    )
                                                },
                                                onIncomeDelete = { incomeId ->
                                                    viewModel.deleteIncome(income.id)
                                                },
                                                navigateToExpenseScreen = { navController.navigate(Screen.Expense.route) },
                                            )
                                        } else {
                                            // Handle case where income is null, e.g., navigate back or show a message to the user
                                        }
                                    } else {
                                        // Handle case where incomeId is null, e.g., navigate back or show a message to the user
                                    }

                                }
                            }
                        }
                    }
                    else -> LoginScreen(
                        setupGoogleSignIn = { setupGoogleSignIn { isSignedIn = true} },
                        isSignedIn = isSignedIn,
                        navigateToMainScreen = {navController.navigate(Screen.Main.route) },
                    )

                }
            }
        }
    }

    sealed class Screen(val route: String) {
        object Main : Screen("main")
        object Expense : Screen("expense")
        object ExpenseAddData : Screen("expense_add_data")
        object Settings : Screen("settings")
        object ExpenseEdit : Screen("expense_edit/{id}") {
            fun createRoute(id: String) = "expense_edit/$id"
        }
        object IncomeEdit : Screen("income_edit/{id}") {
            fun createRoute(id: String) = "income_edit/$id"
        }

    }


    suspend fun callLambda(user_message: String, session_id: String, username: String) = suspendCancellableCoroutine<String> { continuation ->
        val data = JSONObject()
        data.put("user_message", user_message)
        data.put("session_id", session_id)
        data.put("username", username)
        val options = RestOptions.builder()
            .addPath("/gptchat")
            .addBody(data.toString().toByteArray())
            .build()
        Amplify.API.post(
            options,
            { response ->
                val responseString = response.data.asString()
                Log.i("MainActivity", "Lambda response: $responseString")
                if (continuation.isActive) {
                    continuation.resume(responseString)
                }
            },
            { error ->
                Log.e("MainActivity", "Error calling Lambda function", error)
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
        )
    }





    suspend fun fetchAuthSession(): AuthSession = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.fetchAuthSession(
            { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            },
            { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
        )
    }

    suspend fun getCurrentUser(): AuthUser = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.getCurrentUser(
            { authUser ->
                if (continuation.isActive) {
                    continuation.resume(authUser)
                }
            },
            { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
        )
    }

    suspend fun startDataStore() = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            Amplify.DataStore.start(
                {
                    Log.i("MyAmplifyApp", "DataStore started")
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                },
                { error ->
                    Log.e("MyAmplifyApp", "Error starting DataStore", error)
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                }
            )
        } catch (error: DataStoreException) {
            Log.e("MyAmplifyApp", "Error starting DataStore", error)
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


    private fun setupGoogleSignIn(onSuccess: (String) -> Unit) {
        lifecycleScope.launch {
            clearDataStore()
            Amplify.Auth.signInWithSocialWebUI(
                AuthProvider.google(),
                this@MainActivity,
                { result ->
                    Log.i("AuthQuickStart", "Sign in succeeded: ${result.nextStep}")
                    Amplify.Auth.getCurrentUser(
                        { authUser ->
                            // call your checkAndCreateUser function here
                            checkAndCreateUser(authUser.username)
                            onSuccess(authUser.username)
                        },
                        { error -> Log.e("Error", "Error getting the current user", error) }
                    )
                },
                { error -> Log.e("AuthQuickStart", "Sign in failed", error) }
            )
        }
    }



    private fun checkAndCreateUser(username: String) {
        Amplify.API.query(
            ModelQuery.get(User::class.java, username),
            { response ->
                if (response.data == null) {
                    // User does not exist, create it
                    createUser(username)
                } else {
                    Log.i("AmplifyQuickstart", "User already exists: ${response.data}")
                }
            },
            { error -> Log.e("AmplifyQuickstart", "Error querying for user", error) }
        )
    }


    private fun createUser(username: String) {
        val user = User.builder()
            .amount(10.0)
            .id(username)
            .build()

        Amplify.API.mutate(
            ModelMutation.create(user),
            { response -> Log.i("AmplifyQuickstart", "User added: ${response.data}") },
            { error -> Log.e("AmplifyQuickstart", "Error adding user", error) }
        )
    }


    class ExpenseViewModelFactory(private val username: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel(username) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
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
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    private fun signOut(onComplete: () -> Unit) {
        Amplify.Auth.signOut(
            AuthSignOutOptions.builder()
                .globalSignOut(true)
                .build()
        ) { result ->
            when (result) {
                is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                    Log.i("AuthQuickStart", "Signed out successfully")
                    lifecycleScope.launch {
                        stopDataStore()
                        clearDataStore()
                        onComplete()
                    }
                }
                is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                    lifecycleScope.launch {
                        stopDataStore()
                        clearDataStore()
                        onComplete()
                    }
                }
                is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                    // handle failed sign out
                }
            }
        }
    }


}


@Composable
fun NoInternetScreen(retry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No Internet Connection",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            modifier = Modifier.padding(8.dp)
        )
        Button(
            onClick = { retry() },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "Retry")
        }
    }
}


