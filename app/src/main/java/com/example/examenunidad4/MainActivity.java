package com.example.examenunidad4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;

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

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(MainActivity.this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (role.equalsIgnoreCase("Docente")) {
                autenticarDocente(input);
            } else if (role.equalsIgnoreCase("Alumno")) {
                autenticarAlumno(input);
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

    private void autenticarAlumno(String numcontrol) {
        AdminSqLite admin = new AdminSqLite(this, "administracion", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT nombrealum FROM Alumnos WHERE numcontrol = ?", new String[]{numcontrol});
            if (cursor.moveToFirst()) {
                Intent intent = new Intent(MainActivity.this, VistaCalificacionesActivity.class);
                intent.putExtra("alumno_numcontrol", numcontrol);
                intent.putExtra("alumno_nombre", cursor.getString(0));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Número de control no registrado", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private void autenticarDocente(String numempleado) {
        AdminSqLite admin = new AdminSqLite(this, "administracion", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT nombreDoc, Direccion FROM Docentes WHERE numEmpleado = ?", new String[]{numempleado});
            if (cursor.moveToFirst()) {
                Intent intent = new Intent(MainActivity.this, DocentesPantallaActivity.class);
                intent.putExtra("docente_numempleado", numempleado);
                intent.putExtra("docente_nombre", cursor.getString(0));
                intent.putExtra("docente_direccion", cursor.getString(1));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Número de empleado no registrado", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }
}