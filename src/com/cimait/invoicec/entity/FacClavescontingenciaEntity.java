package com.cimait.invoicec.entity;

import javax.persistence.*;
import java.sql.Date;


@Entity
@Table(name = "fac_clavescontingencia")
public class FacClavescontingenciaEntity {
    private int idclavecontingencia;
    private String clave;
    private String estado;
    private Date fechauso;
    private String ruc;
    private String tipo;

    @Id
    @Column(name = "idclavecontingencia")
    public int getIdclavecontingencia() {
        return idclavecontingencia;
    }

    public void setIdclavecontingencia(int idclavecontingencia) {
        this.idclavecontingencia = idclavecontingencia;
    }

    @Basic
    @Column(name = "clave")
    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    @Basic
    @Column(name = "estado")
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Basic
    @Column(name = "fechauso")
    public Date getFechauso() {
        return fechauso;
    }

    public void setFechauso(Date fechauso) {
        this.fechauso = fechauso;
    }

    @Basic
    @Column(name = "ruc")
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    @Basic
    @Column(name = "tipo")
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacClavescontingenciaEntity that = (FacClavescontingenciaEntity) o;

        if (idclavecontingencia != that.idclavecontingencia) return false;
        if (clave != null ? !clave.equals(that.clave) : that.clave != null) return false;
        if (estado != null ? !estado.equals(that.estado) : that.estado != null) return false;
        if (fechauso != null ? !fechauso.equals(that.fechauso) : that.fechauso != null) return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (tipo != null ? !tipo.equals(that.tipo) : that.tipo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = idclavecontingencia;
        result = 31 * result + (clave != null ? clave.hashCode() : 0);
        result = 31 * result + (estado != null ? estado.hashCode() : 0);
        result = 31 * result + (fechauso != null ? fechauso.hashCode() : 0);
        result = 31 * result + (ruc != null ? ruc.hashCode() : 0);
        result = 31 * result + (tipo != null ? tipo.hashCode() : 0);
        return result;
    }
}
