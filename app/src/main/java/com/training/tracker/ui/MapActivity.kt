package com.training.tracker.ui

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.training.tracker.R
import com.training.tracker.databinding.ActivityMapBinding
import com.training.tracker.util.PreferencesUtils.getCachedEmail
import com.training.tracker.util.PreferencesUtils.setCachedEmail
import com.training.tracker.util.RegexUtils
import com.training.tracker.viewModel.MapViewModel
import com.training.tracker.viewModel.MapViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class MapActivity : AppCompatActivity(), OnMapReadyCallback {


    private val mapViewModel: MapViewModel by viewModels { MapViewModelFactory(getCachedEmail()) }
    private lateinit var map: GoogleMap
    private lateinit var usersAdapter: UsersOnMapAdapter
    private lateinit var binding: ActivityMapBinding
    private var isSignedIn: Boolean by Delegates.observable(false) { _, oldV, newV ->
        if (oldV != newV) updateLoginButton(isSignedIn)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mapToolbar)
        updateLoginButton(isSignedIn)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        usersAdapter = UsersOnMapAdapter(UserMapUI(googleMap, this))
        binding.btSignInAndOut.visibility = View.VISIBLE

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mapViewModel.stateFlow.collect { (isSignedIn, userList) ->
                        this@MapActivity.isSignedIn = isSignedIn
                        usersAdapter.updateUserList(userList)
                    }
                }
                launch {
                    mapViewModel.errorSharedFlow.collect { message ->
                        showErrorMessage(message)
                        binding.btSignInAndOut.isEnabled = true
                    }
                }
            }
        }
    }

    private val inputEmailDialog: AlertDialog by lazy {
        val input = EditText(this)
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.setHint(R.string.sign_in_dialog_hint)
        getCachedEmail()?.let { input.setText(it) }

        AlertDialog.Builder(this)
            .setTitle(R.string.sign_in_dialog_title)
            .setView(input)
            .setPositiveButton(R.string.sign_in_dialog_btn_positive) { _, _ ->
                val email = input.text.toString()
                if (RegexUtils.emailRegex.matches(email)) {
                    setCachedEmail(email)
                    mapViewModel.signIn(email)
                } else {
                    showErrorMessage(R.string.sign_in_dialog_error_invalid_email)
                    binding.btSignInAndOut.isEnabled = true
                }
            }
            .setNegativeButton(R.string.sign_in_dialog_btn_negative) { _, _ ->
                binding.btSignInAndOut.isEnabled = true
            }
            .setOnDismissListener { getCachedEmail()?.let { input.setText(it) } }
            .setCancelable(false)
            .create()
    }

    private fun updateLoginButton(signedIn: Boolean) {
        binding.btSignInAndOut.setText(
            if (signedIn) R.string.map_button_sign_out
            else R.string.map_button_sign_in
        )
        binding.btSignInAndOut.isEnabled = true
        binding.btSignInAndOut.setOnClickListener { btn ->
            btn.isEnabled = false
            if (signedIn) {
                mapViewModel.signOut()
                setCachedEmail(null)
            } else {
                inputEmailDialog.show()
            }
        }
    }

    private fun showErrorMessage(@StringRes stringRes: Int) {
        showToast(getString(stringRes))
    }

    private fun showErrorMessage(string: String) {
        showToast(string)
    }

    private fun showToast(string: String) {
        Toast.makeText(this@MapActivity, string, Toast.LENGTH_SHORT).show()
    }
}