package com.example.examenunidad4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.text.InputFilter;
import android.text.Spanned;
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

        // Cargar solo las materias asignadas al docente
        AdminSqLite admin = new AdminSqLite(this, "administracion", null, 2);
        SQLiteDatabase db = admin.getWritableDatabase();

        ArrayList<String> materias = obtenerMateriasDocente(db, num);

        if (materias.isEmpty()) {
            materias.add("Ninguna");
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

    private ArrayList<String> obtenerMateriasDocente(SQLiteDatabase db, String numEmpleado) {
        ArrayList<String> materias = new ArrayList<>();
        if (numEmpleado == null || numEmpleado.trim().isEmpty()) {
            return materias;
        }

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT materia1, materia2 FROM Docentes WHERE numEmpleado = ?",
                    new String[]{numEmpleado}
            );

            if (cursor.moveToFirst()) {
                String materia1 = cursor.getString(0);
                String materia2 = cursor.getString(1);

                if (materia1 != null && !materia1.trim().isEmpty()) {
                    materias.add(materia1.trim());
                }
                if (materia2 != null && !materia2.trim().isEmpty() && !materia2.trim().equalsIgnoreCase(materia1 != null ? materia1.trim() : "")) {
                    materias.add(materia2.trim());
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return materias;
    }

    // if updateExisting==false -> only INSERT when not exists; if true -> INSERT or UPDATE
    private void saveOrUpdateGrades(SQLiteDatabase db, boolean updateExisting) {
        String materia = spinnerMaterias.getSelectedItem() != null ? spinnerMaterias.getSelectedItem().toString() : null;
        if (materia == null || "Ninguna".equalsIgnoreCase(materia.trim())) {
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
                Object tag = row.getTag();
                int numcontrol = -1;
                int existingCalifId = -1;
                if (tag instanceof Object[]) {
                    Object[] arr = (Object[]) tag;
                    if (arr.length > 0 && arr[0] instanceof Integer) numcontrol = (Integer) arr[0];
                    if (arr.length > 1 && arr[1] instanceof Integer) existingCalifId = (Integer) arr[1];
                } else if (tag instanceof Integer) {
                    numcontrol = (Integer) tag;
                }
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

                // enforce allowed range 1..100
                if (calif < 1.0 || calif > 100.0) {
                    Toast.makeText(this, "La calificación debe estar entre 1 y 100: " + nombreAlum, Toast.LENGTH_SHORT).show();
                    skipped++;
                    continue;
                }
                if (numcontrol == -1) {
                    skipped++;
                    continue;
                }

                if (existingCalifId == -1 && !updateExisting) {
                    // add new grade only when there is no existing grade for this student+materia
                    ContentValues vals = new ContentValues();
                    vals.put("numcontrol", numcontrol);
                    vals.put("claveMateria", claveMateria);
                    vals.put("calificacion", calif);
                    long res = db.insertWithOnConflict("Calificaciones", null, vals, SQLiteDatabase.CONFLICT_IGNORE);
                    if (res != -1) inserted++; else skipped++;
                } else if (existingCalifId != -1 && updateExisting) {
                    // update only existing grade
                    ContentValues vals = new ContentValues();
                    vals.put("numcontrol", numcontrol);
                    vals.put("claveMateria", claveMateria);
                    vals.put("calificacion", calif);
                    long res = db.insertWithOnConflict("Calificaciones", null, vals, SQLiteDatabase.CONFLICT_REPLACE);
                    if (res != -1) updated++; else skipped++;
                } else {
                    // either trying to add where grade exists, or trying to update where no grade exists
                    skipped++;
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

        // Obtener solo alumnos que tengan asignada la materia seleccionada
        Cursor cur = db.rawQuery(
            "SELECT numcontrol, nombrealum FROM Alumnos " +
                "WHERE materia1 = ? OR materia2 = ? " +
                "ORDER BY nombrealum",
            new String[]{materia, materia}
        );
        int addedRows = 0;
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    int numcontrol = cur.getInt(0);
                    String nombreAlum = cur.getString(1);
                    TableRow row = new TableRow(this);
                    row.setTag(numcontrol);

                    TextView tvName = new TextView(this);
                    tvName.setText(nombreAlum);
                    tvName.setLayoutParams(lp);
                    tvName.setTextColor(ContextCompat.getColor(this, R.color.table_text));

                    EditText etCalif = new EditText(this);
                    etCalif.setHint("--");
                    etCalif.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    // restrict input to numbers and decimals and enforce 1..100 range while typing
                    etCalif.setFilters(new InputFilter[]{ new RangeInputFilter(1.0, 100.0) });
                    etCalif.setLayoutParams(lp);
                    etCalif.setTextColor(ContextCompat.getColor(this, R.color.table_text));
                    etCalif.setHintTextColor(ContextCompat.getColor(this, R.color.hint));
                    etCalif.setBackground(null);

                    String calificacionActual = obtenerCalificacionActual(db, numcontrol, materia);
                    int califId = obtenerCalificacionId(db, numcontrol, materia);
                    if (calificacionActual != null) {
                        etCalif.setText(calificacionActual);
                    }

                    // store numcontrol and existing calification id (or -1) on the row for later logic
                    row.setTag(new Object[]{numcontrol, califId});

                    row.addView(tvName);
                    row.addView(etCalif);
                    tableAlumnosBody.addView(row);
                    addedRows++;
                } while (cur.moveToNext());
            }
            cur.close();
        }

        if (addedRows == 0) {
            Toast.makeText(this, "No hay alumnos asignados a esta materia", Toast.LENGTH_SHORT).show();
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

    private String obtenerCalificacionActual(SQLiteDatabase db, int numcontrol, String materia) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT c.calificacion " +
                            "FROM Calificaciones c " +
                            "INNER JOIN Materias m ON c.claveMateria = m.claveMateria " +
                            "WHERE c.numcontrol = ? AND m.nombreMat = ?",
                    new String[]{String.valueOf(numcontrol), materia}
            );
            if (cursor.moveToFirst()) {
                return String.valueOf(cursor.getDouble(0));
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private int obtenerCalificacionId(SQLiteDatabase db, int numcontrol, String materia) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT c.id " +
                            "FROM Calificaciones c " +
                            "INNER JOIN Materias m ON c.claveMateria = m.claveMateria " +
                            "WHERE c.numcontrol = ? AND m.nombreMat = ?",
                    new String[]{String.valueOf(numcontrol), materia}
            );
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return -1;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // InputFilter that constrains numeric input to a min..max range (allows intermediate typing like "12." or "")
    private static class RangeInputFilter implements InputFilter {
        private final double min;
        private final double max;

        RangeInputFilter(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                String newVal = dest.subSequence(0, dstart).toString() + source.subSequence(start, end).toString() + dest.subSequence(dend, dest.length()).toString();
                if (newVal.isEmpty() || newVal.equals(".")) {
                    // allow empty or a lone dot while typing
                    return null;
                }
                // allow only numeric input with optional single decimal point
                if (!newVal.matches("^-?\\d*(\\.\\d*)?")) return "";

                double value = Double.parseDouble(newVal);
                if (value >= min && value <= max) return null;
            } catch (NumberFormatException ignored) {
                // fall through
            }
            return ""; // reject change
        }
    }
}
