package ed.maevski.diabeticdiary.di

import dagger.Component
import ed.maevski.diabeticdiary.di.modules.RemoteModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [RemoteModule::class]
)
interface AppComponent {
}