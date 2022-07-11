@file:OptIn(ExperimentalMaterial3Api::class)

package com.conradkramer.wallet.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun BalanceRow(balance: BalanceViewModel) {
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
                text = balance.currencyName,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = balance.formattedConvertedBalance,
                    textAlign = TextAlign.End
                )
                Text(
                    text = balance.formattedBalance,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
