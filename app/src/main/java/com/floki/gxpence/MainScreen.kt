package com.floki.gxpence

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amplifyframework.datastore.generated.model.Expense
import com.amplifyframework.datastore.generated.model.Income
import com.floki.gxpence.expenses.ExpenseViewModel
import com.floki.gxpence.ui.theme.GradientEnd
import com.floki.gxpence.ui.theme.GradientStart
import com.floki.gxpence.ui.theme.GxpenceTheme



@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigateToExpenseScreen: () -> Unit,
    navigateToSettingsScreen: () -> Unit,
    chatViewModel: ChatViewModel,
    username: String,
) {
    val viewModel: ExpenseViewModel =
        viewModel(factory = MainActivity.ExpenseViewModelFactory(username))
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    var openBottomSheetGPT by remember { mutableStateOf(false) }

    val navBarItems = listOf(
        NavItem(
            name = "Transactions",
            icon = ImageVector.vectorResource(id = R.drawable.money_plus),
            onClick = { navigateToExpenseScreen() }
        ),
        NavItem(
            name = "Settings",
            icon = ImageVector.vectorResource(id = R.drawable.settings_minimalistic_bold),
            onClick = { navigateToSettingsScreen() }
        )

    )
    var selectedTab by remember { mutableStateOf(navBarItems[0].name) }

    // Collect StateFlows here
    val income by viewModel.income.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    MaterialTheme {

        Scaffold(
            topBar = { GxpenseTopAppBar() },
            bottomBar = {
                GxpenseBottomAppBar(
                    navBarItems,
                    selectedTab,
                    navigateToExpenseScreen,
                    onTabSelected = { newTab -> selectedTab = newTab },
                    onFabClick = {
                        openBottomSheetGPT = true
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                item { BalanceCard(income, expenses) }
                item {
                    Column {
                        LastExpenses(expenses)
                        LastIncome(income)
                    }
                }
            }

            ProfileBottomSheet(openBottomSheet) {
                openBottomSheet = false
            }

            SheetBottomGPT(openBottomSheetGPT, { openBottomSheetGPT = false }, chatViewModel, username)

        }
    }
}





@Composable
fun BalanceCard(income: List<Income>, expenses: List<Expense>) {
    val totalIncome = income.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense
        val gradientBrush = Brush.linearGradient(
            colors = listOf(GradientStart, GradientEnd),
            start = Offset.Zero,
            end = Offset.Infinite,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = gradientBrush)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Balance",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "$totalBalance",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val UpArrowIcon = Icons.Default.ArrowUpward
                            Icon(UpArrowIcon, contentDescription = "Expenses", tint = Color.White)
                            Text(
                                text = "Expenses",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "$totalExpense",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val DownArrowIcon = Icons.Default.ArrowDownward
                            Icon(DownArrowIcon, contentDescription = "Incomes", tint = Color.White)
                            Text(
                                text = "Incomes",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "$totalIncome",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun TransactionCard(transaction: Any, isExpense: Boolean) {
    val amount = if (transaction is Expense) transaction.amount else (transaction as Income).amount
    val category =
        if (transaction is Expense) transaction.category else (transaction as Income).category
    val notes = if (transaction is Expense) transaction.notes else (transaction as Income).notes
    val date =
        if (transaction is Expense) transaction.createdAt else (transaction as Income).createdAt
    val backgroundColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.White

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.background(backgroundColor)) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = category.toString(), style = MaterialTheme.typography.labelMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isExpense) "-$amount" else "+$amount",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun LastExpenses(expenses: List<Expense>) {
    val lastTwoExpenses = expenses.sortedByDescending { it.createdAt }.take(2)
    Column {
        Text(
            text = "Recent Expenses",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            lastTwoExpenses.forEach { expense ->
                TransactionCard(expense, isExpense = true)
            }
        }
    }
}

@Composable
fun LastIncome(incomes: List<Income>) {
    val lastTwoIncomes = incomes.sortedByDescending { it.createdAt }.take(2)
    Column {
        Text(
            text = "Recent Income",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            lastTwoIncomes.forEach { income ->
                TransactionCard(income, isExpense = false)
            }
        }
    }
}



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GxpenseTopAppBar() {
        TopAppBar(
            title = { Text("Home") },

        )
        Box(modifier = Modifier.background(Color(0xFFF5F5F5))) {
        }
    }
    @Composable
    fun GxpenseBottomAppBar(
        navBarItems: List<NavItem>,
        selectedTab: String,
        navigateToExpenseScreen: () -> Unit,
        onTabSelected: (String) -> Unit,
        onFabClick: () -> Unit
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
