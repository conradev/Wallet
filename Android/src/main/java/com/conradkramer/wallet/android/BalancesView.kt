@file:OptIn(ExperimentalMaterial3Api::class)

package com.conradkramer.wallet.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conradkramer.wallet.PreviewMocks
import com.conradkramer.wallet.viewmodel.BalancesViewModel
import com.conradkramer.wallet.viewmodel.MainViewModel

@Composable
fun BalancesView(viewModel: BalancesViewModel, padding: PaddingValues) {
    val walletTitle by viewModel.accountName.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val assets by viewModel.assets.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement
            .spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            text = walletTitle,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            style = MaterialTheme.typography.headlineLarge,
            text = totalBalance,
            textAlign = TextAlign.Center
        )

        for (balance in assets) {
            AssetBalanceView(balance)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun BalancesViewPreview() {
    Scaffold(
        topBar = { TopBar(tab = MainViewModel.Tab.BALANCE) }
    ) {
        BalancesView(PreviewMocks.get(), it)
    }
}
