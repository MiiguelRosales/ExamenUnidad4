package com.example.examenunidad4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VistaCalificacionesActivity extends AppCompatActivity {

    private TextView textAlumno;
    private TextView textMateria1;
    private TextView textMateria2;
    private TextView textCalificacion1;
    private TextView textCalificacion2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_calificaciones);

        textAlumno = findViewById(R.id.textAlumno);
        textMateria1 = findViewById(R.id.textMateria1);
        textMateria2 = findViewById(R.id.textMateria2);
        textCalificacion1 = findViewById(R.id.textCalificacion1);
        textCalificacion2 = findViewById(R.id.textCalificacion2);

        String numcontrol = getIntent().getStringExtra("alumno_numcontrol");
        if (numcontrol == null || numcontrol.trim().isEmpty()) {
            textAlumno.setText("Alumno no identificado");
            Toast.makeText(this, "No se recibió número de control", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarDatosAlumno(numcontrol.trim());
    }

    private void cargarDatosAlumno(String numcontrol) {
        AdminSqLite admin = new AdminSqLite(this, "administracion", null, 2);
        SQLiteDatabase db = admin.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT nombrealum, materia1, materia2 FROM Alumnos WHERE numcontrol = ?", new String[]{numcontrol});
            if (!cursor.moveToFirst()) {
                textAlumno.setText("Alumno no encontrado");
                Toast.makeText(this, "El alumno no existe en la base", Toast.LENGTH_SHORT).show();
                return;
            }

            String nombreAlumno = cursor.getString(0);
            String materia1 = cursor.getString(1);
            String materia2 = cursor.getString(2);

            textAlumno.setText("Nombre: " + nombreAlumno + "\nNúmero de control: " + numcontrol);
            mostrarMateriaConCalificacion(db, numcontrol, materia1, textMateria1, textCalificacion1);
            mostrarMateriaConCalificacion(db, numcontrol, materia2, textMateria2, textCalificacion2);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private void mostrarMateriaConCalificacion(SQLiteDatabase db, String numcontrol, String materia, TextView textoMateria, TextView textoCalificacion) {
        if (materia == null || materia.trim().isEmpty()) {
            textoMateria.setText("Ninguna");
            textoCalificacion.setText("--");
            return;
        }

        textoMateria.setText(materia);
        textoCalificacion.setText(obtenerCalificacionMateria(db, numcontrol, materia));
    }

    private String obtenerCalificacionMateria(SQLiteDatabase db, String numcontrol, String materia) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT c.calificacion " +
                            "FROM Calificaciones c " +
                            "INNER JOIN Materias m ON c.claveMateria = m.claveMateria " +
                            "WHERE c.numcontrol = ? AND m.nombreMat = ?",
                    new String[]{numcontrol, materia}
            );

            if (cursor.moveToFirst()) {
                return String.valueOf(cursor.getDouble(0));
            }

            return "--";
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
