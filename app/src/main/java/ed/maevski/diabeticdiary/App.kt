package ed.maevski.diabeticdiary

import android.app.Application
import ed.maevski.diabeticdiary.di.AppComponent
import ed.maevski.diabeticdiary.di.DaggerAppComponent

class App : Application() {
    lateinit var dagger: AppComponent

    override fun onCreate() {
        super.onCreate()

        instance = this

    }

    companion object {
        //Здесь статически хранится ссылка на экземпляр App
        lateinit var instance: App
            //Приватный сеттер, чтобы нельзя было в эту переменную присвоить что-либо другое
            private set
    }
}