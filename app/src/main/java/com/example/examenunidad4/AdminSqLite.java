package com.example.examenunidad4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSqLite extends SQLiteOpenHelper{

    public AdminSqLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Alumnos(" +
            "numcontrol INTEGER PRIMARY KEY, " +
            "nombrealum TEXT NOT NULL, " +
            "materia1 TEXT, " +
            "materia2 TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Docentes(" +
            "numEmpleado INTEGER PRIMARY KEY, " +
            "nombreDoc TEXT NOT NULL, " +
            "Direccion TEXT, " +
            "materia1 TEXT, " +
            "materia2 TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Materias(" +
            "claveMateria INTEGER PRIMARY KEY, " +
            "nombreMat TEXT NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Calificaciones(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "numcontrol INTEGER, " +
            "claveMateria INTEGER, " +
            "calificacion REAL, " +
            "UNIQUE(numcontrol, claveMateria))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("ALTER TABLE Alumnos ADD COLUMN materia1 TEXT");
        } catch (Exception ignored) {
        }
        try {
            db.execSQL("ALTER TABLE Alumnos ADD COLUMN materia2 TEXT");
        } catch (Exception ignored) {
        }
        try {
            db.execSQL("ALTER TABLE Docentes ADD COLUMN materia1 TEXT");
        } catch (Exception ignored) {
        }
        try {
            db.execSQL("ALTER TABLE Docentes ADD COLUMN materia2 TEXT");
        } catch (Exception ignored) {
        }

        db.execSQL("CREATE TABLE IF NOT EXISTS Calificaciones(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "numcontrol INTEGER, " +
            "claveMateria INTEGER, " +
            "calificacion REAL, " +
            "UNIQUE(numcontrol, claveMateria))");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        db.execSQL("CREATE TABLE IF NOT EXISTS Calificaciones(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "numcontrol INTEGER, " +
            "claveMateria INTEGER, " +
            "calificacion REAL, " +
            "UNIQUE(numcontrol, claveMateria))");
    }
}