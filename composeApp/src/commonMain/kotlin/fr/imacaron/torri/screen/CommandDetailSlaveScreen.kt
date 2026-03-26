package fr.imacaron.torri.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import fr.imacaron.torri.components.CommandList
import fr.imacaron.torri.viewmodel.SlaveCommandViewModel

@Composable
fun CommandDetailSlaveScreen(slaveCommandViewModel: SlaveCommandViewModel) {
	LaunchedEffect(slaveCommandViewModel) {
		slaveCommandViewModel.loadHistory()
	}
	slaveCommandViewModel.priceList?.let { priceList ->
		CommandList(slaveCommandViewModel.history, slaveCommandViewModel, priceList.priceList, slaveCommandViewModel.items)
	}
}