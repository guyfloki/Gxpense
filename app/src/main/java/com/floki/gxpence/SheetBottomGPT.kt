package com.floki.gxpence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SheetBottomGPT(
    openBottomSheetGPT: Boolean, closeBottomSheet: () -> Unit, chatViewModel: ChatViewModel, username: String
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val bottomSheetState = rememberModalBottomSheetState(true)
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by keyboardAsState()
    var isKeyboardVisible by remember { mutableStateOf(false) }


    var isSending by remember { mutableStateOf(false) }


    val chatMessages by chatViewModel.chatMessages.collectAsState()

    LaunchedEffect(key1 = openBottomSheetGPT) {
        if (openBottomSheetGPT) {
            coroutineScope.launch {
                bottomSheetState.expand()
            }
        } else {
            coroutineScope.launch {
                bottomSheetState.hide()
            }
        }
    }

    if (openBottomSheetGPT) {
        ModalBottomSheet(
            onDismissRequest = closeBottomSheet,
            sheetState = bottomSheetState,
        ) {
            Column(
                Modifier
                    .fillMaxHeight(if (isKeyboardOpen) 0.45f else 0.9f)
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    IconButton(onClick = { chatViewModel.clearChatMessages() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    LazyColumn {
                        items(chatMessages) { chatMessage ->
                            if (chatMessage.isUser) {
                                UserMessage(chatMessage.message)
                            } else {
                                AssistantMessage(chatMessage.message)
                            }
                        }
                    }
                }


                Row(
                        Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically // Align items vertically
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { newText ->
                                text = newText
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 200.dp), // Set a max height for the TextField

                            placeholder = { Text("Type your message here") }
                        )

                    if (isSending) {
                        CircularProgressIndicator()
                    } else {
                        IconButton(
                            onClick = {
                                if (text.text.isNotEmpty()) {
                                    keyboardController?.hide()
                                    coroutineScope.launch {
                                        isSending = true
                                        chatViewModel.sendMessage(text.text, username) // Include the username here
                                        text = TextFieldValue("") // Clear TextField
                                        isSending = false
                                    }
                                }
                            },
                            enabled = text.text.isNotEmpty()
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send")
                        }
                    }

                }
                }
            }
        }
    }


@Composable
fun UserMessage(message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.8f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(Modifier.background(Color.LightGray)) {
                Text(
                    text = message,
                    Modifier.padding(8.dp),
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun AssistantMessage(message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.8f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(Modifier.background(Color.White)) {
                Text(
                    text = message,
                    Modifier.padding(8.dp),
                    color = Color.Black
                )
            }
        }
    }
}
