package com.floki.gxpence.expenses



import android.annotation.SuppressLint
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amplifyframework.datastore.generated.model.ExpenseCategory
import com.amplifyframework.datastore.generated.model.IncomeCategory
import com.floki.gxpence.MainActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.*

@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ExpenseAddData(
    navigateToExpenseScreen: () -> Unit,
    username: String
) {



    val keyboardController = LocalSoftwareKeyboardController.current
    var isExpense by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()





    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFEF5350), Color(0xFFFF7043)),
        start = Offset.Zero,
        end = Offset.Infinite
    )
    val darkTheme = isSystemInDarkTheme()
    val viewModel: ExpenseViewModel =
        viewModel(factory = MainActivity.ExpenseViewModelFactory(username))

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ToggleButton(isExpense) {
                isExpense = !isExpense
                category = ""

            }
            Spacer(modifier = Modifier.height(16.dp))

            DropdownMenuCategories(isExpense, category) { selectedCategory ->
                category = selectedCategory
            }

            Button(onClick = { showDatePicker = true }) {
                Text(if (selectedDate != null) selectedDate.toString() else "Select Date")
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                val confirmEnabled = derivedStateOf { datePickerState.selectedDateMillis != null }
                DatePickerDialog(
                    onDismissRequest = {
                        showDatePicker = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                                selectedDate = LocalDate.ofEpochDay(
                                    datePickerState.selectedDateMillis!!.div(86400000L)
                                ) // update selected date
                            },
                            enabled = confirmEnabled.value
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            var selectedTime by remember { mutableStateOf(LocalTime.of(12, 0)) }
            var showTimePicker by remember { mutableStateOf(false) }
            var tempTime by remember { mutableStateOf(LocalTime.of(12, 0)) }

            Button(onClick = {
                tempTime = selectedTime
                showTimePicker = true
            }) {
                Text(
                    if (selectedDate != null) "${
                        selectedTime.format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        )
                    }" else "Select Date and Time"
                )
            }

            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = tempTime.hour,
                    initialMinute = tempTime.minute
                )

                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text(text = "Select Time") },
                    text = {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier,
                            colors = TimePickerDefaults.colors()
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            selectedTime =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }






            OutlinedTextField(
                value = amount,
                onValueChange = { newAmount -> amount = newAmount },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { /* handle action */ }),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))



            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { newNotes -> notes = newNotes },
                label = { Text("Notes") },
                singleLine = false,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush = gradientBrush)
            ) {
                Button(
                    onClick = {
                        if (amount.isBlank() || category.isBlank() ) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill in all the fields.")
                            }
                        } else {
                            if (isExpense) {
                                viewModel.addExpense(amount, category, notes, selectedDate, selectedTime)
                            } else {
                                viewModel.addIncome(amount, category, notes, selectedDate, selectedTime)
                            }
                            keyboardController?.hide()
                            navigateToExpenseScreen()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Save", style = MaterialTheme.typography.bodyLarge)
                }


            }
        }

        IconButton(
            onClick = { navigateToExpenseScreen() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
        }




        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}





val ExpenseCategoryIcons = mapOf(
    ExpenseCategory.FOOD to Pair(Icons.Filled.Fastfood, Color(android.graphics.Color.parseColor("#FFD700"))),
    ExpenseCategory.TRANSPORTATION to Pair(Icons.Filled.DirectionsCar, Color(android.graphics.Color.parseColor("#0000FF"))),
    ExpenseCategory.ENTERTAINMENT to Pair(Icons.Filled.Movie, Color(android.graphics.Color.parseColor("#800080"))),
    ExpenseCategory.HEALTH to Pair(Icons.Filled.LocalHospital, Color(android.graphics.Color.parseColor("#008000"))),
    ExpenseCategory.SHOPPING to Pair(Icons.Filled.ShoppingCart, Color(android.graphics.Color.parseColor("#FFA500"))),
    ExpenseCategory.BILLS to Pair(Icons.Filled.Receipt, Color(android.graphics.Color.parseColor("#800000"))),
    ExpenseCategory.RENT to Pair(Icons.Filled.Home, Color(android.graphics.Color.parseColor("#2F4F4F"))),
    ExpenseCategory.EDUCATION to Pair(Icons.Filled.School, Color(android.graphics.Color.parseColor("#000080"))),
    ExpenseCategory.TRAVEL to Pair(Icons.Filled.Public, Color(android.graphics.Color.parseColor("#20B2AA"))),
    ExpenseCategory.INSURANCE to Pair(Icons.Filled.Security, Color(android.graphics.Color.parseColor("#8B4513"))),
    ExpenseCategory.UTILITIES to Pair(Icons.Filled.Lightbulb, Color(android.graphics.Color.parseColor("#4682B4"))),
    ExpenseCategory.OTHERS to Pair(Icons.Outlined.HelpOutline, Color(android.graphics.Color.parseColor("#A9A9A9")))
)

