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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;

public class DocentesPantallaActivity extends AppCompatActivity {

    private TextView tvNombre, tvNumero, tvDireccion;
    private Spinner spinnerMaterias;
    private TableLayout tableAlumnos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docentes_pantalla);

        Button btnRegresar = findViewById(R.id.btnRegresarDocentes);
        if (btnRegresar != null) {
            btnRegresar.setOnClickListener(v -> finish());
        }

        tvNombre = findViewById(R.id.tvDocenteNombre);
        tvNumero = findViewById(R.id.tvDocenteNumero);
        tvDireccion = findViewById(R.id.tvDocenteDireccion);
        spinnerMaterias = findViewById(R.id.spinnerMaterias);
        tableAlumnos = findViewById(R.id.tableAlumnos);

        // Leer extras (si vienen)
        String nombre = getIntent().getStringExtra("docente_nombre");
        String num = getIntent().getStringExtra("docente_numempleado");
        String dir = getIntent().getStringExtra("docente_direccion");

        if (nombre != null && !nombre.isEmpty()) tvNombre.setText("Nombre: " + nombre);
        if (num != null && !num.isEmpty()) tvNumero.setText("Número de empleado: " + num);
        if (dir != null && !dir.isEmpty()) tvDireccion.setText("Dirección: " + dir);

        // Cargar materias desde la BD (si hay) o usar lista por defecto
        AdminSqLite admin = new AdminSqLite(this, "administracion", null, 1);
        SQLiteDatabase db = admin.getReadableDatabase();

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, materias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaterias.setAdapter(adapter);

        spinnerMaterias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = materias.get(position);
                populateTableForMateria(db, selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tableAlumnos.removeAllViews();
            }
        });
    }

    private void populateTableForMateria(SQLiteDatabase db, String materia) {
        tableAlumnos.removeAllViews();

        // Header
        TableRow header = new TableRow(this);
        TextView hAlumno = new TextView(this);
        hAlumno.setText("Alumno");
        hAlumno.setTypeface(null, Typeface.BOLD);
        TextView hCalif = new TextView(this);
        hCalif.setText("Calificación");
        hCalif.setTypeface(null, Typeface.BOLD);

        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(8,8,8,8);
        hAlumno.setLayoutParams(lp);
        hCalif.setLayoutParams(lp);
        header.addView(hAlumno);
        header.addView(hCalif);
        tableAlumnos.addView(header);

        // Obtener alumnos (a falta de vínculo por materia, mostramos los registrados)
        Cursor cur = db.rawQuery("SELECT nombrealum FROM Alumnos", null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String nombreAlum = cur.getString(0);
                    TableRow row = new TableRow(this);
                    TextView tvName = new TextView(this);
                    tvName.setText(nombreAlum);
                    tvName.setLayoutParams(lp);

                    EditText etCalif = new EditText(this);
                    etCalif.setHint("--");
                    etCalif.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    etCalif.setLayoutParams(lp);

                    row.addView(tvName);
                    row.addView(etCalif);
                    tableAlumnos.addView(row);
                } while (cur.moveToNext());
            } else {
                TableRow row = new TableRow(this);
                TextView tv = new TextView(this);
                tv.setText("No hay alumnos registrados");
                TableRow.LayoutParams lpFull = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(lpFull);
                row.addView(tv);
                tableAlumnos.addView(row);
            }
            cur.close();
        }
    }
}
