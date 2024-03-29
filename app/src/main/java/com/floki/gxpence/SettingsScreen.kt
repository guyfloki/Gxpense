package com.floki.gxpence


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.floki.gxpence.expenses.ExpenseViewModel

@Composable
fun SettingsScreen(
    navigateToLoginScreen: () -> Unit,
    username: String,
    isSignedIn: MutableState<Boolean>,
    navController: NavController,
) {

    val viewModel: ExpenseViewModel =
        viewModel(factory = MainActivity.ExpenseViewModelFactory(username))
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            viewModel.signOut { isSignedIn.value = false }
            navigateToLoginScreen()
            navController.popBackStack(navController.graph.startDestinationId, false)
        })
        {
            Text("Logout")
        }
    }
}

