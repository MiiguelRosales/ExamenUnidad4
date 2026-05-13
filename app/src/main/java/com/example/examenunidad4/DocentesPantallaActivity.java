package com.example.examenunidad4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import android.content.ContentValues;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class DocentesPantallaActivity extends AppCompatActivity {

    private TextView tvNombre, tvNumero, tvDireccion;
    private Spinner spinnerMaterias;
    private TableLayout tableAlumnosBody;
    private android.widget.ScrollView scrollTableBody;
    private Button btnAgregarCalifs, btnActualizarCalifs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docentes_pantalla);

        // El botón de regresar fue eliminado del layout; no es necesario manejarlo aquí.

        tvNombre = findViewById(R.id.tvDocenteNombre);
        tvNumero = findViewById(R.id.tvDocenteNumero);
        tvDireccion = findViewById(R.id.tvDocenteDireccion);
        spinnerMaterias = findViewById(R.id.spinnerMaterias);
        tableAlumnosBody = findViewById(R.id.tableAlumnosBody);
        scrollTableBody = findViewById(R.id.scrollTableBody);
        // mostrar scrollbar vertical para mayor visibilidad cuando sea necesario
        scrollTableBody.setVerticalScrollBarEnabled(true);
        scrollTableBody.setScrollbarFadingEnabled(false);
        btnAgregarCalifs = findViewById(R.id.btnAgregarCalifs);
        btnActualizarCalifs = findViewById(R.id.btnActualizarCalifs);

        // Leer extras (si vienen)
        String nombre = getIntent().getStringExtra("docente_nombre");
        String num = getIntent().getStringExtra("docente_numempleado");
        String dir = getIntent().getStringExtra("docente_direccion");

        if (nombre != null && !nombre.isEmpty()) tvNombre.setText("Nombre: " + nombre);
        if (num != null && !num.isEmpty()) tvNumero.setText("Número de empleado: " + num);
        if (dir != null && !dir.isEmpty()) tvDireccion.setText("Dirección: " + dir);

        // Cargar materias desde la BD (si hay) o usar lista por defecto
        AdminSqLite admin = new AdminSqLite(this, "administracion", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        ArrayList<String> materias = new ArrayList<>();
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, materias);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMaterias.setAdapter(adapter);

        spinnerMaterias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = materias.get(position);
                populateTableForMateria(db, selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tableAlumnosBody.removeAllViews();
            }
        });

        btnAgregarCalifs.setOnClickListener(v -> saveOrUpdateGrades(db, false));
        btnActualizarCalifs.setOnClickListener(v -> saveOrUpdateGrades(db, true));
    }

    // if updateExisting==false -> only INSERT when not exists; if true -> INSERT or UPDATE
    private void saveOrUpdateGrades(SQLiteDatabase db, boolean updateExisting) {
        String materia = spinnerMaterias.getSelectedItem() != null ? spinnerMaterias.getSelectedItem().toString() : null;
        if (materia == null) {
            Toast.makeText(this, "Seleccione una materia", Toast.LENGTH_SHORT).show();
            return;
        }

        // obtener claveMateria
        int claveMateria = -1;
        Cursor cm = db.rawQuery("SELECT claveMateria FROM Materias WHERE nombreMat = ?", new String[]{materia});
        if (cm != null) {
            if (cm.moveToFirst()) {
                claveMateria = cm.getInt(0);
            }
            cm.close();
        }

        if (claveMateria == -1) {
            Toast.makeText(this, "No se encontró clave de materia", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear tabla Calificaciones si no existe
        db.execSQL("CREATE TABLE IF NOT EXISTS Calificaciones(id INTEGER PRIMARY KEY AUTOINCREMENT, numcontrol INTEGER, claveMateria INTEGER, calificacion REAL, UNIQUE(numcontrol, claveMateria))");

        int childCount = tableAlumnosBody.getChildCount();
        int inserted = 0, updated = 0, skipped = 0;

        db.beginTransaction();
        try {
            for (int i = 0; i < childCount; i++) {
                TableRow row = (TableRow) tableAlumnosBody.getChildAt(i);
                if (row.getChildCount() < 2) continue;
                TextView tvName = (TextView) row.getChildAt(0);
                View gradeView = row.getChildAt(1);
                String nombreAlum = tvName.getText().toString();
                String gradeStr = "";
                if (gradeView instanceof EditText) {
                    gradeStr = ((EditText) gradeView).getText().toString().trim();
                } else if (gradeView instanceof TextView) {
                    gradeStr = ((TextView) gradeView).getText().toString().trim();
                }

                if (gradeStr.isEmpty() || gradeStr.equals("--")) {
                    skipped++;
                    continue;
                }

                double calif;
                try { calif = Double.parseDouble(gradeStr); } catch (NumberFormatException e) { skipped++; continue; }

                // Obtener numcontrol por nombre (si existe)
                int numcontrol = -1;
                Cursor c = db.rawQuery("SELECT numcontrol FROM Alumnos WHERE nombrealum = ?", new String[]{nombreAlum});
                if (c != null) {
                    if (c.moveToFirst()) numcontrol = c.getInt(0);
                    c.close();
                }

                if (numcontrol == -1) {
                    skipped++;
                    continue;
                }

                // check exists
                boolean exists = false;
                Cursor ex = db.rawQuery("SELECT id FROM Calificaciones WHERE numcontrol = ? AND claveMateria = ?", new String[]{String.valueOf(numcontrol), String.valueOf(claveMateria)});
                if (ex != null) {
                    if (ex.moveToFirst()) exists = true;
                    ex.close();
                }

                if (!exists && !updateExisting) {
                    ContentValues vals = new ContentValues();
                    vals.put("numcontrol", numcontrol);
                    vals.put("claveMateria", claveMateria);
                    vals.put("calificacion", calif);
                    long res = db.insertWithOnConflict("Calificaciones", null, vals, SQLiteDatabase.CONFLICT_IGNORE);
                    if (res != -1) inserted++; else skipped++;
                } else {
                    // update or insert
                    ContentValues vals = new ContentValues();
                    vals.put("numcontrol", numcontrol);
                    vals.put("claveMateria", claveMateria);
                    vals.put("calificacion", calif);
                    long res = db.insertWithOnConflict("Calificaciones", null, vals, SQLiteDatabase.CONFLICT_REPLACE);
                    if (res != -1) updated++; else skipped++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        String msg = "Insertados: " + inserted + ", Actualizados: " + updated + ", Omitidos: " + skipped;
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void populateTableForMateria(SQLiteDatabase db, String materia) {
        tableAlumnosBody.removeAllViews();

        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(8,8,8,8);

        // Obtener alumnos (a falta de vínculo por materia, mostramos los registrados)
        Cursor cur = db.rawQuery("SELECT nombrealum FROM Alumnos", null);
        int addedRows = 0;
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String nombreAlum = cur.getString(0);
                    TableRow row = new TableRow(this);
                    TextView tvName = new TextView(this);
                    tvName.setText(nombreAlum);
                    tvName.setLayoutParams(lp);
                    tvName.setTextColor(ContextCompat.getColor(this, R.color.table_text));

                    EditText etCalif = new EditText(this);
                    etCalif.setHint("--");
                    etCalif.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etCalif.setLayoutParams(lp);
                    etCalif.setTextColor(ContextCompat.getColor(this, R.color.table_text));
                    etCalif.setHintTextColor(ContextCompat.getColor(this, R.color.hint));
                    etCalif.setBackground(null);

                    row.addView(tvName);
                    row.addView(etCalif);
                    tableAlumnosBody.addView(row);
                    addedRows++;
                } while (cur.moveToNext());
            }
            cur.close();
        }

        // Mostrar scrollbar solo si hay más de `targetRows` filas reales
        int targetRows = 7;
        final int currentRows = tableAlumnosBody.getChildCount();
        if (currentRows <= targetRows) {
            // mostrar todo: altura automática (sin scroll)
            scrollTableBody.post(() -> {
                android.view.ViewGroup.LayoutParams lpScroll = scrollTableBody.getLayoutParams();
                lpScroll.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                scrollTableBody.setLayoutParams(lpScroll);
            });
        } else {
            // Si hay más de targetRows filas, limitar la altura del ScrollView para mostrar 'targetRows' y permitir scroll
            final int visibleRows = targetRows;
            scrollTableBody.post(() -> {
                if (tableAlumnosBody.getChildCount() > 0) {
                    View firstRow = tableAlumnosBody.getChildAt(0);
                    int rowHeight = firstRow.getHeight();
                    if (rowHeight == 0) {
                        float density = getResources().getDisplayMetrics().density;
                        rowHeight = (int) (48 * density + 0.5f);
                    }
                    android.view.ViewGroup.LayoutParams lpScroll = scrollTableBody.getLayoutParams();
                    lpScroll.height = rowHeight * visibleRows + (int)(8 * getResources().getDisplayMetrics().density);
                    scrollTableBody.setLayoutParams(lpScroll);
                }
            });
            scrollTableBody.setVerticalScrollBarEnabled(true);
        }
    }
}
