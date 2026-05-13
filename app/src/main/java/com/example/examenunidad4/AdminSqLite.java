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
            "nombrealum TEXT NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Docentes(" +
            "numEmpleado INTEGER PRIMARY KEY, " +
            "nombreDoc TEXT NOT NULL, " +
            "Direccion TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS Materias(" +
            "claveMateria INTEGER PRIMARY KEY, " +
            "nombreMat TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Alumnos");
        db.execSQL("DROP TABLE IF EXISTS Docentes");
        db.execSQL("DROP TABLE IF EXISTS Materias");
        onCreate(db);
    }
}