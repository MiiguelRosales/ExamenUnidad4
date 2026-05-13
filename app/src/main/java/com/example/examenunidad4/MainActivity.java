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
import android.widget.ArrayAdapter;
import android.webkit.WebView;
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

        // Cargar tarjeta SIE desde HTML
        WebView sieCardView = findViewById(R.id.sieCardView);
        sieCardView.getSettings().setJavaScriptEnabled(true);
        sieCardView.loadUrl("file:///android_asset/sie_card.html");

        EditText nombreEditText = findViewById(R.id.nombre);
        Spinner roleSpinner = findViewById(R.id.role_spinner);
        // Reiniciar adapter para usar layout con texto oscuro
        String[] roles = getResources().getStringArray(R.array.login_roles);
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, roles);
        roleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Listener para cambiar el hint dinámicamente según el rol seleccionado
        roleSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = roles[position];
                if (selectedRole.equalsIgnoreCase("Docente")) {
                    nombreEditText.setHint("Número de empleado");
                    nombreEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                } else if (selectedRole.equalsIgnoreCase("Alumno")) {
                    nombreEditText.setHint("Número de control");
                    nombreEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                } else if (selectedRole.equalsIgnoreCase("Admin")) {
                    nombreEditText.setHint("Contraseña");
                    nombreEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                nombreEditText.setText("");
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                nombreEditText.setHint("Nombre");
            }
        });

        Button loginNormal = findViewById(R.id.loginNormal);
        loginNormal.setOnClickListener(v -> {
            String role = roleSpinner.getSelectedItem() != null ? roleSpinner.getSelectedItem().toString() : "";
            String input = nombreEditText.getText().toString().trim();

            if (role.equalsIgnoreCase("Docente")) {
                Intent intent = new Intent(MainActivity.this, DocentesPantallaActivity.class);
                intent.putExtra("docente_numempleado", input);
                startActivity(intent);
            } else if (role.equalsIgnoreCase("Alumno")) {
                Intent intent = new Intent(MainActivity.this, VistaCalificacionesActivity.class);
                intent.putExtra("alumno_numcontrol", input);
                startActivity(intent);
            } else if (role.equalsIgnoreCase("Admin")) {
                if (input.equalsIgnoreCase("admin")) {
                    Intent intent = new Intent(MainActivity.this, RegistrosActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Seleccione un rol", Toast.LENGTH_SHORT).show();
            }
        });
    }
}