package com.example.tfg2.Modelo;

public class Modelo {

    private String nombre;

    private String ultimoAnalisis;

    private String fecha;

    private String tipo;

    public Modelo(String nombre,String tipo, String ultimoAnalisis, String fecha) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ultimoAnalisis = ultimoAnalisis;
        this.fecha = fecha;
    }

    public Modelo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUltimoAnalisis() {
        return ultimoAnalisis;
    }

    public void setUltimoAnalisis(String ultimoAnalisis) {
        this.ultimoAnalisis = ultimoAnalisis;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