val IncomeCategoryIcons = mapOf(
    IncomeCategory.SALARY to Pair(Icons.Filled.MonetizationOn, Color(android.graphics.Color.parseColor("#008B8B"))),
    IncomeCategory.BUSINESS to Pair(Icons.Filled.Storefront, Color(android.graphics.Color.parseColor("#DAA520"))),
    IncomeCategory.INVESTITIONS to Pair(Icons.Filled.TrendingUp, Color(android.graphics.Color.parseColor("#6B8E23"))),
    IncomeCategory.SIDE_HUSTLE to Pair(Icons.Filled.Build, Color(android.graphics.Color.parseColor("#B22222"))),
    IncomeCategory.FREELANCE to Pair(Icons.Filled.Laptop, Color(android.graphics.Color.parseColor("#6A5ACD"))),
    IncomeCategory.PENSION to Pair(Icons.Filled.Elderly, Color(android.graphics.Color.parseColor("#696969"))),
    IncomeCategory.RENTAL_INCOME to Pair(Icons.Filled.Apartment, Color(android.graphics.Color.parseColor("#483D8B"))),
    IncomeCategory.DIVIDENDS to Pair(Icons.Filled.MoneyOff, Color(android.graphics.Color.parseColor("#8B008B"))),
    IncomeCategory.GIFTS to Pair(Icons.Filled.CardGiftcard, Color(android.graphics.Color.parseColor("#228B22"))),
    IncomeCategory.LOANS to Pair(Icons.Filled.Note, Color(android.graphics.Color.parseColor("#CD853F"))),
    IncomeCategory.SAVINGS_INTEREST to Pair(Icons.Filled.AccountBalance, Color(android.graphics.Color.parseColor("#8B0000"))),
    IncomeCategory.OTHERS to Pair(Icons.Outlined.HelpOutline, Color(android.graphics.Color.parseColor("#D3D3D3")))
)




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownMenuItem(
    category: String,
    onClick: () -> Unit,
    icon: ImageVector,
    backgroundColor: Color,
    contentPadding: PaddingValues = ExposedDropdownMenuDefaults.ItemContentPadding
) {
    DropdownMenuItem(
        onClick = onClick,
        contentPadding = contentPadding,
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = backgroundColor,
                    modifier = Modifier
                        .size(24.dp)
                )
                Text(category)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuCategories(isExpense: Boolean, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categories = if (isExpense) ExpenseCategory.values() else IncomeCategory.values()

    val selectedExpenseCategory = if (ExpenseCategory.values().any { it.name == selectedCategory }) ExpenseCategory.valueOf(selectedCategory) else null
    val selectedIncomeCategory = if (IncomeCategory.values().any { it.name == selectedCategory }) IncomeCategory.valueOf(selectedCategory) else null

    val iconAndColor = if (isExpense) {
        if (selectedExpenseCategory != null) {
            ExpenseCategoryIcons[selectedExpenseCategory]
        } else null
    } else {
        if (selectedIncomeCategory != null) {
            IncomeCategoryIcons[selectedIncomeCategory]
        } else null
    }

    // Get screen width
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val buttonWidth = screenWidthDp * 0.45f

    ExposedDropdownMenuBox(
        expanded = !expanded,
        onExpandedChange = { !expanded },
    ) {
        OutlinedButton(
            onClick = {
                expanded = !expanded
            },
            modifier = Modifier
                .menuAnchor()
                .padding(start = 8.dp, end = 8.dp)
                .width(buttonWidth)
                .wrapContentSize(Alignment.Center),
        ) {
            if (selectedCategory.isNotBlank()) {
                Icon(
                    imageVector = iconAndColor?.first ?: Icons.Default.QuestionMark,
                    tint = iconAndColor?.second ?: Color.Transparent,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (selectedCategory.isBlank()) "Category" else selectedCategory)
            Icon(if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown, contentDescription = "expand dropdown")
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            categories.forEach { category ->
                val icon = if (isExpense) ExpenseCategoryIcons[category]?.first else IncomeCategoryIcons[category]?.first
                val color = if (isExpense) ExpenseCategoryIcons[category]?.second else IncomeCategoryIcons[category]?.second
                CategoryDropdownMenuItem(
                    category = category.name,
                    onClick = {
                        expanded = false
                        onCategorySelected(category.name)
                    },
                    icon = icon ?: Icons.Default.QuestionMark,
                    backgroundColor = color ?: Color.Transparent
                )
            }

        }
    }
}
