package com.example.diarioviajes.models;

public class Usuario {

        private int id;
        private String correo;
        private String contraseña;
       private String nombre;
       private String foto;
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getCorreo() {
            return correo;
        }
        public void setCorreo(String correo) {
            this.correo = correo;
        }
        public String getContraseña() {
            return contraseña;
        }
        public void setContraseña(String contraseña) {
            this.contraseña = contraseña;
        }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
    }

