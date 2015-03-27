package com.cimait.invoicec.entity;

import javax.persistence.*;

@Entity
@Table(name = "fac_clientes")
@IdClass(FacClientesEntityPK.class)
public class FacClientesEntity {
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String email;
    private String tipoCliente;
    private String tipoIdentificacion;
    private String rise;
    private String telefono;
    private String rucCliente;
    private String isActive;
    private String qadCodCliente;

    @Id
    @Column(name = "\"Ruc\"")
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    @Basic
    @Column(name = "\"RazonSocial\"")
    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    @Basic
    @Column(name = "\"Direccion\"")
    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Basic
    @Column(name = "\"Email\"")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "\"TipoCliente\"")
    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    @Basic
    @Column(name = "\"TipoIdentificacion\"")
    public String getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(String tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    @Basic
    @Column(name = "\"Rise\"")
    public String getRise() {
        return rise;
    }

    public void setRise(String rise) {
        this.rise = rise;
    }

    @Basic
    @Column(name = "\"Telefono\"")
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Id
    @Column(name = "\"RucCliente\"")
    public String getRucCliente() {
        return rucCliente;
    }

    public void setRucCliente(String rucCliente) {
        this.rucCliente = rucCliente;
    }

    @Basic
    @Column(name = "\"isActive\"")
    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    @Basic
    @Column(name = "\"QADCodCliente\"")
    public String getQadCodCliente() {
        return qadCodCliente;
    }

    public void setQadCodCliente(String qadCodCliente) {
        this.qadCodCliente = qadCodCliente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacClientesEntity that = (FacClientesEntity) o;

        if (direccion != null ? !direccion.equals(that.direccion) : that.direccion != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (qadCodCliente != null ? !qadCodCliente.equals(that.qadCodCliente) : that.qadCodCliente != null)
            return false;
        if (razonSocial != null ? !razonSocial.equals(that.razonSocial) : that.razonSocial != null) return false;
        if (rise != null ? !rise.equals(that.rise) : that.rise != null) return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (rucCliente != null ? !rucCliente.equals(that.rucCliente) : that.rucCliente != null) return false;
        if (telefono != null ? !telefono.equals(that.telefono) : that.telefono != null) return false;
        if (tipoCliente != null ? !tipoCliente.equals(that.tipoCliente) : that.tipoCliente != null) return false;
        if (tipoIdentificacion != null ? !tipoIdentificacion.equals(that.tipoIdentificacion) : that.tipoIdentificacion != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (razonSocial != null ? razonSocial.hashCode() : 0);
        result = 31 * result + (direccion != null ? direccion.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (tipoCliente != null ? tipoCliente.hashCode() : 0);
        result = 31 * result + (tipoIdentificacion != null ? tipoIdentificacion.hashCode() : 0);
        result = 31 * result + (rise != null ? rise.hashCode() : 0);
        result = 31 * result + (telefono != null ? telefono.hashCode() : 0);
        result = 31 * result + (rucCliente != null ? rucCliente.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (qadCodCliente != null ? qadCodCliente.hashCode() : 0);
        return result;
    }
}
