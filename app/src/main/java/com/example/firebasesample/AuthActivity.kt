package com.example.firebasesample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasesample.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (MyApplication.checkAuth()) {
            changeVisibility("login")
        } else {
            changeVisibility("logout")
        }

        binding.logoutBtn.setOnClickListener {
            // 로그아웃
            MyApplication.auth.signOut()
            MyApplication.email = null
            changeVisibility("logout")
        }

        binding.goSignInBtn.setOnClickListener {
            changeVisibility("signin")
        }

        binding.googleLoginBtn.setOnClickListener {
            // 구글 로그인
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            // 구글의 인증관리 앱 실행
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            startActivityForResult(signInIntent, 10)
        }

        binding.signBtn.setOnClickListener {
            // 이메일, 비밀번호 회원가입
            val email: String = binding.authEmailEditView.text.toString()
            val password: String = binding.authPasswordEditView.text.toString()
            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if (task.isSuccessful) {
                        // 비밀번호는 최소 6자 이상
                        // send email
                        MyApplication.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { sendTask ->
                                if (sendTask.isSuccessful) {
                                    Toast.makeText(baseContext, "회원가입에 성공하였습니다. 전송된 메일을 확인해 주세요.",
                                        Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                } else {
                                    Toast.makeText(baseContext, "메일 전송 실패",
                                        Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }
                            }
                    } else {
                        Toast.makeText(baseContext, "회원가입 실패",
                            Toast.LENGTH_SHORT).show()
                        changeVisibility("logout")
                    }
                }
        }

        binding.loginBtn.setOnClickListener {
            // 이메일, 비밀번호 로그인
            val email: String = binding.authEmailEditView.text.toString()
            val password: String = binding.authPasswordEditView.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if (task.isSuccessful) {
                        if (MyApplication.checkAuth()) {
                            // 로그인 성공
                            MyApplication.email = email
                            changeVisibility("login")
                        } else {
                            // 발송된 메일로 인증 확인을 하지 않은 경우
                            Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.",
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "로그인 실패",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 구글 로그인 결과 처리
        if (10 == requestCode) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                MyApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // 구글 로그인 성공
                            MyApplication.email = account.email
                            changeVisibility("login")
                        } else {
                            // 구글 로그인 실패
                            changeVisibility("logout")
                        }
                    }
            } catch (e: ApiException) {
                changeVisibility("logout")
            }
        }
    }


    fun changeVisibility(mode: String){
        if ("login" === mode) {
            binding.run {
                authMainTextView.text = "${MyApplication.email} 님 반갑습니다."
                logoutBtn.visibility = View.VISIBLE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                authEmailEditView.visibility = View.GONE
                authPasswordEditView.visibility = View.GONE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.GONE
            }
        } else if ("logout" === mode) {
            binding.run {
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
            }
        } else if ("signin" === mode) {
            binding.run {
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
            }
        }
    }
}