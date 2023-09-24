package ed.maevski.diabeticdiary.di

import dagger.Component
import ed.maevski.diabeticdiary.di.modules.DatabaseModule
import ed.maevski.diabeticdiary.di.modules.DomainModule
import ed.maevski.diabeticdiary.di.modules.RemoteModule
import ed.maevski.diabeticdiary.di.modules.S3Module
import ed.maevski.diabeticdiary.view.MainActivity
import ed.maevski.diabeticdiary.viewmodel.AudioJournalFragmentViewModel
import ed.maevski.diabeticdiary.viewmodel.MainActivityViewModel
import javax.inject.Singleton

@Singleton
@Component(
    //Внедряем все модули, нужные для этого компонента
    modules = [
        RemoteModule::class,
        DatabaseModule::class,
        DomainModule::class,
        S3Module::class
    ]
)
interface AppComponent {
    //метод для того, чтобы появилась внедрять зависимости в HomeFragmentViewModel
    fun inject(mainActivityViewModel: MainActivityViewModel)

    //метод для того, чтобы появилась внедрять зависимости в HomeFragmentViewModel
    fun inject(audioJournalFragmentViewModel: AudioJournalFragmentViewModel)

    /*    //метод для того, чтобы появилась возможность внедрять зависимости в SettingsFragmentViewModel
        fun inject(settingsFragmentViewModel: SettingsFragmentViewModel)*/
}