package com.example.examenunidad4;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText nombreEditText = findViewById(R.id.nombre);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        btnAdmin.setOnClickListener(v -> {
            String nombre = nombreEditText.getText().toString().trim();
            if (nombre.equalsIgnoreCase("admin")) {
                Intent intent = new Intent(MainActivity.this, RegistrosActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Acceso denegado: nombre incorrecto", Toast.LENGTH_SHORT).show();
            }
        });

        Spinner roleSpinner = findViewById(R.id.role_spinner);
        Button loginNormal = findViewById(R.id.loginNormal);
        loginNormal.setOnClickListener(v -> {
            String role = roleSpinner.getSelectedItem() != null ? roleSpinner.getSelectedItem().toString() : "";
            if (role.equalsIgnoreCase("Docente")) {
                Intent intent = new Intent(MainActivity.this, DocentesPantallaActivity.class);
                startActivity(intent);
            } else if (role.equalsIgnoreCase("Alumno")) {
                Intent intent = new Intent(MainActivity.this, VistaCalificacionesActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Seleccione un rol", Toast.LENGTH_SHORT).show();
            }
        });
    }
}