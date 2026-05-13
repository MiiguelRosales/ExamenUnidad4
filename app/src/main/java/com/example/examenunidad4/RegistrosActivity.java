package com.example.examenunidad4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RegistrosActivity extends AppCompatActivity {

    private Spinner spinnerMateria1, spinnerMateria2;
    private Spinner spinnerDocente1, spinnerDocente2;
    private boolean ignoreStudentSpinnerChange = false;
    private boolean ignoreDocenteSpinnerChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registros);

        // El botón "Regresar" fue eliminado del layout; no requiere manejo aquí.

        spinnerMateria1 = findViewById(R.id.spinnerMateria1);
        spinnerMateria2 = findViewById(R.id.spinnerMateria2);
        spinnerDocente1 = findViewById(R.id.spinnerDocente1);
        spinnerDocente2 = findViewById(R.id.spinnerDocente2);

        // Cargar materias desde la BD
        AdminSqLite admin = new AdminSqLite(this, "administracion", null, 1);
        SQLiteDatabase db = admin.getReadableDatabase();

        final ArrayList<String> materias = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT nombreMat FROM Materias", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    materias.add(c.getString(0));
                } while (c.moveToNext());
            }
            c.close();
        }

        if (materias.isEmpty()) {
            materias.add("Matemáticas");
            materias.add("Historia");
            materias.add("Física");
        }

        // Añadir opción inicial para permitir "ninguna" selección
        final ArrayList<String> options = new ArrayList<>();
        options.add("Ninguna");
        options.addAll(materias);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        if (spinnerMateria1 != null) spinnerMateria1.setAdapter(adapter);
        if (spinnerMateria2 != null) spinnerMateria2.setAdapter(adapter);
        if (spinnerDocente1 != null) spinnerDocente1.setAdapter(adapter);
        if (spinnerDocente2 != null) spinnerDocente2.setAdapter(adapter);

        // Por defecto, seleccionar "Ninguna" en los cuatro spinners
        if (spinnerMateria1 != null) spinnerMateria1.setSelection(0);
        if (spinnerMateria2 != null) spinnerMateria2.setSelection(0);
        if (spinnerDocente1 != null) spinnerDocente1.setSelection(0);
        if (spinnerDocente2 != null) spinnerDocente2.setSelection(0);

        AdapterView.OnItemSelectedListener studentListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignoreStudentSpinnerChange) return;
                try {
                    // Si alguna selección es "Ninguna" (posición 0), permitirlo
                    int pos1 = spinnerMateria1 != null ? spinnerMateria1.getSelectedItemPosition() : 0;
                    int pos2 = spinnerMateria2 != null ? spinnerMateria2.getSelectedItemPosition() : 0;

                    if (parent == spinnerMateria1 && spinnerMateria2 != null) {
                        if (pos1 != 0 && pos1 == pos2) {
                            ignoreStudentSpinnerChange = true;
                            // buscar una posición diferente que no sea 0 (Ninguna) y distinta de pos1
                            int newPos = findDifferentPosition(options, pos1);
                            spinnerMateria2.setSelection(newPos);
                            Toast.makeText(RegistrosActivity.this, "Las materias del alumno deben ser diferentes", Toast.LENGTH_SHORT).show();
                            ignoreStudentSpinnerChange = false;
                        }
                    } else if (parent == spinnerMateria2 && spinnerMateria1 != null) {
                        if (pos2 != 0 && pos2 == pos1) {
                            ignoreStudentSpinnerChange = true;
                            int newPos = findDifferentPosition(options, pos2);
                            spinnerMateria1.setSelection(newPos);
                            Toast.makeText(RegistrosActivity.this, "Las materias del alumno deben ser diferentes", Toast.LENGTH_SHORT).show();
                            ignoreStudentSpinnerChange = false;
                        }
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        AdapterView.OnItemSelectedListener docenteListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignoreDocenteSpinnerChange) return;
                try {
                    int dpos1 = spinnerDocente1 != null ? spinnerDocente1.getSelectedItemPosition() : 0;
                    int dpos2 = spinnerDocente2 != null ? spinnerDocente2.getSelectedItemPosition() : 0;

                    if (parent == spinnerDocente1 && spinnerDocente2 != null) {
                        if (dpos1 != 0 && dpos1 == dpos2) {
                            ignoreDocenteSpinnerChange = true;
                            int newPos = findDifferentPosition(options, dpos1);
                            spinnerDocente2.setSelection(newPos);
                            Toast.makeText(RegistrosActivity.this, "Las materias del docente deben ser diferentes", Toast.LENGTH_SHORT).show();
                            ignoreDocenteSpinnerChange = false;
                        }
                    } else if (parent == spinnerDocente2 && spinnerDocente1 != null) {
                        if (dpos2 != 0 && dpos2 == dpos1) {
                            ignoreDocenteSpinnerChange = true;
                            int newPos = findDifferentPosition(options, dpos2);
                            spinnerDocente1.setSelection(newPos);
                            Toast.makeText(RegistrosActivity.this, "Las materias del docente deben ser diferentes", Toast.LENGTH_SHORT).show();
                            ignoreDocenteSpinnerChange = false;
                        }
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        if (spinnerMateria1 != null) spinnerMateria1.setOnItemSelectedListener(studentListener);
        if (spinnerMateria2 != null) spinnerMateria2.setOnItemSelectedListener(studentListener);
        if (spinnerDocente1 != null) spinnerDocente1.setOnItemSelectedListener(docenteListener);
        if (spinnerDocente2 != null) spinnerDocente2.setOnItemSelectedListener(docenteListener);
    }

    private int findDifferentPosition(ArrayList<String> options, int forbiddenPos) {
        if (options == null || options.size() <= 1) return 0;
        for (int i = 1; i < options.size(); i++) {
            if (i != forbiddenPos) return i;
        }
        return 0;
    }
}
