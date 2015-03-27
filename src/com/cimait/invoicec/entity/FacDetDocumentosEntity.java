package com.cimait.invoicec.entity;

import javax.persistence.*;


@Entity
@Table(name = "fac_det_documentos")
@IdClass(FacDetDocumentosEntityPK.class)
public class FacDetDocumentosEntity {
    private String ruc;
    private String codEstablecimiento;
    private String codPuntEmision;
    private String secuencial;
    private String codPrincipal;
    private String codAuxiliar;
    private String descripcion;
    private Double cantidad;
    private Double precioUnitario;
    private Double descuento;
    private Double precioTotalSinImpuesto;
    private Double valorIce;
    private int secuencialDetalle;
    private String codigoDocumento;

    @Id
    @Column(name = "\"Ruc\"")
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    @Id
    @Column(name = "\"CodEstablecimiento\"")
    public String getCodEstablecimiento() {
        return codEstablecimiento;
    }

    public void setCodEstablecimiento(String codEstablecimiento) {
        this.codEstablecimiento = codEstablecimiento;
    }

    @Id
    @Column(name = "\"CodPuntEmision\"")
    public String getCodPuntEmision() {
        return codPuntEmision;
    }

    public void setCodPuntEmision(String codPuntEmision) {
        this.codPuntEmision = codPuntEmision;
    }

    @Id
    @Column(name = "\"secuencial\"")
    public String getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(String secuencial) {
        this.secuencial = secuencial;
    }

    @Basic
    @Column(name = "\"CodPrincipal\"")
    public String getCodPrincipal() {
        return codPrincipal;
    }

    public void setCodPrincipal(String codPrincipal) {
        this.codPrincipal = codPrincipal;
    }

    @Basic
    @Column(name = "\"CodAuxiliar\"")
    public String getCodAuxiliar() {
        return codAuxiliar;
    }

    public void setCodAuxiliar(String codAuxiliar) {
        this.codAuxiliar = codAuxiliar;
    }

    @Basic
    @Column(name = "\"descripcion\"")
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Basic
    @Column(name = "\"cantidad\"")
    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    @Basic
    @Column(name = "\"precioUnitario\"")
    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    @Basic
    @Column(name = "\"descuento\"")
    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    @Basic
    @Column(name = "\"precioTotalSinImpuesto\"")
    public Double getPrecioTotalSinImpuesto() {
        return precioTotalSinImpuesto;
    }

    public void setPrecioTotalSinImpuesto(Double precioTotalSinImpuesto) {
        this.precioTotalSinImpuesto = precioTotalSinImpuesto;
    }

    @Basic
    @Column(name = "\"valorIce\"")
    public Double getValorIce() {
        return valorIce;
    }

    public void setValorIce(Double valorIce) {
        this.valorIce = valorIce;
    }

    @Id
    @Column(name = "\"secuencialDetalle\"")
    public int getSecuencialDetalle() {
        return secuencialDetalle;
    }

    public void setSecuencialDetalle(int secuencialDetalle) {
        this.secuencialDetalle = secuencialDetalle;
    }

    @Id
    @Column(name = "\"CodigoDocumento\"")
    public String getCodigoDocumento() {
        return codigoDocumento;
    }

    public void setCodigoDocumento(String codigoDocumento) {
        this.codigoDocumento = codigoDocumento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacDetDocumentosEntity that = (FacDetDocumentosEntity) o;

        if (secuencialDetalle != that.secuencialDetalle) return false;
        if (cantidad != null ? !cantidad.equals(that.cantidad) : that.cantidad != null) return false;
        if (codAuxiliar != null ? !codAuxiliar.equals(that.codAuxiliar) : that.codAuxiliar != null) return false;
        if (codEstablecimiento != null ? !codEstablecimiento.equals(that.codEstablecimiento) : that.codEstablecimiento != null)
            return false;
        if (codPrincipal != null ? !codPrincipal.equals(that.codPrincipal) : that.codPrincipal != null) return false;
        if (codPuntEmision != null ? !codPuntEmision.equals(that.codPuntEmision) : that.codPuntEmision != null)
            return false;
        if (codigoDocumento != null ? !codigoDocumento.equals(that.codigoDocumento) : that.codigoDocumento != null)
            return false;
        if (descripcion != null ? !descripcion.equals(that.descripcion) : that.descripcion != null) return false;
        if (descuento != null ? !descuento.equals(that.descuento) : that.descuento != null) return false;
        if (precioTotalSinImpuesto != null ? !precioTotalSinImpuesto.equals(that.precioTotalSinImpuesto) : that.precioTotalSinImpuesto != null)
            return false;
        if (precioUnitario != null ? !precioUnitario.equals(that.precioUnitario) : that.precioUnitario != null)
            return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (secuencial != null ? !secuencial.equals(that.secuencial) : that.secuencial != null) return false;
        if (valorIce != null ? !valorIce.equals(that.valorIce) : that.valorIce != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (codEstablecimiento != null ? codEstablecimiento.hashCode() : 0);
        result = 31 * result + (codPuntEmision != null ? codPuntEmision.hashCode() : 0);
        result = 31 * result + (secuencial != null ? secuencial.hashCode() : 0);
        result = 31 * result + (codPrincipal != null ? codPrincipal.hashCode() : 0);
        result = 31 * result + (codAuxiliar != null ? codAuxiliar.hashCode() : 0);
        result = 31 * result + (descripcion != null ? descripcion.hashCode() : 0);
        result = 31 * result + (cantidad != null ? cantidad.hashCode() : 0);
        result = 31 * result + (precioUnitario != null ? precioUnitario.hashCode() : 0);
        result = 31 * result + (descuento != null ? descuento.hashCode() : 0);
        result = 31 * result + (precioTotalSinImpuesto != null ? precioTotalSinImpuesto.hashCode() : 0);
        result = 31 * result + (valorIce != null ? valorIce.hashCode() : 0);
        result = 31 * result + secuencialDetalle;
        result = 31 * result + (codigoDocumento != null ? codigoDocumento.hashCode() : 0);
        return result;
    }
}
