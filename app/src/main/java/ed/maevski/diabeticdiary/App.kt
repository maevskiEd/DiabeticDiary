package ed.maevski.diabeticdiary

import android.app.Application
import ed.maevski.diabeticdiary.di.AppComponent
import ed.maevski.diabeticdiary.di.DaggerAppComponent
import ed.maevski.diabeticdiary.di.modules.DatabaseModule
import ed.maevski.diabeticdiary.di.modules.DomainModule
import ed.maevski.diabeticdiary.di.modules.RemoteModule

class App : Application() {
    lateinit var dagger: AppComponent

    override fun onCreate() {
        super.onCreate()

        instance = this

        dagger = DaggerAppComponent.builder()
            .remoteModule(RemoteModule())
            .databaseModule(DatabaseModule())
            .domainModule(DomainModule(this))
            .build()
    }

    companion object {
        //Здесь статически хранится ссылка на экземпляр App
        lateinit var instance: App
            //Приватный сеттер, чтобы нельзя было в эту переменную присвоить что-либо другое
            private set
    }
}