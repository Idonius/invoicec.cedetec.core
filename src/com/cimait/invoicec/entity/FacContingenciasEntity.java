package com.cimait.invoicec.entity;

import javax.persistence.*;
import java.sql.Timestamp;


@Entity
@Table(name = "fac_contingencias")
@IdClass(FacContingenciasEntityPK.class)
public class FacContingenciasEntity {
    private String ruc;
    private String tipoAmbiente;
    private String numeroContingencia;
    private Timestamp fechaUtilizado;
    private String isActive;

    @Id
    @Column(name = "Ruc")
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    @Id
    @Column(name = "TipoAmbiente")
    public String getTipoAmbiente() {
        return tipoAmbiente;
    }

    public void setTipoAmbiente(String tipoAmbiente) {
        this.tipoAmbiente = tipoAmbiente;
    }

    @Id
    @Column(name = "NumeroContingencia")
    public String getNumeroContingencia() {
        return numeroContingencia;
    }

    public void setNumeroContingencia(String numeroContingencia) {
        this.numeroContingencia = numeroContingencia;
    }

    @Basic
    @Column(name = "fechaUtilizado")
    public Timestamp getFechaUtilizado() {
        return fechaUtilizado;
    }

    public void setFechaUtilizado(Timestamp fechaUtilizado) {
        this.fechaUtilizado = fechaUtilizado;
    }

    @Basic
    @Column(name = "isActive")
    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacContingenciasEntity that = (FacContingenciasEntity) o;

        if (fechaUtilizado != null ? !fechaUtilizado.equals(that.fechaUtilizado) : that.fechaUtilizado != null)
            return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (numeroContingencia != null ? !numeroContingencia.equals(that.numeroContingencia) : that.numeroContingencia != null)
            return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (tipoAmbiente != null ? !tipoAmbiente.equals(that.tipoAmbiente) : that.tipoAmbiente != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (tipoAmbiente != null ? tipoAmbiente.hashCode() : 0);
        result = 31 * result + (numeroContingencia != null ? numeroContingencia.hashCode() : 0);
        result = 31 * result + (fechaUtilizado != null ? fechaUtilizado.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        return result;
    }
}
