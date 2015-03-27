package com.cimait.invoicec.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Date;

@Entity
@javax.persistence.Table(name = "fac_cab_documentos")
public class FacCabDocumentosEntity implements Serializable {
    private Integer ambiente;

    @Basic
    @javax.persistence.Column(name = "\"ambiente\"")
    public Integer getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(Integer ambiente) {
        this.ambiente = ambiente;
    }

    private String ruc;

    @Id
    @javax.persistence.Column(name = "\"Ruc\"")
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    private Integer tipoIdentificacion;

    @Basic
    @javax.persistence.Column(name = "\"TipoIdentificacion\"")
    public Integer getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(Integer tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    private String codEstablecimiento;

    @Id
    @javax.persistence.Column(name = "\"CodEstablecimiento\"")
    public String getCodEstablecimiento() {
        return codEstablecimiento;
    }

    public void setCodEstablecimiento(String codEstablecimiento) {
        this.codEstablecimiento = codEstablecimiento;
    }

    private String codPuntEmision;

    @Id
    @javax.persistence.Column(name = "\"CodPuntEmision\"")
    public String getCodPuntEmision() {
        return codPuntEmision;
    }

    public void setCodPuntEmision(String codPuntEmision) {
        this.codPuntEmision = codPuntEmision;
    }

    private String secuencial;

    @Id
    @javax.persistence.Column(name = "\"secuencial\"")
    public String getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(String secuencial) {
        this.secuencial = secuencial;
    }

    private Date fechaEmision;

    @Basic
    @javax.persistence.Column(name = "\"fechaEmision\"")
    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    private String guiaRemision;

    @Basic
    @javax.persistence.Column(name = "\"guiaRemision\"")
    public String getGuiaRemision() {
        return guiaRemision;
    }

    public void setGuiaRemision(String guiaRemision) {
        this.guiaRemision = guiaRemision;
    }

    private String razonSocialComprador;

    @Basic
    @javax.persistence.Column(name = "\"razonSocialComprador\"")
    public String getRazonSocialComprador() {
        return razonSocialComprador;
    }

    public void setRazonSocialComprador(String razonSocialComprador) {
        this.razonSocialComprador = razonSocialComprador;
    }

    private String identificacionComprador;

    @Basic
    @javax.persistence.Column(name = "\"identificacionComprador\"")
    public String getIdentificacionComprador() {
        return identificacionComprador;
    }

    public void setIdentificacionComprador(String identificacionComprador) {
        this.identificacionComprador = identificacionComprador;
    }

    private Double totalSinImpuesto;

    @Basic
    @javax.persistence.Column(name = "\"totalSinImpuesto\"")
    public Double getTotalSinImpuesto() {
        return totalSinImpuesto;
    }

    public void setTotalSinImpuesto(Double totalSinImpuesto) {
        this.totalSinImpuesto = totalSinImpuesto;
    }

    private Double totalDescuento;

    @Basic
    @javax.persistence.Column(name = "\"totalDescuento\"")
    public Double getTotalDescuento() {
        return totalDescuento;
    }

    public void setTotalDescuento(Double totalDescuento) {
        this.totalDescuento = totalDescuento;
    }

    private String email;

    @Basic
    @javax.persistence.Column(name = "\"email\"")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private Double propina;

    @Basic
    @javax.persistence.Column(name = "\"propina\"")
    public Double getPropina() {
        return propina;
    }

    public void setPropina(Double propina) {
        this.propina = propina;
    }

    private String moneda;

    @Basic
    @javax.persistence.Column(name = "\"moneda\"")
    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    private String infoAdicional;

    @Basic
    @javax.persistence.Column(name = "\"infoAdicional\"")
    public String getInfoAdicional() {
        return infoAdicional;
    }

    public void setInfoAdicional(String infoAdicional) {
        this.infoAdicional = infoAdicional;
    }

    private String periodoFiscal;

    @Basic
    @javax.persistence.Column(name = "\"periodoFiscal\"")
    public String getPeriodoFiscal() {
        return periodoFiscal;
    }

    public void setPeriodoFiscal(String periodoFiscal) {
        this.periodoFiscal = periodoFiscal;
    }

    private String rise;

    @Basic
    @javax.persistence.Column(name = "\"rise\"")
    public String getRise() {
        return rise;
    }

    public void setRise(String rise) {
        this.rise = rise;
    }

    private Date fechaInicioTransporte;

    @Basic
    @javax.persistence.Column(name = "\"fechaInicioTransporte\"")
    public Date getFechaInicioTransporte() {
        return fechaInicioTransporte;
    }

    public void setFechaInicioTransporte(Date fechaInicioTransporte) {
        this.fechaInicioTransporte = fechaInicioTransporte;
    }

    private Date fechaFinTransporte;

    @Basic
    @javax.persistence.Column(name = "\"fechaFinTransporte\"")
    public Date getFechaFinTransporte() {
        return fechaFinTransporte;
    }

    public void setFechaFinTransporte(Date fechaFinTransporte) {
        this.fechaFinTransporte = fechaFinTransporte;
    }

    private String placa;

    @Basic
    @javax.persistence.Column(name = "\"placa\"")
    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    private Date fechaEmisionDocSustento;

    @Basic
    @javax.persistence.Column(name = "\"fechaEmisionDocSustento\"")
    public Date getFechaEmisionDocSustento() {
        return fechaEmisionDocSustento;
    }

    public void setFechaEmisionDocSustento(Date fechaEmisionDocSustento) {
        this.fechaEmisionDocSustento = fechaEmisionDocSustento;
    }

    private String motivoRazon;

    @Basic
    @javax.persistence.Column(name = "\"motivoRazon\"")
    public String getMotivoRazon() {
        return motivoRazon;
    }

    public void setMotivoRazon(String motivoRazon) {
        this.motivoRazon = motivoRazon;
    }

    private String identificacionDestinatario;

    @Basic
    @javax.persistence.Column(name = "\"identificacionDestinatario\"")
    public String getIdentificacionDestinatario() {
        return identificacionDestinatario;
    }

    public void setIdentificacionDestinatario(String identificacionDestinatario) {
        this.identificacionDestinatario = identificacionDestinatario;
    }

    private String razonSocialDestinatario;

    @Basic
    @javax.persistence.Column(name = "\"razonSocialDestinatario\"")
    public String getRazonSocialDestinatario() {
        return razonSocialDestinatario;
    }

    public void setRazonSocialDestinatario(String razonSocialDestinatario) {
        this.razonSocialDestinatario = razonSocialDestinatario;
    }

    private String direccionDestinatario;

    @Basic
    @javax.persistence.Column(name = "\"direccionDestinatario\"")
    public String getDireccionDestinatario() {
        return direccionDestinatario;
    }

    public void setDireccionDestinatario(String direccionDestinatario) {
        this.direccionDestinatario = direccionDestinatario;
    }

    private String motivoTraslado;

    @Basic
    @javax.persistence.Column(name = "\"motivoTraslado\"")
    public String getMotivoTraslado() {
        return motivoTraslado;
    }

    public void setMotivoTraslado(String motivoTraslado) {
        this.motivoTraslado = motivoTraslado;
    }

    private String docAduaneroUnico;

    @Basic
    @javax.persistence.Column(name = "\"docAduaneroUnico\"")
    public String getDocAduaneroUnico() {
        return docAduaneroUnico;
    }

    public void setDocAduaneroUnico(String docAduaneroUnico) {
        this.docAduaneroUnico = docAduaneroUnico;
    }

    private String codEstablecimientoDest;

    @Basic
    @javax.persistence.Column(name = "\"codEstablecimientoDest\"")
    public String getCodEstablecimientoDest() {
        return codEstablecimientoDest;
    }

    public void setCodEstablecimientoDest(String codEstablecimientoDest) {
        this.codEstablecimientoDest = codEstablecimientoDest;
    }

    private String ruta;

    @Basic
    @javax.persistence.Column(name = "\"ruta\"")
    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    private String codDocSustento;

    @Basic
    @javax.persistence.Column(name = "\"codDocSustento\"")
    public String getCodDocSustento() {
        return codDocSustento;
    }

    public void setCodDocSustento(String codDocSustento) {
        this.codDocSustento = codDocSustento;
    }

    private String numDocSustento;

    @Basic
    @javax.persistence.Column(name = "\"numDocSustento\"")
    public String getNumDocSustento() {
        return numDocSustento;
    }

    public void setNumDocSustento(String numDocSustento) {
        this.numDocSustento = numDocSustento;
    }

    private String numAutDocSustento;

    @Basic
    @javax.persistence.Column(name = "\"numAutDocSustento\"")
    public String getNumAutDocSustento() {
        return numAutDocSustento;
    }

    public void setNumAutDocSustento(String numAutDocSustento) {
        this.numAutDocSustento = numAutDocSustento;
    }

    private Date fecEmisionDocSustento;

    @Basic
    @javax.persistence.Column(name = "\"fecEmisionDocSustento\"")
    public Date getFecEmisionDocSustento() {
        return fecEmisionDocSustento;
    }

    public void setFecEmisionDocSustento(Date fecEmisionDocSustento) {
        this.fecEmisionDocSustento = fecEmisionDocSustento;
    }

    private String autorizacion;

    @Basic
    @javax.persistence.Column(name = "\"autorizacion\"")
    public String getAutorizacion() {
        return autorizacion;
    }

    public void setAutorizacion(String autorizacion) {
        this.autorizacion = autorizacion;
    }

    private Date fechaautorizacion;

    @Basic
    @javax.persistence.Column(name = "\"fechaautorizacion\"")
    public Date getFechaautorizacion() {
        return fechaautorizacion;
    }

    public void setFechaautorizacion(Date fechaautorizacion) {
        this.fechaautorizacion = fechaautorizacion;
    }

    private String claveAcceso;

    @Basic
    @javax.persistence.Column(name = "\"claveAcceso\"")
    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    private Double importeTotal;

    @Basic
    @javax.persistence.Column(name = "\"importeTotal\"")
    public Double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(Double importeTotal) {
        this.importeTotal = importeTotal;
    }

    private String codigoDocumento;

    @Id
    @javax.persistence.Column(name = "\"CodigoDocumento\"")
    public String getCodigoDocumento() {
        return codigoDocumento;
    }

    public void setCodigoDocumento(String codigoDocumento) {
        this.codigoDocumento = codigoDocumento;
    }

    private String codDocModificado;

    @Basic
    @javax.persistence.Column(name = "\"codDocModificado\"")
    public String getCodDocModificado() {
        return codDocModificado;
    }

    public void setCodDocModificado(String codDocModificado) {
        this.codDocModificado = codDocModificado;
    }

    private String numDocModificado;

    @Basic
    @javax.persistence.Column(name = "\"numDocModificado\"")
    public String getNumDocModificado() {
        return numDocModificado;
    }

    public void setNumDocModificado(String numDocModificado) {
        this.numDocModificado = numDocModificado;
    }

    private Double motivoValor;

    @Basic
    @javax.persistence.Column(name = "\"motivoValor\"")
    public Double getMotivoValor() {
        return motivoValor;
    }

    public void setMotivoValor(Double motivoValor) {
        this.motivoValor = motivoValor;
    }

    private String tipIdentificacionComprador;

    @Basic
    @javax.persistence.Column(name = "\"tipIdentificacionComprador\"")
    public String getTipIdentificacionComprador() {
        return tipIdentificacionComprador;
    }

    public void setTipIdentificacionComprador(String tipIdentificacionComprador) {
        this.tipIdentificacionComprador = tipIdentificacionComprador;
    }

    private String tipoEmision;

    @Basic
    @javax.persistence.Column(name = "\"tipoEmision\"")
    public String getTipoEmision() {
        return tipoEmision;
    }

    public void setTipoEmision(String tipoEmision) {
        this.tipoEmision = tipoEmision;
    }

    private String partida;

    @Basic
    @javax.persistence.Column(name = "\"partida\"")
    public String getPartida() {
        return partida;
    }

    public void setPartida(String partida) {
        this.partida = partida;
    }

    private Double subtotal12;

    @Basic
    @javax.persistence.Column(name = "\"subtotal12\"")
    public Double getSubtotal12() {
        return subtotal12;
    }

    public void setSubtotal12(Double subtotal12) {
        this.subtotal12 = subtotal12;
    }

    private Double subtotal0;

    @Basic
    @javax.persistence.Column(name = "\"subtotal0\"")
    public Double getSubtotal0() {
        return subtotal0;
    }

    public void setSubtotal0(Double subtotal0) {
        this.subtotal0 = subtotal0;
    }

    private Double subtotalNoIva;

    @Basic
    @javax.persistence.Column(name = "\"subtotalNoIva\"")
    public Double getSubtotalNoIva() {
        return subtotalNoIva;
    }

    public void setSubtotalNoIva(Double subtotalNoIva) {
        this.subtotalNoIva = subtotalNoIva;
    }

    private Double totalvalorIce;

    @Basic
    @javax.persistence.Column(name = "\"totalvalorICE\"")
    public Double getTotalvalorIce() {
        return totalvalorIce;
    }

    public void setTotalvalorIce(Double totalvalorIce) {
        this.totalvalorIce = totalvalorIce;
    }

    private Double iva12;

    @Basic
    @javax.persistence.Column(name = "\"iva12\"")
    public Double getIva12() {
        return iva12;
    }

    public void setIva12(Double iva12) {
        this.iva12 = iva12;
    }

    private String isActive;

    @Basic
    @javax.persistence.Column(name = "\"isActive\"")
    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    private String estadoTransaccion;

    @Basic
    @javax.persistence.Column(name = "\"ESTADO_TRANSACCION\"")
    public String getEstadoTransaccion() {
        return estadoTransaccion;
    }

    public void setEstadoTransaccion(String estadoTransaccion) {
        this.estadoTransaccion = estadoTransaccion;
    }

    private String msjError;

    @Basic
    @javax.persistence.Column(name = "\"MSJ_ERROR\"")
    public String getMsjError() {
        return msjError;
    }

    public void setMsjError(String msjError) {
        this.msjError = msjError;
    }

    private String tipo;

    @Basic
    @javax.persistence.Column(name = "\"Tipo\"")
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    private String claveAccesoContigente;

    @Basic
    @javax.persistence.Column(name = "\"claveAccesoContigente\"")
    public String getClaveAccesoContigente() {
        return claveAccesoContigente;
    }

    public void setClaveAccesoContigente(String claveAccesoContigente) {
        this.claveAccesoContigente = claveAccesoContigente;
    }

    private String claveContingencia;

    @Basic
    @javax.persistence.Column(name = "\"claveContingencia\"")
    public String getClaveContingencia() {
        return claveContingencia;
    }

    public void setClaveContingencia(String claveContingencia) {
        this.claveContingencia = claveContingencia;
    }

    private String docuAutorizacion;

    @Basic
    @javax.persistence.Column(name = "\"docuAutorizacion\"")
    public String getDocuAutorizacion() {
        return docuAutorizacion;
    }

    public void setDocuAutorizacion(String docuAutorizacion) {
        this.docuAutorizacion = docuAutorizacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacCabDocumentosEntity that = (FacCabDocumentosEntity) o;

        if (ambiente != null ? !ambiente.equals(that.ambiente) : that.ambiente != null) return false;
        if (autorizacion != null ? !autorizacion.equals(that.autorizacion) : that.autorizacion != null) return false;
        if (claveAcceso != null ? !claveAcceso.equals(that.claveAcceso) : that.claveAcceso != null) return false;
        if (claveAccesoContigente != null ? !claveAccesoContigente.equals(that.claveAccesoContigente) : that.claveAccesoContigente != null)
            return false;
        if (claveContingencia != null ? !claveContingencia.equals(that.claveContingencia) : that.claveContingencia != null)
            return false;
        if (codDocModificado != null ? !codDocModificado.equals(that.codDocModificado) : that.codDocModificado != null)
            return false;
        if (codDocSustento != null ? !codDocSustento.equals(that.codDocSustento) : that.codDocSustento != null)
            return false;
        if (codEstablecimiento != null ? !codEstablecimiento.equals(that.codEstablecimiento) : that.codEstablecimiento != null)
            return false;
        if (codEstablecimientoDest != null ? !codEstablecimientoDest.equals(that.codEstablecimientoDest) : that.codEstablecimientoDest != null)
            return false;
        if (codPuntEmision != null ? !codPuntEmision.equals(that.codPuntEmision) : that.codPuntEmision != null)
            return false;
        if (codigoDocumento != null ? !codigoDocumento.equals(that.codigoDocumento) : that.codigoDocumento != null)
            return false;
        if (direccionDestinatario != null ? !direccionDestinatario.equals(that.direccionDestinatario) : that.direccionDestinatario != null)
            return false;
        if (docAduaneroUnico != null ? !docAduaneroUnico.equals(that.docAduaneroUnico) : that.docAduaneroUnico != null)
            return false;
        if (docuAutorizacion != null ? !docuAutorizacion.equals(that.docuAutorizacion) : that.docuAutorizacion != null)
            return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (estadoTransaccion != null ? !estadoTransaccion.equals(that.estadoTransaccion) : that.estadoTransaccion != null)
            return false;
        if (fecEmisionDocSustento != null ? !fecEmisionDocSustento.equals(that.fecEmisionDocSustento) : that.fecEmisionDocSustento != null)
            return false;
        if (fechaEmision != null ? !fechaEmision.equals(that.fechaEmision) : that.fechaEmision != null) return false;
        if (fechaEmisionDocSustento != null ? !fechaEmisionDocSustento.equals(that.fechaEmisionDocSustento) : that.fechaEmisionDocSustento != null)
            return false;
        if (fechaFinTransporte != null ? !fechaFinTransporte.equals(that.fechaFinTransporte) : that.fechaFinTransporte != null)
            return false;
        if (fechaInicioTransporte != null ? !fechaInicioTransporte.equals(that.fechaInicioTransporte) : that.fechaInicioTransporte != null)
            return false;
        if (fechaautorizacion != null ? !fechaautorizacion.equals(that.fechaautorizacion) : that.fechaautorizacion != null)
            return false;
        if (guiaRemision != null ? !guiaRemision.equals(that.guiaRemision) : that.guiaRemision != null) return false;
        if (identificacionComprador != null ? !identificacionComprador.equals(that.identificacionComprador) : that.identificacionComprador != null)
            return false;
        if (identificacionDestinatario != null ? !identificacionDestinatario.equals(that.identificacionDestinatario) : that.identificacionDestinatario != null)
            return false;
        if (importeTotal != null ? !importeTotal.equals(that.importeTotal) : that.importeTotal != null) return false;
        if (infoAdicional != null ? !infoAdicional.equals(that.infoAdicional) : that.infoAdicional != null)
            return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (iva12 != null ? !iva12.equals(that.iva12) : that.iva12 != null) return false;
        if (moneda != null ? !moneda.equals(that.moneda) : that.moneda != null) return false;
        if (motivoRazon != null ? !motivoRazon.equals(that.motivoRazon) : that.motivoRazon != null) return false;
        if (motivoTraslado != null ? !motivoTraslado.equals(that.motivoTraslado) : that.motivoTraslado != null)
            return false;
        if (motivoValor != null ? !motivoValor.equals(that.motivoValor) : that.motivoValor != null) return false;
        if (msjError != null ? !msjError.equals(that.msjError) : that.msjError != null) return false;
        if (numAutDocSustento != null ? !numAutDocSustento.equals(that.numAutDocSustento) : that.numAutDocSustento != null)
            return false;
        if (numDocModificado != null ? !numDocModificado.equals(that.numDocModificado) : that.numDocModificado != null)
            return false;
        if (numDocSustento != null ? !numDocSustento.equals(that.numDocSustento) : that.numDocSustento != null)
            return false;
        if (partida != null ? !partida.equals(that.partida) : that.partida != null) return false;
        if (periodoFiscal != null ? !periodoFiscal.equals(that.periodoFiscal) : that.periodoFiscal != null)
            return false;
        if (placa != null ? !placa.equals(that.placa) : that.placa != null) return false;
        if (propina != null ? !propina.equals(that.propina) : that.propina != null) return false;
        if (razonSocialComprador != null ? !razonSocialComprador.equals(that.razonSocialComprador) : that.razonSocialComprador != null)
            return false;
        if (razonSocialDestinatario != null ? !razonSocialDestinatario.equals(that.razonSocialDestinatario) : that.razonSocialDestinatario != null)
            return false;
        if (rise != null ? !rise.equals(that.rise) : that.rise != null) return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (ruta != null ? !ruta.equals(that.ruta) : that.ruta != null) return false;
        if (secuencial != null ? !secuencial.equals(that.secuencial) : that.secuencial != null) return false;
        if (subtotal0 != null ? !subtotal0.equals(that.subtotal0) : that.subtotal0 != null) return false;
        if (subtotal12 != null ? !subtotal12.equals(that.subtotal12) : that.subtotal12 != null) return false;
        if (subtotalNoIva != null ? !subtotalNoIva.equals(that.subtotalNoIva) : that.subtotalNoIva != null)
            return false;
        if (tipIdentificacionComprador != null ? !tipIdentificacionComprador.equals(that.tipIdentificacionComprador) : that.tipIdentificacionComprador != null)
            return false;
        if (tipo != null ? !tipo.equals(that.tipo) : that.tipo != null) return false;
        if (tipoEmision != null ? !tipoEmision.equals(that.tipoEmision) : that.tipoEmision != null) return false;
        if (tipoIdentificacion != null ? !tipoIdentificacion.equals(that.tipoIdentificacion) : that.tipoIdentificacion != null)
            return false;
        if (totalDescuento != null ? !totalDescuento.equals(that.totalDescuento) : that.totalDescuento != null)
            return false;
        if (totalSinImpuesto != null ? !totalSinImpuesto.equals(that.totalSinImpuesto) : that.totalSinImpuesto != null)
            return false;
        if (totalvalorIce != null ? !totalvalorIce.equals(that.totalvalorIce) : that.totalvalorIce != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ambiente != null ? ambiente.hashCode() : 0;
        result = 31 * result + (ruc != null ? ruc.hashCode() : 0);
        result = 31 * result + (tipoIdentificacion != null ? tipoIdentificacion.hashCode() : 0);
        result = 31 * result + (codEstablecimiento != null ? codEstablecimiento.hashCode() : 0);
        result = 31 * result + (codPuntEmision != null ? codPuntEmision.hashCode() : 0);
        result = 31 * result + (secuencial != null ? secuencial.hashCode() : 0);
        result = 31 * result + (fechaEmision != null ? fechaEmision.hashCode() : 0);
        result = 31 * result + (guiaRemision != null ? guiaRemision.hashCode() : 0);
        result = 31 * result + (razonSocialComprador != null ? razonSocialComprador.hashCode() : 0);
        result = 31 * result + (identificacionComprador != null ? identificacionComprador.hashCode() : 0);
        result = 31 * result + (totalSinImpuesto != null ? totalSinImpuesto.hashCode() : 0);
        result = 31 * result + (totalDescuento != null ? totalDescuento.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (propina != null ? propina.hashCode() : 0);
        result = 31 * result + (moneda != null ? moneda.hashCode() : 0);
        result = 31 * result + (infoAdicional != null ? infoAdicional.hashCode() : 0);
        result = 31 * result + (periodoFiscal != null ? periodoFiscal.hashCode() : 0);
        result = 31 * result + (rise != null ? rise.hashCode() : 0);
        result = 31 * result + (fechaInicioTransporte != null ? fechaInicioTransporte.hashCode() : 0);
        result = 31 * result + (fechaFinTransporte != null ? fechaFinTransporte.hashCode() : 0);
        result = 31 * result + (placa != null ? placa.hashCode() : 0);
        result = 31 * result + (fechaEmisionDocSustento != null ? fechaEmisionDocSustento.hashCode() : 0);
        result = 31 * result + (motivoRazon != null ? motivoRazon.hashCode() : 0);
        result = 31 * result + (identificacionDestinatario != null ? identificacionDestinatario.hashCode() : 0);
        result = 31 * result + (razonSocialDestinatario != null ? razonSocialDestinatario.hashCode() : 0);
        result = 31 * result + (direccionDestinatario != null ? direccionDestinatario.hashCode() : 0);
        result = 31 * result + (motivoTraslado != null ? motivoTraslado.hashCode() : 0);
        result = 31 * result + (docAduaneroUnico != null ? docAduaneroUnico.hashCode() : 0);
        result = 31 * result + (codEstablecimientoDest != null ? codEstablecimientoDest.hashCode() : 0);
        result = 31 * result + (ruta != null ? ruta.hashCode() : 0);
        result = 31 * result + (codDocSustento != null ? codDocSustento.hashCode() : 0);
        result = 31 * result + (numDocSustento != null ? numDocSustento.hashCode() : 0);
        result = 31 * result + (numAutDocSustento != null ? numAutDocSustento.hashCode() : 0);
        result = 31 * result + (fecEmisionDocSustento != null ? fecEmisionDocSustento.hashCode() : 0);
        result = 31 * result + (autorizacion != null ? autorizacion.hashCode() : 0);
        result = 31 * result + (fechaautorizacion != null ? fechaautorizacion.hashCode() : 0);
        result = 31 * result + (claveAcceso != null ? claveAcceso.hashCode() : 0);
        result = 31 * result + (importeTotal != null ? importeTotal.hashCode() : 0);
        result = 31 * result + (codigoDocumento != null ? codigoDocumento.hashCode() : 0);
        result = 31 * result + (codDocModificado != null ? codDocModificado.hashCode() : 0);
        result = 31 * result + (numDocModificado != null ? numDocModificado.hashCode() : 0);
        result = 31 * result + (motivoValor != null ? motivoValor.hashCode() : 0);
        result = 31 * result + (tipIdentificacionComprador != null ? tipIdentificacionComprador.hashCode() : 0);
        result = 31 * result + (tipoEmision != null ? tipoEmision.hashCode() : 0);
        result = 31 * result + (partida != null ? partida.hashCode() : 0);
        result = 31 * result + (subtotal12 != null ? subtotal12.hashCode() : 0);
        result = 31 * result + (subtotal0 != null ? subtotal0.hashCode() : 0);
        result = 31 * result + (subtotalNoIva != null ? subtotalNoIva.hashCode() : 0);
        result = 31 * result + (totalvalorIce != null ? totalvalorIce.hashCode() : 0);
        result = 31 * result + (iva12 != null ? iva12.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (estadoTransaccion != null ? estadoTransaccion.hashCode() : 0);
        result = 31 * result + (msjError != null ? msjError.hashCode() : 0);
        result = 31 * result + (tipo != null ? tipo.hashCode() : 0);
        result = 31 * result + (claveAccesoContigente != null ? claveAccesoContigente.hashCode() : 0);
        result = 31 * result + (claveContingencia != null ? claveContingencia.hashCode() : 0);
        result = 31 * result + (docuAutorizacion != null ? docuAutorizacion.hashCode() : 0);
        return result;
    }
}
