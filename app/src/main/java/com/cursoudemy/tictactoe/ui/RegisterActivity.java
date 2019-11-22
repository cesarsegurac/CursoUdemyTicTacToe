package com.cursoudemy.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.cursoudemy.tictactoe.R;
import com.cursoudemy.tictactoe.app.Constantes;
import com.cursoudemy.tictactoe.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    EditText etName, etEmail, etPassword;
    Button btnRegister;
    ScrollView formRegister;
    ProgressBar pbRegister;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String name, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViews();
        changeFormVisibility(true);
        attachevents();
    }

    private void attachevents() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etName.getText().toString();
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();

                if(name.isEmpty()) {
                    etName.setError("El nombre es requerido");
                }else if(email.isEmpty()) {
                    etEmail.setError("El email es requerido");
                } else if(password.isEmpty()) {
                    etPassword.setError("La contraseña es requerida");
                } else{
                    createUser();
                }

            }
        });
    }

    private void createUser() {
        changeFormVisibility(false);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }else{
                            Toast.makeText(getApplication(), "Error en el proceso de registro", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            // Guardar datos en Firestore
            User newUser = new User(name, 0, 0);

            db.collection(Constantes.DOC_USUARIO)
                    .document(user.getUid())
                    .set(newUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Ir a la siguiente pantalla
                            finish();
                            Intent i = new Intent(RegisterActivity.this, FindGameActivity.class);
                            startActivity(i);
                        }
                    });

        }else{
            changeFormVisibility(true);
            etPassword.setError("Los datos no son correctos");
            etName.requestFocus();
        }
    }

    private void findViews() {
        etName = findViewById(R.id.editTextName);
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        formRegister = findViewById(R.id.formRegister);
        pbRegister = findViewById(R.id.progressBarRegister);
        btnRegister = findViewById(R.id.buttonRegistro);
    }

    private void changeFormVisibility(boolean showForm) {
        pbRegister.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formRegister.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }
}
