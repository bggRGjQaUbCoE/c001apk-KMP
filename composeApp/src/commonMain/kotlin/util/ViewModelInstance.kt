package util

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class ViewModelInstance(val dispatcher: CoroutineDispatcher) : InstanceKeeper.Instance {
    val viewModelScope = CoroutineScope(dispatcher + SupervisorJob())
    override fun onDestroy() {
        viewModelScope.cancel()
    }
}