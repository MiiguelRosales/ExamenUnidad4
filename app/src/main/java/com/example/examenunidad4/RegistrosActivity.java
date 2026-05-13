package com.example.examenunidad4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RegistrosActivity extends AppCompatActivity {

    private AdminSqLite admin;
    private SQLiteDatabase db;

    private EditText numcontrol;
    private EditText nombre;
    private EditText numempleado;
    private EditText docente;
    private EditText direcciondocente;
    private EditText clavemateria;
    private EditText nombremateria;

    private Spinner spinnerMateria1;
    private Spinner spinnerMateria2;
    private Spinner spinnerDocente1;
    private Spinner spinnerDocente2;

    private Button btnInsertar;
    private Button btnBuscar;
    private Button btnActualizar;
    private Button btnEliminar;
    private Button btnLimpiar;

    private boolean ignoreStudentSpinnerChange = false;
    private boolean ignoreDocenteSpinnerChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registros);

        admin = new AdminSqLite(this, "administracion", null, 2);
        db = admin.getWritableDatabase();

        bindViews();
        loadMateriaAdapters();
        configureButtons();
    }

    private void bindViews() {
        numcontrol = findViewById(R.id.numcontrol);
        nombre = findViewById(R.id.nombre);
        numempleado = findViewById(R.id.numempleado);
        docente = findViewById(R.id.docente);
        direcciondocente = findViewById(R.id.direcciondocente);
        clavemateria = findViewById(R.id.clavemateria);
        nombremateria = findViewById(R.id.nombremateria);

        spinnerMateria1 = findViewById(R.id.spinnerMateria1);
        spinnerMateria2 = findViewById(R.id.spinnerMateria2);
        spinnerDocente1 = findViewById(R.id.spinnerDocente1);
        spinnerDocente2 = findViewById(R.id.spinnerDocente2);

        btnInsertar = findViewById(R.id.btnInsertar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
    }

    private ArrayList<String> obtenerOpcionesMaterias() {
        ArrayList<String> materias = new ArrayList<>();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT nombreMat FROM Materias ORDER BY nombreMat", null);
            if (c.moveToFirst()) {
                do {
                    materias.add(c.getString(0));
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
        }

        ArrayList<String> options = new ArrayList<>();
        options.add("Ninguna");
        options.addAll(materias);
        return options;
    }

    private void loadMateriaAdapters() {
        final ArrayList<String> options = obtenerOpcionesMaterias();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        if (spinnerMateria1 != null) spinnerMateria1.setAdapter(adapter);
        if (spinnerMateria2 != null) spinnerMateria2.setAdapter(adapter);
        if (spinnerDocente1 != null) spinnerDocente1.setAdapter(adapter);
        if (spinnerDocente2 != null) spinnerDocente2.setAdapter(adapter);

        if (spinnerMateria1 != null) spinnerMateria1.setSelection(0);
        if (spinnerMateria2 != null) spinnerMateria2.setSelection(0);
        if (spinnerDocente1 != null) spinnerDocente1.setSelection(0);
        if (spinnerDocente2 != null) spinnerDocente2.setSelection(0);

        AdapterView.OnItemSelectedListener studentListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignoreStudentSpinnerChange) return;

                int pos1 = spinnerMateria1 != null ? spinnerMateria1.getSelectedItemPosition() : 0;
                int pos2 = spinnerMateria2 != null ? spinnerMateria2.getSelectedItemPosition() : 0;

                if (parent == spinnerMateria1 && spinnerMateria2 != null && pos1 != 0 && pos1 == pos2) {
                    ignoreStudentSpinnerChange = true;
                    spinnerMateria2.setSelection(findDifferentPosition(options, pos1));
                    Toast.makeText(RegistrosActivity.this, "Las materias del alumno deben ser diferentes", Toast.LENGTH_SHORT).show();
                    ignoreStudentSpinnerChange = false;
                } else if (parent == spinnerMateria2 && spinnerMateria1 != null && pos2 != 0 && pos2 == pos1) {
                    ignoreStudentSpinnerChange = true;
                    spinnerMateria1.setSelection(findDifferentPosition(options, pos2));
                    Toast.makeText(RegistrosActivity.this, "Las materias del alumno deben ser diferentes", Toast.LENGTH_SHORT).show();
                    ignoreStudentSpinnerChange = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        AdapterView.OnItemSelectedListener docenteListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignoreDocenteSpinnerChange) return;

                int pos1 = spinnerDocente1 != null ? spinnerDocente1.getSelectedItemPosition() : 0;
                int pos2 = spinnerDocente2 != null ? spinnerDocente2.getSelectedItemPosition() : 0;

                if (parent == spinnerDocente1 && spinnerDocente2 != null && pos1 != 0 && pos1 == pos2) {
                    ignoreDocenteSpinnerChange = true;
                    spinnerDocente2.setSelection(findDifferentPosition(options, pos1));
                    Toast.makeText(RegistrosActivity.this, "Las materias del docente deben ser diferentes", Toast.LENGTH_SHORT).show();
                    ignoreDocenteSpinnerChange = false;
                } else if (parent == spinnerDocente2 && spinnerDocente1 != null && pos2 != 0 && pos2 == pos1) {
                    ignoreDocenteSpinnerChange = true;
                    spinnerDocente1.setSelection(findDifferentPosition(options, pos2));
                    Toast.makeText(RegistrosActivity.this, "Las materias del docente deben ser diferentes", Toast.LENGTH_SHORT).show();
                    ignoreDocenteSpinnerChange = false;
                }
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

    private void configureButtons() {
        btnInsertar.setOnClickListener(v -> guardarRegistros());
        btnBuscar.setOnClickListener(v -> buscarRegistros());
        btnActualizar.setOnClickListener(v -> actualizarRegistros());
        btnEliminar.setOnClickListener(v -> eliminarRegistros());
        btnLimpiar.setOnClickListener(v -> limpiarTodosLosCampos());
    }

    private void guardarRegistros() {
        int guardados = 0;

        if (guardarAlumno()) guardados++;
        if (guardarDocente()) guardados++;
        if (guardarMateria()) guardados++;

        if (guardados == 0) {
            Toast.makeText(this, "Completa al menos un apartado para guardar", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Guardado correcto en " + guardados + " apartado(s)", Toast.LENGTH_LONG).show();
            loadMateriaAdapters();
        }
    }

    private boolean guardarAlumno() {
        String codigo = texto(numcontrol);
        String nombreAlumno = texto(nombre);

        if (TextUtils.isEmpty(codigo) && TextUtils.isEmpty(nombreAlumno)) {
            return false;
        }
        if (TextUtils.isEmpty(codigo) || TextUtils.isEmpty(nombreAlumno)) {
            Toast.makeText(this, "Completa número de control y nombre del alumno", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!tieneMateriaSeleccionada(spinnerMateria1, spinnerMateria2)) {
            Toast.makeText(this, "Selecciona al menos 1 materia para el alumno", Toast.LENGTH_SHORT).show();
            return false;
        }

        Integer num = parseEntero(codigo);
        if (num == null) {
            Toast.makeText(this, "Número de control inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("numcontrol", num);
        values.put("nombrealum", nombreAlumno);
        values.put("materia1", materiaSeleccionada(spinnerMateria1));
        values.put("materia2", materiaSeleccionada(spinnerMateria2));

        long result = db.insertWithOnConflict("Alumnos", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) {
            Toast.makeText(this, "El alumno ya existe", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean guardarDocente() {
        String codigo = texto(numempleado);
        String nombreDocente = texto(docente);
        String direccion = texto(direcciondocente);

        if (TextUtils.isEmpty(codigo) && TextUtils.isEmpty(nombreDocente) && TextUtils.isEmpty(direccion)) {
            return false;
        }
        if (TextUtils.isEmpty(codigo) || TextUtils.isEmpty(nombreDocente)) {
            Toast.makeText(this, "Completa número de empleado y nombre del docente", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!tieneMateriaSeleccionada(spinnerDocente1, spinnerDocente2)) {
            Toast.makeText(this, "Selecciona al menos 1 materia para el docente", Toast.LENGTH_SHORT).show();
            return false;
        }

        Integer num = parseEntero(codigo);
        if (num == null) {
            Toast.makeText(this, "Número de empleado inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("numEmpleado", num);
        values.put("nombreDoc", nombreDocente);
        values.put("Direccion", direccion);
        values.put("materia1", materiaSeleccionada(spinnerDocente1));
        values.put("materia2", materiaSeleccionada(spinnerDocente2));

        long result = db.insertWithOnConflict("Docentes", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) {
            Toast.makeText(this, "El docente ya existe", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean guardarMateria() {
        String codigo = texto(clavemateria);
        String nombreMateria = texto(nombremateria);

        if (TextUtils.isEmpty(codigo) && TextUtils.isEmpty(nombreMateria)) {
            return false;
        }
        if (TextUtils.isEmpty(codigo) || TextUtils.isEmpty(nombreMateria)) {
            Toast.makeText(this, "Completa clave y nombre de la materia", Toast.LENGTH_SHORT).show();
            return false;
        }

        Integer num = parseEntero(codigo);
        if (num == null) {
            Toast.makeText(this, "Clave de materia inválida", Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("claveMateria", num);
        values.put("nombreMat", nombreMateria);

        long result = db.insertWithOnConflict("Materias", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (result == -1) {
            Toast.makeText(this, "La materia ya existe", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void buscarRegistros() {
        buscarAlumno();
        buscarDocente();
        buscarMateria();
    }

    private void buscarAlumno() {
        String codigo = texto(numcontrol);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }

        Cursor c = null;
        try {
            c = db.rawQuery("SELECT nombrealum, materia1, materia2 FROM Alumnos WHERE numcontrol = ?", new String[]{codigo});
            if (c.moveToFirst()) {
                nombre.setText(c.getString(0));
                setSpinnerSelectionByName(spinnerMateria1, c.getString(1));
                setSpinnerSelectionByName(spinnerMateria2, c.getString(2));
                Toast.makeText(this, "Alumno encontrado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Alumno no encontrado", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (c != null) c.close();
        }
    }

    private void buscarDocente() {
        String codigo = texto(numempleado);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }

        Cursor c = null;
        try {
            c = db.rawQuery("SELECT nombreDoc, Direccion, materia1, materia2 FROM Docentes WHERE numEmpleado = ?", new String[]{codigo});
            if (c.moveToFirst()) {
                docente.setText(c.getString(0));
                direcciondocente.setText(c.getString(1));
                setSpinnerSelectionByName(spinnerDocente1, c.getString(2));
                setSpinnerSelectionByName(spinnerDocente2, c.getString(3));
                Toast.makeText(this, "Docente encontrado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Docente no encontrado", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (c != null) c.close();
        }
    }

    private void buscarMateria() {
        String codigo = texto(clavemateria);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }

        Cursor c = null;
        try {
            c = db.rawQuery("SELECT nombreMat FROM Materias WHERE claveMateria = ?", new String[]{codigo});
            if (c.moveToFirst()) {
                nombremateria.setText(c.getString(0));
                Toast.makeText(this, "Materia encontrada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Materia no encontrada", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (c != null) c.close();
        }
    }

    private void actualizarRegistros() {
        actualizarAlumno();
        actualizarDocente();
        actualizarMateria();
        loadMateriaAdapters();
    }

    private void actualizarAlumno() {
        String codigo = texto(numcontrol);
        String nombreAlumno = texto(nombre);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }
        if (TextUtils.isEmpty(nombreAlumno)) {
            Toast.makeText(this, "No hay datos para actualizar alumno", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("nombrealum", nombreAlumno);
        values.put("materia1", materiaSeleccionada(spinnerMateria1));
        values.put("materia2", materiaSeleccionada(spinnerMateria2));
        int rows = db.update("Alumnos", values, "numcontrol = ?", new String[]{codigo});
        Toast.makeText(this, rows > 0 ? "Alumno actualizado" : "Alumno no encontrado", Toast.LENGTH_SHORT).show();
    }

    private void actualizarDocente() {
        String codigo = texto(numempleado);
        String nombreDocente = texto(docente);
        String direccion = texto(direcciondocente);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }
        if (TextUtils.isEmpty(nombreDocente) && TextUtils.isEmpty(direccion)) {
            Toast.makeText(this, "No hay datos para actualizar docente", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(nombreDocente)) values.put("nombreDoc", nombreDocente);
        if (!TextUtils.isEmpty(direccion)) values.put("Direccion", direccion);
        values.put("materia1", materiaSeleccionada(spinnerDocente1));
        values.put("materia2", materiaSeleccionada(spinnerDocente2));

        int rows = db.update("Docentes", values, "numEmpleado = ?", new String[]{codigo});
        Toast.makeText(this, rows > 0 ? "Docente actualizado" : "Docente no encontrado", Toast.LENGTH_SHORT).show();
    }

    private void actualizarMateria() {
        String codigo = texto(clavemateria);
        String nombreMateria = texto(nombremateria);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }
        if (TextUtils.isEmpty(nombreMateria)) {
            Toast.makeText(this, "No hay datos para actualizar materia", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("nombreMat", nombreMateria);
        int rows = db.update("Materias", values, "claveMateria = ?", new String[]{codigo});
        Toast.makeText(this, rows > 0 ? "Materia actualizada" : "Materia no encontrada", Toast.LENGTH_SHORT).show();
    }

    private void eliminarRegistros() {
        eliminarAlumno();
        eliminarDocente();
        eliminarMateria();
        loadMateriaAdapters();
    }

    private void eliminarAlumno() {
        String codigo = texto(numcontrol);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }

        int rows = db.delete("Alumnos", "numcontrol = ?", new String[]{codigo});
        if (rows > 0) {
            limpiarAlumno();
            Toast.makeText(this, "Alumno eliminado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Alumno no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarDocente() {
        String codigo = texto(numempleado);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }

        int rows = db.delete("Docentes", "numEmpleado = ?", new String[]{codigo});
        if (rows > 0) {
            limpiarDocente();
            Toast.makeText(this, "Docente eliminado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Docente no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarMateria() {
        String codigo = texto(clavemateria);
        if (TextUtils.isEmpty(codigo)) {
            return;
        }

        int rows = db.delete("Materias", "claveMateria = ?", new String[]{codigo});
        if (rows > 0) {
            limpiarMateria();
            Toast.makeText(this, "Materia eliminada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Materia no encontrada", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarTodosLosCampos() {
        limpiarAlumno();
        limpiarDocente();
        limpiarMateria();

        if (spinnerMateria1 != null) spinnerMateria1.setSelection(0);
        if (spinnerMateria2 != null) spinnerMateria2.setSelection(0);
        if (spinnerDocente1 != null) spinnerDocente1.setSelection(0);
        if (spinnerDocente2 != null) spinnerDocente2.setSelection(0);

        Toast.makeText(this, "Campos limpiados", Toast.LENGTH_SHORT).show();
    }

    private String texto(EditText editText) {
        return editText == null ? "" : editText.getText().toString().trim();
    }

    private Integer parseEntero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean tieneMateriaSeleccionada(Spinner spinner1, Spinner spinner2) {
        return materiaSeleccionada(spinner1) != null || materiaSeleccionada(spinner2) != null;
    }

    private String materiaSeleccionada(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItem() == null) {
            return null;
        }

        String materia = spinner.getSelectedItem().toString().trim();
        if (materia.isEmpty() || "Ninguna".equalsIgnoreCase(materia)) {
            return null;
        }
        return materia;
    }

    private void setSpinnerSelectionByName(Spinner spinner, String materia) {
        if (spinner == null || materia == null) {
            return;
        }

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) {
            return;
        }

        int index = adapter.getPosition(materia);
        if (index >= 0) {
            spinner.setSelection(index);
        }
    }

    private void limpiarAlumno() {
        numcontrol.setText("");
        nombre.setText("");
    }

    private void limpiarDocente() {
        numempleado.setText("");
        docente.setText("");
        direcciondocente.setText("");
    }

    private void limpiarMateria() {
        clavemateria.setText("");
        nombremateria.setText("");
    }

    private int findDifferentPosition(ArrayList<String> options, int forbiddenPos) {
        if (options == null || options.size() <= 1) return 0;
        for (int i = 1; i < options.size(); i++) {
            if (i != forbiddenPos) return i;
        }
        return 0;
    }
}
