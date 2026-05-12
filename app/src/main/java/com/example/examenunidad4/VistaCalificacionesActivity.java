package com.example.examenunidad4;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class VistaCalificacionesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_calificaciones);

        Button btnRegresar = findViewById(R.id.btnRegresarCalificaciones);
        if (btnRegresar != null) {
            btnRegresar.setOnClickListener(v -> finish());
        }
    }
}
