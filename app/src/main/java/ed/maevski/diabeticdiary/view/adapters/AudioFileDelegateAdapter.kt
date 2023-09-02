package ed.maevski.diabeticdiary.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ed.maevski.diabeticdiary.data.entity.Audiofile
import ed.maevski.diabeticdiary.databinding.ItemAudiofileBinding

class AudioFileDelegateAdapter:
    AbsListItemAdapterDelegate<Audiofile, Any, AudioFileDelegateAdapter.ViewHolder>() {
    class ViewHolder(binding: ItemAudiofileBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameFile = binding.nameFile
    }

    override fun isForViewType(item: Any, items: MutableList<Any>, position: Int): Boolean {
        return item is Audiofile
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemAudiofileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(item: Audiofile, holder: ViewHolder, payloads: MutableList<Any>) {
        holder.nameFile.text = item.nameFile
    }
}