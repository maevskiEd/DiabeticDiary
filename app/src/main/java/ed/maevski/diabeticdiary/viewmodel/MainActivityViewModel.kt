package ed.maevski.diabeticdiary.viewmodel

import androidx.lifecycle.ViewModel
import ed.maevski.diabeticdiary.App
import ed.maevski.diabeticdiary.domain.DDInteractor
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivityViewModel: ViewModel() {
    @Inject
    lateinit var interactor: DDInteractor

    init {
        App.instance.dagger.inject(this)
    }
}