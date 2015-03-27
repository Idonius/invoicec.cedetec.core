package com.cimait.invoicec.entity;

import javax.persistence.*;

@Entity
@Table(name = "fac_det_retenciones")
@IdClass(FacDetRetencionesEntityPK.class)
public class FacDetRetencionesEntity {
    private String ruc;
    private String codEstablecimiento;
    private String codPuntEmision;
    private String secuencial;
    private int codImpuesto;
    private Integer codPorcentaje;
    private Double baseImponible;
    private Integer tarifa;
    private Double valor;
    private Double porcentajeRetencion;
    private int secuencialRetencion;
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

    @Id
    @Column(name = "\"codImpuesto\"")
    public int getCodImpuesto() {
        return codImpuesto;
    }

    public void setCodImpuesto(int codImpuesto) {
        this.codImpuesto = codImpuesto;
    }

    @Basic
    @Column(name = "\"codPorcentaje\"")
    public Integer getCodPorcentaje() {
        return codPorcentaje;
    }

    public void setCodPorcentaje(Integer codPorcentaje) {
        this.codPorcentaje = codPorcentaje;
    }

    @Basic
    @Column(name = "\"baseImponible\"")
    public Double getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(Double baseImponible) {
        this.baseImponible = baseImponible;
    }

    @Basic
    @Column(name = "\"tarifa\"")
    public Integer getTarifa() {
        return tarifa;
    }

    public void setTarifa(Integer tarifa) {
        this.tarifa = tarifa;
    }

    @Basic
    @Column(name = "\"valor\"")
    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    @Basic
    @Column(name = "\"porcentajeRetencion\"")
    public Double getPorcentajeRetencion() {
        return porcentajeRetencion;
    }

    public void setPorcentajeRetencion(Double porcentajeRetencion) {
        this.porcentajeRetencion = porcentajeRetencion;
    }

    @Id
    @Column(name = "\"secuencialRetencion\"")
    public int getSecuencialRetencion() {
        return secuencialRetencion;
    }

    public void setSecuencialRetencion(int secuencialRetencion) {
        this.secuencialRetencion = secuencialRetencion;
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

        FacDetRetencionesEntity that = (FacDetRetencionesEntity) o;

        if (codImpuesto != that.codImpuesto) return false;
        if (secuencialRetencion != that.secuencialRetencion) return false;
        if (baseImponible != null ? !baseImponible.equals(that.baseImponible) : that.baseImponible != null)
            return false;
        if (codEstablecimiento != null ? !codEstablecimiento.equals(that.codEstablecimiento) : that.codEstablecimiento != null)
            return false;
        if (codPorcentaje != null ? !codPorcentaje.equals(that.codPorcentaje) : that.codPorcentaje != null)
            return false;
        if (codPuntEmision != null ? !codPuntEmision.equals(that.codPuntEmision) : that.codPuntEmision != null)
            return false;
        if (codigoDocumento != null ? !codigoDocumento.equals(that.codigoDocumento) : that.codigoDocumento != null)
            return false;
        if (porcentajeRetencion != null ? !porcentajeRetencion.equals(that.porcentajeRetencion) : that.porcentajeRetencion != null)
            return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (secuencial != null ? !secuencial.equals(that.secuencial) : that.secuencial != null) return false;
        if (tarifa != null ? !tarifa.equals(that.tarifa) : that.tarifa != null) return false;
        if (valor != null ? !valor.equals(that.valor) : that.valor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (codEstablecimiento != null ? codEstablecimiento.hashCode() : 0);
        result = 31 * result + (codPuntEmision != null ? codPuntEmision.hashCode() : 0);
        result = 31 * result + (secuencial != null ? secuencial.hashCode() : 0);
        result = 31 * result + codImpuesto;
        result = 31 * result + (codPorcentaje != null ? codPorcentaje.hashCode() : 0);
        result = 31 * result + (baseImponible != null ? baseImponible.hashCode() : 0);
        result = 31 * result + (tarifa != null ? tarifa.hashCode() : 0);
        result = 31 * result + (valor != null ? valor.hashCode() : 0);
        result = 31 * result + (porcentajeRetencion != null ? porcentajeRetencion.hashCode() : 0);
        result = 31 * result + secuencialRetencion;
        result = 31 * result + (codigoDocumento != null ? codigoDocumento.hashCode() : 0);
        return result;
    }
}
