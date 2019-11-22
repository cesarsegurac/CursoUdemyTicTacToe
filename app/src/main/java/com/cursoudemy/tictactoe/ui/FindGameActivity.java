package com.cursoudemy.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cursoudemy.tictactoe.R;
import com.cursoudemy.tictactoe.app.Constantes;
import com.cursoudemy.tictactoe.model.Jugada;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class FindGameActivity extends AppCompatActivity {

    private TextView tvLoadingMessage;
    private ProgressBar progressBar;
    private ScrollView layoutProgressBar, layoutMenuJuego;
    private Button btnJugar, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String uid;
    private String jugadaId;
    private ListenerRegistration listenerRegistration = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);

        findViews();
        initProgressBar();
        initFirebase();
        attachEvents();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
    }

    private void attachEvents() {
        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMenuVisibility(false);
                buscarJugadaLibre();
            }
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void buscarJugadaLibre() {
        tvLoadingMessage.setText("Buscando partidas disponibles...");
        db.collection(Constantes.DOC_JUGADA)
                .whereEqualTo("jugadorDosId", "")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0){
                            crearNuevaJugada();
                        }else{
                            DocumentSnapshot docJugada = task.getResult().getDocuments().get(0);
                            jugadaId = docJugada.getId();
                            Jugada jugada = docJugada.toObject(Jugada.class);
                            jugada.setJugadorDosId(uid);

                            db.collection(Constantes.DOC_JUGADA)
                                    .document(jugadaId)
                                    .set(jugada)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startGame();
                                        }
                                    })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        changeMenuVisibility(true);
                                        Toast.makeText(getApplication(), "Error al entrar a la partida", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        }
                    }
                });
    }

    private void crearNuevaJugada() {
        tvLoadingMessage.setText("Creando una jugada nueva...");
        Jugada nuevaJugada = new Jugada(uid);
        db.collection(Constantes.DOC_JUGADA)
                .add(nuevaJugada)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        jugadaId = documentReference.getId();
                        esperarJugador();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                changeMenuVisibility(true);
                Toast.makeText(getApplication(), "Error al crear la partida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void esperarJugador() {
        tvLoadingMessage.setText("Esperando a otro jugador...");
        listenerRegistration = db.collection(Constantes.DOC_JUGADA)
                .document(jugadaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(!documentSnapshot.get("jugadorDosId").equals("")){
                            tvLoadingMessage.setText("iniciando la partida...");
                            final Handler handler = new Handler();
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    startGame();
                                }
                            };
                            handler.postDelayed(r, 1500);
                        }
                    }
                });
    }

    private void startGame() {
        if(listenerRegistration != null){
            listenerRegistration.remove();
        }
        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra(Constantes.EXTRA_JUGADA_ID, jugadaId);
        startActivity(i);
    }

    private void initProgressBar() {
        progressBar.setIndeterminate(true);
        tvLoadingMessage.setText("Cargando...");
        changeMenuVisibility(true);
    }

    private void changeMenuVisibility(boolean showMenu) {
        layoutProgressBar.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        layoutMenuJuego.setVisibility(showMenu ? View.VISIBLE : View.GONE);
    }

    private void findViews() {
        tvLoadingMessage = findViewById(R.id.textViewLoading);
        progressBar = findViewById(R.id.progressBarLoading);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        layoutMenuJuego = findViewById(R.id.menuJuego);
        btnJugar = findViewById(R.id.buttonJugar);
        btnRanking = findViewById(R.id.buttonRanking);
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeMenuVisibility(true);
    }
}
