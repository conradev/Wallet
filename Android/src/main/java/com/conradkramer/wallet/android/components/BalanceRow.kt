package com.conradkramer.wallet.android.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conradkramer.wallet.viewmodel.BalanceViewModel

@Composable
fun BalanceRow(balance: BalanceViewModel.RowViewModel) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = balance.displayName,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "\$TODO",
                    textAlign = TextAlign.End
                )
                Text(
                    text = balance.balance,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}