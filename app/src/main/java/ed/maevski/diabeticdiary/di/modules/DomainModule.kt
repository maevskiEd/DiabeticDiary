package ed.maevski.diabeticdiary.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import ed.maevski.diabeticdiary.data.DDRepository
import ed.maevski.diabeticdiary.data.YandexApi
import ed.maevski.diabeticdiary.domain.DDInteractor
import javax.inject.Singleton

@Module
class DomainModule(val context: Context) {
    //Нам нужно контекст как-то провайдить, поэтому создаем такой метод
    @Provides
    fun provideContext() = context

    @Singleton
    @Provides
    fun provideInteractor(repository: DDRepository, yandexApi: YandexApi) =
        DDInteractor(repo = repository, retrofitService = yandexApi)
}