package com.example.snapshots

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.Fragments.AddFragment
import com.example.snapshots.Fragments.HomeFragment
import com.example.snapshots.Fragments.ProfileFragment
import com.example.snapshots.databinding.ActivityMainBinding
import com.example.snapshots.utils.HomeAux
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupAuth()
        setupBottomNav()
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())

    private fun setupAuth() {
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_foreground) // Set logo drawable
            .setTheme(R.style.FirebaseUI_DefaultMaterialTheme) // Set theme
            .setTosAndPrivacyPolicyUrls(
                "https://example.com/terms.html",
                "https://example.com/privacy.html")
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Toast.makeText(this,"Bienvenido ${user.displayName}!!",Toast.LENGTH_SHORT).show()
            }
        } else {
            if (response == null) {
                finish()
            }
//            Toast.makeText(this, response?.error?.errorCode.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav () {
        mFragmentManager = supportFragmentManager
        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()
        mActiveFragment = homeFragment
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragments, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragments, addFragment, AddFragment::class.java.name)
            .hide(addFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragments, homeFragment, HomeFragment::class.java.name)
            .commit()

        mBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.action_item_home -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_item_add -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_item_profile -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }
                else -> false
            }
        }

        mBinding.bottomNav.setOnItemReselectedListener {
            when(it.itemId) {
                R.id.action_item_home -> {
                    val mFragment = HomeFragment.newInstance()
                    (mFragment as HomeAux).goToTop()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener { mAuthListener }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener { mAuthListener }
    }
}