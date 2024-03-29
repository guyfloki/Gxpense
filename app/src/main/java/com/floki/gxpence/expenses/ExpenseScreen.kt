package com.floki.gxpence.expenses


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amplifyframework.datastore.generated.model.Expense
import com.amplifyframework.datastore.generated.model.Income
import com.floki.gxpence.ChatViewModel
import com.floki.gxpence.MainActivity
import com.floki.gxpence.R
import com.floki.gxpence.SheetBottomGPT
import com.floki.gxpence.ui.theme.GradientEnd
import com.floki.gxpence.ui.theme.GradientStart
import com.floki.gxpence.ui.theme.GxpenceTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navigateBack: () -> Unit,
    navigateToMainScreen: () -> Unit,
    username: String,
    navigateToAddExpense: () -> Unit,
    navigateToSettingsScreen: () -> Unit,
    chatViewModel: ChatViewModel,
    navigateToExpenseEdit: (String) -> Unit,
    navigateToIncomeEdit: (String) -> Unit,
) {
    val viewModel: ExpenseViewModel =
        viewModel(factory = MainActivity.ExpenseViewModelFactory(username))
    val expenses by viewModel.expenses.collectAsState()
    val incomes by viewModel.income.collectAsState()

    val toggleViewModel: ToggleViewModel = viewModel()

    var openBottomSheetGPT by remember { mutableStateOf(false) }

    // Create a DateRangePickerState
    val dateRangePickerState = rememberDateRangePickerState()
    val selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis
    val selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis


    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

    val filteredExpenses = expenses.filter {
        val createdAtMillis = dateFormat.parse(it.createdAt)?.time ?: 0
        if (selectedStartDateMillis != null && selectedEndDateMillis != null) {
            createdAtMillis in selectedStartDateMillis..selectedEndDateMillis
        } else {
            true
        }
    }

    val filteredIncomes = incomes.filter {
        val createdAtMillis = dateFormat.parse(it.createdAt)?.time ?: 0
        if (selectedStartDateMillis != null && selectedEndDateMillis != null) {
            createdAtMillis in selectedStartDateMillis..selectedEndDateMillis
        } else {
            true
        }
    }




    val isXPCardVisible by toggleViewModel.isXPCardVisible.collectAsState()

    val navBarItems = listOf(
        NavItem(
            name = "Home",
            icon = Icons.Default.Home,
            onClick = { navigateToMainScreen() }
        ),
        NavItem(
            name = "Settings",
            icon = ImageVector.vectorResource(id = R.drawable.settings_minimalistic_bold),
            onClick = { /* Do nothing here */ }
        )
    )
    var selectedTab by remember { mutableStateOf(navBarItems[0].name) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    var isFilterActive by remember { mutableStateOf(false) }
    MaterialTheme {
        Scaffold(
            topBar = {
                GxpenseTopAppBar(
                    navigateBack = navigateBack,
                    isFilterActive = isFilterActive,  // Pass isFilterActive here
                    onFilterClick = { isFilterActive = !isFilterActive }  // Change isFilterActive when filter is clicked
                )
            },
            bottomBar = {
                GxpenseBottomAppBar(
                    navBarItems = navBarItems,
                    selectedTab = selectedTab,
                    navigateBack = navigateBack,
                    onTabSelected = { newTab ->
                        selectedTab = newTab
                        if (newTab == "Other") {
                            openBottomSheet = true
                        }
                    },
                    onFabClick = { openBottomSheetGPT = true },
                    AddExpenseFAB = navigateToAddExpense
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(paddingValues)) {
                    ProfileBottomSheet(openBottomSheet) {
                        openBottomSheet = false
                    }

                    // Default view with expenses and incomes list
                    ToggleButton(isXPCardVisible) {
                        toggleViewModel.switch()
                    }


                    SheetBottomGPT(openBottomSheetGPT, { openBottomSheetGPT = false }, chatViewModel, username)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        /*
                        IconButton(onClick = {
                            dateRangePickerState.setSelection(null, null)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete dates")
                        }
                        */
                        if (isFilterActive) {
                            Column {
                                IconButton(onClick = {
                                    dateRangePickerState.setSelection(null, null)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete dates")
                                }
                                DateRangePicker(
                                    state = dateRangePickerState
                                )

                            }// Only show the DateRangePicker when isFilterActive is true

                        }
                    }


                    if (isXPCardVisible) {
                        ExpenseList(filteredExpenses, navigateToExpenseEdit)
                    } else {
                        IncomeList(filteredIncomes, navigateToIncomeEdit)
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GxpenseTopAppBar(
    navigateBack: () -> Unit,
    isFilterActive: Boolean,  // Receive isFilterActive
    onFilterClick: () -> Unit  // Receive onFilterClick
) {
    TopAppBar(
        title = { Text("Transactions") },
        actions = {

            IconButton(onClick = { onFilterClick() }) {  // Call onFilterClick when filter icon is clicked
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = if (isFilterActive) Color.Green else Color.Unspecified
                )
            }
        }
    )
}






@Composable
fun VerticalProgressBar(
    progress: Float,
    gradientStart: Color = Color(0xFFFF9572), // Light Coral
    gradientEnd: Color = Color(0xFFD069F6), // Medium Orchid
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    width: Dp = 10.dp,
    height: Dp = 200.dp,
    color: Color = Color.Gray
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.verticalGradient(
                colors = listOf(gradientStart, gradientEnd),
                startY = size.height,
                endY = size.height - size.height * progress
            )
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x = 0f, y = size.height - size.height * progress),
                size = Size(width = size.width, height = size.height * progress),
                cornerRadius = CornerRadius(cornerRadius.value)
            )
        }
    }
}

@Composable
fun MyProgressBarExpenses(expenses: List<Expense>) {
    val progressState = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }
    val monthLabels = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    val groupedExpenses = expenses.groupBy {
        it.createdAt.split("T")[0].substring(0, 7)
    }

    LaunchedEffect(key1 = expenses) {
        val currentMonth = LocalDate.now().monthValue
        val currentYear = LocalDate.now().year
        var totalAmount = 0f
        val amounts = FloatArray(9)
        for (i in 0..8) {
            val month = if (currentMonth - i > 0) currentMonth - i else currentMonth - i + 12
            val year = if (currentMonth - i > 0) currentYear else currentYear - 1
            val monthKey = "$year-${month.toString().padStart(2, '0')}"
            monthLabels[i] = monthKey.substring(5, 7)
            val expensesForMonth = groupedExpenses[monthKey]
            val amountForMonth = expensesForMonth?.sumOf { it.amount.toDouble() }?.toFloat() ?: 0f
            amounts[i] = amountForMonth
            totalAmount += amountForMonth
        }
        if (totalAmount != 0f) {
            for (i in 0..8) {
                progressState[i] = amounts[i] / totalAmount
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 8 downTo 0) {
                    if (i != 8) {
                        Spacer(modifier = Modifier.width(22.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        VerticalProgressBar(progress = progressState[i])
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = monthLabels[i], style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun MyProgressBarIncomes(incomes: List<Income>) {
    val progressState = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }
    val monthLabels = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    val groupedIncomes = incomes.groupBy {
        it.createdAt.split("T")[0].substring(0, 7)
    }

    LaunchedEffect(key1 = incomes) {
        val currentMonth = LocalDate.now().monthValue
        val currentYear = LocalDate.now().year
        var totalAmount = 0f
        val amounts = FloatArray(9)
        for (i in 0..8) {
            val month = if (currentMonth - i > 0) currentMonth - i else currentMonth - i + 12
            val year = if (currentMonth - i > 0) currentYear else currentYear - 1
            val monthKey = "$year-${month.toString().padStart(2, '0')}"
            monthLabels[i] = monthKey.substring(5, 7)
            val incomesForMonth = groupedIncomes[monthKey]
            val amountForMonth = incomesForMonth?.sumOf { it.amount.toDouble() }?.toFloat() ?: 0f
            amounts[i] = amountForMonth
            totalAmount += amountForMonth
        }
        if (totalAmount != 0f) {
            for (i in 0..8) {
                progressState[i] = amounts[i] / totalAmount
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 8 downTo 0) {
                    if (i != 8) {
                        Spacer(modifier = Modifier.width(22.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        VerticalProgressBar(progress = progressState[i])
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = monthLabels[i], style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}




@Composable
fun GxpenseBottomAppBar(
    navBarItems: List<NavItem>,
    selectedTab: String,
    navigateBack: () -> Unit,
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit,
    AddExpenseFAB: () -> Unit
) {
    Box {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationBar {
                        navBarItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.name) },
                                selected = false,
                                onClick = {
                                    onTabSelected(item.name)
                                    item.onClick()
                                }
                            )
                        }
                    }
                }
            }
        )
        val gradientBrush = Brush.linearGradient(
            colors = listOf(GradientStart, GradientEnd),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
        val backgroundColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -32.dp)
                .scale(1.4f),
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(gradientBrush),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(50.5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null)
                }
            }
        }
        FloatingActionButton(
            onClick = AddExpenseFAB,
            modifier = Modifier
                .size(65.dp)
                .align(Alignment.TopEnd)
                .absoluteOffset(x = (-7).dp, y = (-65).dp),
            shape = CircleShape,
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    openBottomSheet: Boolean,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = bottomSheetState,
        ) {
            Column(Modifier.fillMaxHeight(0.5f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    // Content of the bottom sheet
                }
            }
        }
    }
}

data class NavItem(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ToggleButton(
    isXPCardVisible: Boolean,
    onToggle: () -> Unit
) {

    val transparentBrush = Brush.linearGradient(
        colors = listOf(Color.Transparent, Color.Transparent)
    )
    val gradientBrush = Brush.linearGradient(
        colors = listOf(GradientStart, GradientEnd),
        start = Offset.Zero,
        end = Offset.Infinite,
    )
    val darkTheme = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush = if (isXPCardVisible) gradientBrush else transparentBrush)
        ) {
            Button(
                onClick = { onToggle() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.size(250.dp, 50.dp)
            ) {
                Text(
                    text = "Expense",
                    color = if (isXPCardVisible) {
                        if (darkTheme) Color.Black else Color.White
                    } else {
                        if (darkTheme) Color.White else Color.Black
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush = if (!isXPCardVisible) gradientBrush else transparentBrush)
        ) {
            Button(
                onClick = { onToggle() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.size(250.dp, 50.dp)
            ) {
                Text(
                    text = "Income",
                    color = if (!isXPCardVisible) {
                        if (darkTheme) Color.Black else Color.White
                    } else {
                        if (darkTheme) Color.White else Color.Black
                    }
                )
            }
        }
    }
}

@Composable
private fun ParallaxCard(
    modifier: Modifier = Modifier,
    factor: Float,
    scrollPosition: Int,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = factor * scrollPosition,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
    )

    val scale = 1f - (factor * scrollPosition / 30000f) // increase divisor to amplify scaling effect
    val rotation = factor * scrollPosition / 500f // increase divisor to amplify rotation effect

    Card(
        modifier = modifier
            .graphicsLayer {
                translationY =
                    animatedProgress / 6f // decrease divisor here to amplify translation effect
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .animateContentSize()
    ) {
        content()
    }
}

fun dateToDisplayFormat(date: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC") // specify the timezone for inputFormat
    val outputFormat = SimpleDateFormat("EEE.dd.MMM.yyyy", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getTimeZone("UTC")

    return outputFormat.format(inputFormat.parse(date)!!) // convert "2023-01-01" to "Wed.01.Jan.2023"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseList(expenses: List<Expense>, navigateToExpenseEdit: (String) -> Unit) {
    val listState = rememberLazyListState()

    val groupedExpenses = expenses.groupBy {
        it.createdAt.split("T")[0] // group expenses by the date part of the createdAt field
    }.mapValues { (_, expensesForDate) ->
        expensesForDate.sortedByDescending { it.createdAt } // sort expenses of the same day by time
    }.toSortedMap(reverseOrder()) // sort dates in descending order

    LazyColumn(state = listState, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        item {
            MyProgressBarExpenses(expenses)
        }
        groupedExpenses.keys.forEachIndexed { index, date ->
            val expensesForDate = groupedExpenses[date]!!
            stickyHeader {
                Text(
                    text = dateToDisplayFormat(date),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            itemsIndexed(expensesForDate) { index, expense ->
                ParallaxCard(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { navigateToExpenseEdit(expense.id) },
                    factor = index.toFloat() / expensesForDate.size,
                    scrollPosition = listState.firstVisibleItemScrollOffset
                ) {
                    TransactionCard(transaction = expense, isExpense = true)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// The same change applies to IncomeList
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IncomeList(incomes: List<Income>, navigateToIncomeEdit: (String) -> Unit) {
    val listState = rememberLazyListState()

    val groupedIncomes = incomes.groupBy {
        it.createdAt.split("T")[0] // group incomes by the date part of the createdAt field
    }.mapValues { (_, incomesForDate) ->
        incomesForDate.sortedByDescending { it.createdAt } // sort incomes of the same day by time
    }.toSortedMap(reverseOrder()) // sort dates in descending order

    LazyColumn(state = listState, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        item {
            MyProgressBarIncomes(incomes)
        }
        groupedIncomes.keys.forEachIndexed { index, date ->
            val incomesForDate = groupedIncomes[date]!!
            stickyHeader {
                Text(
                    text = dateToDisplayFormat(date),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            itemsIndexed(incomesForDate) { index, income ->
                ParallaxCard(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { navigateToIncomeEdit(income.id) },
                    factor = index.toFloat() / incomesForDate.size,
                    scrollPosition = listState.firstVisibleItemScrollOffset
                ) {
                    TransactionCard(transaction = income, isExpense = false)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Any, isExpense: Boolean) {
    val amount = if (transaction is Expense) transaction.amount else (transaction as Income).amount
    val category = if (transaction is Expense) transaction.category else (transaction as Income).category
    val notes = if (transaction is Expense) transaction.notes else (transaction as Income).notes
    var date = if (transaction is Expense) transaction.createdAt else (transaction as Income).createdAt
    date = formatDateToHoursMinutes(date)

    val iconColorPair = if (isExpense) ExpenseCategoryIcons[category] else IncomeCategoryIcons[category]

    Card(
        shape = RoundedCornerShape(8.dp), // add a shape with rounded corners to the Card
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    iconColorPair?.first?.let { Icon(it, contentDescription = null, tint = iconColorPair?.second ?: Color.Transparent) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = category.toString(), style = MaterialTheme.typography.labelMedium)
                }
                Text(text = notes, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = if (isExpense) "-$amount" else "+$amount", style = MaterialTheme.typography.titleMedium)
                Text(text = date, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


fun formatDateToHoursMinutes(dateStr: String): String {
    val zonedDateTime = ZonedDateTime.parse(dateStr)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return zonedDateTime.format(formatter)
}
