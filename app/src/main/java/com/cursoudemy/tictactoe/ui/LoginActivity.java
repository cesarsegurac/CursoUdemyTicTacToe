package com.cursoudemy.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import com.cursoudemy.tictactoe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ScrollView formLogin;
    private ProgressBar pbLogin;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    boolean tryLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        findViews();
        changeFormVisibility(true);
        attachEvents();
    }

    private void attachEvents() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();

                if(email.isEmpty()) {
                    etEmail.setError("El email es requerido");
                } else if(password.isEmpty()) {
                    etPassword.setError("La contraseña es requerida");
                } else{
                    changeFormVisibility(false);
                    loginUser();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        tryLogin = true;
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }else{
                            Log.w("TAG","LoginError", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null){
            // Guardar datos en Firestore

            // Ir a la siguiente pantalla
            Intent i = new Intent(LoginActivity.this, FindGameActivity.class);
            startActivity(i);
        }else{
            changeFormVisibility(true);
            if(tryLogin) {
                etPassword.setError("Los datos no son correctos");
                etPassword.requestFocus();
            }
        }
    }

    private void changeFormVisibility(boolean showForm) {
        pbLogin.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formLogin.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }

    private void findViews() {
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        formLogin = findViewById(R.id.formLogin);
        pbLogin = findViewById(R.id.progressBarLogin);
        btnRegister = findViewById(R.id.buttonRegistro);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // comprobar si el usuario esta logueado
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }
}
