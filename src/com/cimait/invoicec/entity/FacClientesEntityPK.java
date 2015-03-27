package com.cimait.invoicec.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;


public class FacClientesEntityPK implements Serializable {
    private String ruc;
    private String rucCliente;

    @Column(name = "\"Ruc\"")
    @Id
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    @Column(name = "\"RucCliente\"")
    @Id
    public String getRucCliente() {
        return rucCliente;
    }

    public void setRucCliente(String rucCliente) {
        this.rucCliente = rucCliente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacClientesEntityPK that = (FacClientesEntityPK) o;

        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (rucCliente != null ? !rucCliente.equals(that.rucCliente) : that.rucCliente != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (rucCliente != null ? rucCliente.hashCode() : 0);
        return result;
    }
}
