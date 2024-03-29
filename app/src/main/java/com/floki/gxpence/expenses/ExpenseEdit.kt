package com.floki.gxpence.expenses


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amplifyframework.datastore.generated.model.Expense
import com.amplifyframework.datastore.generated.model.ExpenseCategory
import com.floki.gxpence.MainActivity
import com.floki.gxpence.ui.theme.DeleteButtonColor
import com.floki.gxpence.ui.theme.GradientEnd
import com.floki.gxpence.ui.theme.GradientStart
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEdit(
    username: String,
    expense: Expense,
    onExpenseUpdate: (Expense) -> Unit,
    onExpenseDelete: (String) -> Unit,
    navigateToExpenseScreen: () -> Unit,
) {
    val viewModel: ExpenseViewModel =
        viewModel(factory = MainActivity.ExpenseViewModelFactory(username))
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var category by remember { mutableStateOf(expense.category.name) }
    var notes by remember { mutableStateOf(expense.notes ?: "") }
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DropdownMenuCategories(true, category) { selectedCategory ->
                    category = selectedCategory
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 25.dp)) {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        onExpenseDelete(expense.id)
                        navigateToExpenseScreen()
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(56.dp)
                        .background(DeleteButtonColor, shape = CircleShape)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        if (amount.isBlank() || category.isBlank() ) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill in all the fields.")
                            }
                        } else {
                            onExpenseUpdate(
                                expense.copyOfBuilder()
                                    .amount(amount.toDouble())
                                    .category(ExpenseCategory.valueOf(category))
                                    .notes(notes)
                                    .build()
                            )
                            navigateToExpenseScreen()
                        }
                    },
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Update", tint = Color.White)
                }
            }
        }
    }
}