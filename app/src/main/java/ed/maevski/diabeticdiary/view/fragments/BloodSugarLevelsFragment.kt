package ed.maevski.diabeticdiary.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ed.maevski.diabeticdiary.databinding.FragmentBloodSugarLevelsBinding

class BloodSugarLevelsFragment : Fragment()  {
    private var _binding: FragmentBloodSugarLevelsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBloodSugarLevelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}