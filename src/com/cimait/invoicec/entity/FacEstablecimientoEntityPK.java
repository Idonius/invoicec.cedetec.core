package com.cimait.invoicec.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class FacEstablecimientoEntityPK implements Serializable {
    private String ruc;
    private String codEstablecimiento;

    @Column(name = "\"Ruc\"")
    @Id
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    @Column(name = "\"CodEstablecimiento\"")
    @Id
    public String getCodEstablecimiento() {
        return codEstablecimiento;
    }

    public void setCodEstablecimiento(String codEstablecimiento) {
        this.codEstablecimiento = codEstablecimiento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacEstablecimientoEntityPK that = (FacEstablecimientoEntityPK) o;

        if (codEstablecimiento != null ? !codEstablecimiento.equals(that.codEstablecimiento) : that.codEstablecimiento != null)
            return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (codEstablecimiento != null ? codEstablecimiento.hashCode() : 0);
        return result;
    }
}
