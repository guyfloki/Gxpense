package com.floki.gxpence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.floki.gxpence.ui.theme.GxpenceTheme


@Composable
fun LoginScreen(
    setupGoogleSignIn: () -> Unit,
    navigateToMainScreen: () -> Unit,
    isSignedIn: Boolean,
) {
    GxpenceTheme {
        if (isSignedIn) {
            navigateToMainScreen()
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(16.dp))

                    Image(
                        painter = painterResource(id = R.drawable.robot),
                        contentDescription = "Robot Image",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1f),
                    )
                }

                OutlinedButton(
                    onClick = {
                        setupGoogleSignIn()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                    ),
                    border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color.White else Color.Black),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 96.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Continue with Google")
                }
            }
        }
    }
}

