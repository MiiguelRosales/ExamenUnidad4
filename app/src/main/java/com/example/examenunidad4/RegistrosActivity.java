package com.example.examenunidad4;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registros);

        Button btnRegresar = findViewById(R.id.btnRegresar);
        if (btnRegresar != null) {
            btnRegresar.setOnClickListener(v -> finish());
        }
    }
}
