package com.example.diarioviajes.models;

public class Gasto {

    private int id;
    private int viajeId;
    private String nombre;
    private double cantidad;
    private String categoria;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getViajeId() { return viajeId; }
    public void setViajeId(int viajeId) { this.viajeId = viajeId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}