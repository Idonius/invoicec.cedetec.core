package com.cimait.invoicec.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;


public class FacContingenciasEntityPK implements Serializable {
    private String ruc;
    private String tipoAmbiente;
    private String numeroContingencia;

    @Column(name = "Ruc")
    @Id
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    @Column(name = "TipoAmbiente")
    @Id
    public String getTipoAmbiente() {
        return tipoAmbiente;
    }

    public void setTipoAmbiente(String tipoAmbiente) {
        this.tipoAmbiente = tipoAmbiente;
    }

    @Column(name = "NumeroContingencia")
    @Id
    public String getNumeroContingencia() {
        return numeroContingencia;
    }

    public void setNumeroContingencia(String numeroContingencia) {
        this.numeroContingencia = numeroContingencia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacContingenciasEntityPK that = (FacContingenciasEntityPK) o;

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
        return result;
    }
}
