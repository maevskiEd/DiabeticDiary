package ed.maevski.diabeticdiary.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import ed.maevski.diabeticdiary.R
import ed.maevski.diabeticdiary.databinding.ActivityMainBinding
import ed.maevski.diabeticdiary.view.fragments.AudioJournalFragment
import ed.maevski.diabeticdiary.view.fragments.BloodSugarLevelsFragment
import ed.maevski.diabeticdiary.view.fragments.FoodFragment
import ed.maevski.diabeticdiary.view.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_placeholder, AudioJournalFragment())
            .addToBackStack(null)
            .commit()
        initMenu()
    }

    private fun changeFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_placeholder, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private fun checkFragmentExistence(tag: String): Fragment? =
        supportFragmentManager.findFragmentByTag(tag)

    private fun initMenu() {
        binding.bottomNavigation.selectedItemId = R.id.audio_journal

        binding.bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {
                R.id.blood_sugar_levels -> {
                    val tag = "blood_sugar_levels"
                    val fragment = checkFragmentExistence(tag)

                    changeFragment(fragment ?: BloodSugarLevelsFragment(), tag)
                    true
                }

                R.id.food -> {
                        val tag = "food"
                        val fragment = checkFragmentExistence(tag)
                        changeFragment(fragment ?: FoodFragment(), tag)

                    true
                }

                R.id.audio_journal -> {
                    val tag = "audio_journal"
                    val fragment = checkFragmentExistence(tag)
                    changeFragment(fragment ?: AudioJournalFragment(), tag)

                    true
                }

                R.id.settings -> {
                    val tag = "settings"
                    val fragment = checkFragmentExistence(tag)
                    changeFragment(fragment ?: SettingsFragment(), tag)
                    true
                }

                else -> false
            }
        }
    }
}