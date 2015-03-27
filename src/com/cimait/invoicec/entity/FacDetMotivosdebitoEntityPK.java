package com.cimait.invoicec.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;


public class FacDetMotivosdebitoEntityPK implements Serializable {
    private String ruc;

    @Column(name = "\"Ruc\"")
    @Id
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    private String codEstablecimiento;

    @Column(name = "\"CodEstablecimiento\"")
    @Id
    public String getCodEstablecimiento() {
        return codEstablecimiento;
    }

    public void setCodEstablecimiento(String codEstablecimiento) {
        this.codEstablecimiento = codEstablecimiento;
    }

    private String codPuntEmision;

    @Column(name = "\"CodPuntEmision\"")
    @Id
    public String getCodPuntEmision() {
        return codPuntEmision;
    }

    public void setCodPuntEmision(String codPuntEmision) {
        this.codPuntEmision = codPuntEmision;
    }

    private String secuencial;

    @Column(name = "\"secuencial\"")
    @Id
    public String getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(String secuencial) {
        this.secuencial = secuencial;
    }

    private String codigoDocumento;

    @Column(name = "\"CodigoDocumento\"")
    @Id
    public String getCodigoDocumento() {
        return codigoDocumento;
    }

    public void setCodigoDocumento(String codigoDocumento) {
        this.codigoDocumento = codigoDocumento;
    }

    private int secuencialDetalle;

    @Column(name = "\"secuencialDetalle\"")
    @Id
    public int getSecuencialDetalle() {
        return secuencialDetalle;
    }

    public void setSecuencialDetalle(int secuencialDetalle) {
        this.secuencialDetalle = secuencialDetalle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacDetMotivosdebitoEntityPK that = (FacDetMotivosdebitoEntityPK) o;

        if (secuencialDetalle != that.secuencialDetalle) return false;
        if (codEstablecimiento != null ? !codEstablecimiento.equals(that.codEstablecimiento) : that.codEstablecimiento != null)
            return false;
        if (codPuntEmision != null ? !codPuntEmision.equals(that.codPuntEmision) : that.codPuntEmision != null)
            return false;
        if (codigoDocumento != null ? !codigoDocumento.equals(that.codigoDocumento) : that.codigoDocumento != null)
            return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (secuencial != null ? !secuencial.equals(that.secuencial) : that.secuencial != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (codEstablecimiento != null ? codEstablecimiento.hashCode() : 0);
        result = 31 * result + (codPuntEmision != null ? codPuntEmision.hashCode() : 0);
        result = 31 * result + (secuencial != null ? secuencial.hashCode() : 0);
        result = 31 * result + (codigoDocumento != null ? codigoDocumento.hashCode() : 0);
        result = 31 * result + secuencialDetalle;
        return result;
    }
}
