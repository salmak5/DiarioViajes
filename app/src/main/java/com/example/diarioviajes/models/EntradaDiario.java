package com.example.diarioviajes.models;

public class EntradaDiario {

        private int id, viajeId;
        private String titulo, descripcion, fecha,foto;


    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getViajeId() { return viajeId; }
        public void setViajeId(int viajeId) { this.viajeId = viajeId; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
    }

