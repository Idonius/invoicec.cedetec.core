package com.cimait.invoicec.entity;

import javax.persistence.*;
import java.sql.Timestamp;


@Entity
@Table(name = "fac_bitacora_documentos")
@IdClass(FacBitacoraDocumentosEntityPK.class)
public class FacBitacoraDocumentosEntity {
    private int ambiente;
    private String ruc;
    private String codEstablecimiento;
    private String codPuntEmision;
    private String secuencial;
    private String codigoDocumento;
    private Timestamp fechaEmision;
    private Timestamp fechaProceso;
    private String estadoTransaccion;
    private String msjProceso;
    private String msjError;
    private String xmlGenerado;
    private String xmlFirmado;
    private String xmlRespuesta;
    private String xmlAutorizacion;

    @Id
    @Column(name = "\"ambiente\"")
    public int getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(int ambiente) {
        this.ambiente = ambiente;
    }

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
    @Column(name = "\"CodigoDocumento\"")
    public String getCodigoDocumento() {
        return codigoDocumento;
    }

    public void setCodigoDocumento(String codigoDocumento) {
        this.codigoDocumento = codigoDocumento;
    }

    @Id
    @Column(name = "\"fechaEmision\"")
    public Timestamp getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Timestamp fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    @Id
    @Column(name = "\"fechaProceso\"")
    public Timestamp getFechaProceso() {
        return fechaProceso;
    }

    public void setFechaProceso(Timestamp fechaProceso) {
        this.fechaProceso = fechaProceso;
    }

    @Basic
    @Column(name = "\"ESTADO_TRANSACCION\"")
    public String getEstadoTransaccion() {
        return estadoTransaccion;
    }

    public void setEstadoTransaccion(String estadoTransaccion) {
        this.estadoTransaccion = estadoTransaccion;
    }

    @Basic
    @Column(name = "\"MSJ_PROCESO\"")
    public String getMsjProceso() {
        return msjProceso;
    }

    public void setMsjProceso(String msjProceso) {
        this.msjProceso = msjProceso;
    }

    @Basic
    @Column(name = "\"MSJ_ERROR\"")
    public String getMsjError() {
        return msjError;
    }

    public void setMsjError(String msjError) {
        this.msjError = msjError;
    }

    @Basic
    @Column(name = "\"xml_generado\"")
    public String getXmlGenerado() {
        return xmlGenerado;
    }

    public void setXmlGenerado(String xmlGenerado) {
        this.xmlGenerado = xmlGenerado;
    }

    @Basic
    @Column(name = "\"xml_firmado\"")
    public String getXmlFirmado() {
        return xmlFirmado;
    }

    public void setXmlFirmado(String xmlFirmado) {
        this.xmlFirmado = xmlFirmado;
    }

    @Basic
    @Column(name = "\"xml_respuesta\"")
    public String getXmlRespuesta() {
        return xmlRespuesta;
    }

    public void setXmlRespuesta(String xmlRespuesta) {
        this.xmlRespuesta = xmlRespuesta;
    }

    @Basic
    @Column(name = "\"xml_autorizacion\"")
    public String getXmlAutorizacion() {
        return xmlAutorizacion;
    }

    public void setXmlAutorizacion(String xmlAutorizacion) {
        this.xmlAutorizacion = xmlAutorizacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacBitacoraDocumentosEntity that = (FacBitacoraDocumentosEntity) o;

        if (ambiente != that.ambiente) return false;
        if (codEstablecimiento != null ? !codEstablecimiento.equals(that.codEstablecimiento) : that.codEstablecimiento != null)
            return false;
        if (codPuntEmision != null ? !codPuntEmision.equals(that.codPuntEmision) : that.codPuntEmision != null)
            return false;
        if (codigoDocumento != null ? !codigoDocumento.equals(that.codigoDocumento) : that.codigoDocumento != null)
            return false;
        if (estadoTransaccion != null ? !estadoTransaccion.equals(that.estadoTransaccion) : that.estadoTransaccion != null)
            return false;
        if (fechaEmision != null ? !fechaEmision.equals(that.fechaEmision) : that.fechaEmision != null) return false;
        if (fechaProceso != null ? !fechaProceso.equals(that.fechaProceso) : that.fechaProceso != null) return false;
        if (msjError != null ? !msjError.equals(that.msjError) : that.msjError != null) return false;
        if (msjProceso != null ? !msjProceso.equals(that.msjProceso) : that.msjProceso != null) return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (secuencial != null ? !secuencial.equals(that.secuencial) : that.secuencial != null) return false;
        if (xmlAutorizacion != null ? !xmlAutorizacion.equals(that.xmlAutorizacion) : that.xmlAutorizacion != null)
            return false;
        if (xmlFirmado != null ? !xmlFirmado.equals(that.xmlFirmado) : that.xmlFirmado != null) return false;
        if (xmlGenerado != null ? !xmlGenerado.equals(that.xmlGenerado) : that.xmlGenerado != null) return false;
        if (xmlRespuesta != null ? !xmlRespuesta.equals(that.xmlRespuesta) : that.xmlRespuesta != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ambiente;
        result = 31 * result + (ruc != null ? ruc.hashCode() : 0);
        result = 31 * result + (codEstablecimiento != null ? codEstablecimiento.hashCode() : 0);
        result = 31 * result + (codPuntEmision != null ? codPuntEmision.hashCode() : 0);
        result = 31 * result + (secuencial != null ? secuencial.hashCode() : 0);
        result = 31 * result + (codigoDocumento != null ? codigoDocumento.hashCode() : 0);
        result = 31 * result + (fechaEmision != null ? fechaEmision.hashCode() : 0);
        result = 31 * result + (fechaProceso != null ? fechaProceso.hashCode() : 0);
        result = 31 * result + (estadoTransaccion != null ? estadoTransaccion.hashCode() : 0);
        result = 31 * result + (msjProceso != null ? msjProceso.hashCode() : 0);
        result = 31 * result + (msjError != null ? msjError.hashCode() : 0);
        result = 31 * result + (xmlGenerado != null ? xmlGenerado.hashCode() : 0);
        result = 31 * result + (xmlFirmado != null ? xmlFirmado.hashCode() : 0);
        result = 31 * result + (xmlRespuesta != null ? xmlRespuesta.hashCode() : 0);
        result = 31 * result + (xmlAutorizacion != null ? xmlAutorizacion.hashCode() : 0);
        return result;
    }
}
