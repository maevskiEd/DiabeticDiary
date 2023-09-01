package ed.maevski.diabeticdiary.view.rv_adapters

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import ed.maevski.diabeticdiary.view.adapters.AudioFileDelegateAdapter

class AudioJournalRecyclerAdapter() : ListDelegationAdapter<List<Any>>() {

    init {
        delegatesManager.addDelegate(AudioFileDelegateAdapter())
    }

    override fun setItems(items: List<Any>?) {
        super.setItems(items)
        notifyDataSetChanged()
    }
}