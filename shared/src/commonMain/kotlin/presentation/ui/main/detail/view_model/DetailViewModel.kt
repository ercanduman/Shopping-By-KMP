package presentation.ui.main.detail.view_model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import business.core.DataState
import business.core.NetworkState
import business.core.Queue
import business.core.UIComponent
import business.interactors.main.HomeInteractor
import business.interactors.main.ProductInteractor
import business.interactors.splash.CheckTokenInteractor
import business.interactors.splash.LoginInteractor
import business.interactors.splash.RegisterInteractor
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class DetailViewModel(
    private val productInteractor: ProductInteractor,
) : ViewModel() {


    private val TAG = "AppDebug LoginViewModel"


    val state: MutableState<DetailState> = mutableStateOf(DetailState())


    fun onTriggerEvent(event: DetailEvent) {
        when (event) {

            is DetailEvent.OnUpdateSelectedImage -> {
                onUpdateSelectedImage(event.value)
            }

            is DetailEvent.GetProduct -> {
                getProduct(event.id)
            }

            is DetailEvent.OnRemoveHeadFromQueue -> {
                removeHeadMessage()
            }

            is DetailEvent.Error -> {
                appendToMessageQueue(event.uiComponent)
            }

            is DetailEvent.OnRetryNetwork -> {
                onRetryNetwork()
            }

            is DetailEvent.OnUpdateNetworkState -> {
                onUpdateNetworkState(event.networkState)
            }
        }
    }

    private fun onUpdateSelectedImage(value: String) {
        state.value = state.value.copy(selectedImage = value)
    }


    private fun getProduct(id: Int) {
        productInteractor.execute(id = id).onEach { dataState ->
            when (dataState) {
                is DataState.NetworkStatus -> {}
                is DataState.Response -> {
                    onTriggerEvent(DetailEvent.Error(dataState.uiComponent))
                }

                is DataState.Data -> {
                    dataState.data?.let {
                        state.value = state.value.copy(product = it)
                        state.value =
                            state.value.copy(selectedImage = it.gallery.firstOrNull() ?: "")
                    }
                }

                is DataState.Loading -> {
                    state.value =
                        state.value.copy(progressBarState = dataState.progressBarState)
                }
            }
        }.launchIn(viewModelScope)
    }


    private fun appendToMessageQueue(uiComponent: UIComponent) {
        if (uiComponent is UIComponent.None) {
            println("${TAG}: onTriggerEvent:  ${(uiComponent as UIComponent.None).message}")
            return
        }

        val queue = state.value.errorQueue
        queue.add(uiComponent)
        state.value = state.value.copy(errorQueue = Queue(mutableListOf())) // force recompose
        state.value = state.value.copy(errorQueue = queue)
    }

    private fun removeHeadMessage() {
        try {
            val queue = state.value.errorQueue
            queue.remove() // can throw exception if empty
            state.value = state.value.copy(errorQueue = Queue(mutableListOf())) // force recompose
            state.value = state.value.copy(errorQueue = queue)
        } catch (e: Exception) {
            println("${TAG}: removeHeadMessage: Nothing to remove from DialogQueue")
        }
    }


    private fun onRetryNetwork() {
        getProduct(state.value.product.id)
    }


    private fun onUpdateNetworkState(networkState: NetworkState) {
        state.value = state.value.copy(networkState = networkState)
    }


}