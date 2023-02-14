package com.conradkramer.wallet.android.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.layout.Alignment
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.conradkramer.wallet.viewmodel.Asset
import com.conradkramer.wallet.viewmodel.BalancesViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class BalanceWidget : GlanceAppWidget(), KoinComponent {

    override val sizeMode: SizeMode = SizeMode.Exact

    private val viewModel: BalancesViewModel by get()

    @Composable
    override fun Content() {
        val walletTitle by viewModel.accountName.collectAsState()

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(text = walletTitle)
        }
//        val totalBalance by viewModel.totalBalance.collectAsState()
//        val assets by viewModel.assets.collectAsState()
//        Column(
//            modifier = GlanceModifier
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalAlignment = Alignment.Vertical.Top,
//            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
//        ) {
//            Text(
//                modifier = GlanceModifier
//                    .fillMaxWidth()
//                    .padding(top = 16.dp),
//                text = walletTitle,
//            )
//            Text(
//                modifier = GlanceModifier
//                    .fillMaxWidth()
//                    .padding(vertical = 16.dp),
//                text = totalBalance,
//                maxLines = 1
//            )
//            for (balance in assets) {
//                AssetBalanceView(balance)
//            }
//        }
    }

    @Composable
    private fun AssetBalanceView(asset: Asset) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = asset.balance.currency.name,
                    )
                    Text(
                        text = asset.balanceString,
                    )
                }
                Text(
                    text = asset.fiatBalanceString,
                )
            }
    }
}

class BalanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BalanceWidget()
}
