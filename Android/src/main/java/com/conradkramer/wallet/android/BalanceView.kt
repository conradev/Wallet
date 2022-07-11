package com.conradkramer.wallet.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conradkramer.wallet.PreviewMocks
import com.conradkramer.wallet.viewmodel.BalancesViewModel

@Composable
fun BalanceView(viewModel: BalancesViewModel) {
    val walletTitle = viewModel.accountName
    val totalBalance by viewModel.totalBalance.collectAsState()
    val balances by viewModel.balances.collectAsState()
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                text = walletTitle,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                text = totalBalance,
                textAlign = TextAlign.Center,
                fontSize = 32.sp
            )

            for (balance in balances) {
                BalanceRow(balance)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun BalanceViewPreview() {
    BalanceView(PreviewMocks.get())
}
