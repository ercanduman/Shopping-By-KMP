package di


import business.core.AppDataStore
import business.core.AppDataStoreManager
import business.datasource.network.main.MainService
import business.datasource.network.main.MainServiceImpl
import business.datasource.network.splash.SplashService
import business.datasource.network.splash.SplashServiceImpl
import business.interactors.main.HomeInteractor
import business.interactors.main.MainInteractors
import business.interactors.main.ProductInteractor
import business.interactors.splash.CheckTokenInteractor
import business.interactors.splash.LoginInteractor
import business.interactors.splash.RegisterInteractor
import business.interactors.splash.SplashInteractors
import common.Context
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import presentation.ui.main.detail.view_model.DetailViewModel
import presentation.ui.main.home.view_model.HomeViewModel
import presentation.ui.main.wishlist.view_model.WishlistViewModel
import presentation.ui.splash.view_model.LoginViewModel


fun appModule(context: Context) = module {
    single { Json { isLenient = true; ignoreUnknownKeys = true } }
    single {
        HttpClient() {
            expectSuccess = false
            install(HttpTimeout) {
                val timeout = 60000L
                connectTimeoutMillis = timeout
                requestTimeoutMillis = timeout
                socketTimeoutMillis = timeout
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            install(ResponseObserver) {
                onResponse { response ->
                    println("AppDebug HTTP ResponseObserver status: ${response.status.value}")
                }
            }
            HttpResponseValidator {
                validateResponse { response: HttpResponse ->
                    val statusCode = response.status.value

                    /*
                                        when (statusCode) {
                                            in 300..399 -> throw RedirectResponseException(response)
                                            in 400..499 -> throw ClientRequestException(response)
                                            in 500..599 -> throw ServerResponseException(response)
                                        }

                                        if (statusCode >= 600) {
                                            throw ResponseException(response)
                                        }
                                    }

                                    handleResponseException { cause: Throwable ->
                                        throw cause
                                    }*/
                }
            }


            install(ContentNegotiation) {
                json(Json {
                    explicitNulls = false
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                    encodeDefaults = true
                    classDiscriminator = "#class"
                })
            }

        }
    }
    single<SplashService> { SplashServiceImpl(get()) }
    single<MainService> { MainServiceImpl(get()) }
    single<AppDataStore> { AppDataStoreManager(context) }
    single { SplashInteractors.build(get()) }
    single { MainInteractors.build(get()) }
    factory { LoginViewModel(get(), get(), get()) }
    factory { HomeViewModel(get()) }
    factory { WishlistViewModel() }
    factory { DetailViewModel(get()) }
    single { LoginInteractor(get(), get()) }
    single { RegisterInteractor(get(), get()) }
    single { CheckTokenInteractor(get()) }
    single { HomeInteractor(get(), get()) }
    single { ProductInteractor(get(), get()) }
}