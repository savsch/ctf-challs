package com.google.calendar.android

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun VoucherRedeemScreen(
    voucherCode: String?,
    onCodeSubmit: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(voucherCode == null) }
    var inputCode by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = if (voucherCode==null )"NetherGames Voucher Redeem" else "NetherGames Transfer Code Status",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        voucherCode?.let { code ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = code,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Redeemed less than an hour ago",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Awaiting admin approval. We appreciate your patience",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val ctx = LocalContext.current
            Button(
                onClick = {
                    Toast.makeText(ctx, "Please wait until the current code transfer is approved.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Redeem another code",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* Non-dismissable */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            title = {
                Text("Enter Voucher or Transfer Code")
            },
            text = {
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    label = { Text("Voucher/Transfer Code") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputCode.isNotBlank()) {
                            showDialog = onCodeSubmit(inputCode)
                        }
                    },
                    enabled = inputCode.isNotBlank()
                ) {
                    Text("Submit")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VoucherRedeemScreenPreview() {
    MaterialTheme {
        VoucherRedeemScreen(
            voucherCode = null,
            onCodeSubmit = {true}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VoucherRedeemScreenWithCodePreview() {
    MaterialTheme {
        VoucherRedeemScreen(
            voucherCode = "EXAMPLE123",
            onCodeSubmit = {true}
        )
    }
}