package com.bayutb.gombti.ui.register

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bayutb.gombti.R
import com.bayutb.gombti.api.ApiConfig
import com.bayutb.gombti.api.responses.RegisterResponse
import com.bayutb.gombti.databinding.ActivityRegisterBinding
import com.bayutb.gombti.ui.login.LoginActivity
import com.bayutb.gombti.ui.mbti.MbtiActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var datePickerDialog : DatePickerDialog
    private var userId = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        datePickerDialog = initDatePicker(binding.etBirthDate)

        binding.apply {
            etPassword.addTextChangedListener {
                checkForm(etPassword, etPassword.text.toString())
            }
            etEmailAddress.addTextChangedListener {
                checkForm(etEmailAddress, etEmailAddress.text.toString())
            }
            etBirthDate.showSoftInputOnFocus = false
            etBirthDate.setOnClickListener {
                datePickerDialog.show()
            }

            goLogin.setOnClickListener {
                Intent(this@RegisterActivity, LoginActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            }

            btnRegister.setOnClickListener {
                val name = etFullName.text.toString()
                val email = etEmailAddress.text.toString()
                val password = etPassword.text.toString()
                val rePassword = etRePassword.text.toString()
                val gender = when (rgGender.checkedRadioButtonId) {
                    rbMale.id -> "Male"
                    rbFemale.id -> "Female"
                    else -> "Unresolved"
                }
                val birthDate = etBirthDate.text.toString()
                registerUser(name, email, password, rePassword, gender, birthDate)
            }
        }
    }

    private fun registerUser(name: String, email:String, password: String, rePassword: String, gender: String, birthDate: String) {
        if (name == "" || email == "" || password == "" || gender == "Unresolved" || birthDate == "") {
            Toast.makeText(this@RegisterActivity, getString(R.string.dialog_invalid_register), Toast.LENGTH_SHORT).show()
        } else if  (password != rePassword) {
            Toast.makeText(this@RegisterActivity, getString(R.string.dialog_invalid_register_password), Toast.LENGTH_SHORT).show()
        } else {
            isLoading(true)
            ApiConfig.getInstance().registerUser(name, email, password, gender, birthDate).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()?.data != null) {
                            userId.add(0, response.body()!!.data.userId)
                            registerAlert(this@RegisterActivity, getString(R.string.dialog_register_complete_message))
                            Log.d("Success : ", response.body()!!.data.toString())
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, getString(R.string.toast_register_email_exists), Toast.LENGTH_SHORT).show()
                    }
                    Log.d("Data : ", response.body().toString())
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, getString(R.string.toast_connection_error), Toast.LENGTH_SHORT).show()
                    Log.d("Failed: ", t.message.toString())
                }

            })
            isLoading(false)
        }
    }

    private fun initDatePicker(etBirthDate: EditText): DatePickerDialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this@RegisterActivity,
            { _, _year, _monthOfYear, _dayOfMonth ->
                // on below line we are setting
                // date to our edit text.
                val dat =
                    (_year.toString() + "-" + (_monthOfYear + 1) + "-" + _dayOfMonth.toString())
                etBirthDate.setText(dat)
            },
            year,
            month,
            day
        )
        return datePickerDialog
    }

    private fun isLoading(status: Boolean) {
        binding.btnRegister.isEnabled = !status
    }

    private fun checkForm(form :EditText, value: String) {

        if (form == binding.etEmailAddress && !isValidEmail(value)) {
            form.error = "Email tidak valid"
            Log.d("checkForm : ", value)
        } else if (form == binding.etPassword && isPasswordGood(value)) {
            form.error = "Password harus lebih dari 8 digit"
        }
        Log.d("Error Status : ", "${form.text} from ${form.id} is ${isValidEmail(value)}")
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    private fun isPasswordGood(target: CharSequence): Boolean {
        return target.length <= 8
    }
}

private fun registerAlert(context: Context, message: String) {
    val alertDialog = AlertDialog.Builder(context)
    alertDialog.setMessage(message)
    alertDialog.setPositiveButton(context.getString(R.string.dialog_register_complete_positive)) { dialogInterface: DialogInterface, _: Int ->
        val intent = Intent(context, MbtiActivity::class.java)
        (context as RegisterActivity).startActivity(intent)
        context.finish()
        dialogInterface.dismiss()
    }

    val dialog = alertDialog.create()
    dialog.show()
}
