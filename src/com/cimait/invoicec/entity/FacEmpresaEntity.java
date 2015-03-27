package com.cimait.invoicec.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
@javax.persistence.Table(name = "fac_empresa")
public class FacEmpresaEntity {
    private String ruc;

    @Id
    @javax.persistence.Column(name = "\"Ruc\"")
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    private String razonSocial;

    @Basic
    @javax.persistence.Column(name = "\"RazonSocial\"")
    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    private String razonComercial;

    @Basic
    @javax.persistence.Column(name = "\"RazonComercial\"")
    public String getRazonComercial() {
        return razonComercial;
    }

    public void setRazonComercial(String razonComercial) {
        this.razonComercial = razonComercial;
    }

    private String direccionMatriz;

    @Basic
    @javax.persistence.Column(name = "\"DireccionMatriz\"")
    public String getDireccionMatriz() {
        return direccionMatriz;
    }

    public void setDireccionMatriz(String direccionMatriz) {
        this.direccionMatriz = direccionMatriz;
    }

    private Integer contribEspecial;

    @Basic
    @javax.persistence.Column(name = "\"ContribEspecial\"")
    public Integer getContribEspecial() {
        return contribEspecial;
    }

    public void setContribEspecial(Integer contribEspecial) {
        this.contribEspecial = contribEspecial;
    }

    private String obligContabilidad;

    @Basic
    @javax.persistence.Column(name = "\"ObligContabilidad\"")
    public String getObligContabilidad() {
        return obligContabilidad;
    }

    public void setObligContabilidad(String obligContabilidad) {
        this.obligContabilidad = obligContabilidad;
    }

    private String pathCompGenerados;

    @Basic
    @javax.persistence.Column(name = "\"PathCompGenerados\"")
    public String getPathCompGenerados() {
        return pathCompGenerados;
    }

    public void setPathCompGenerados(String pathCompGenerados) {
        this.pathCompGenerados = pathCompGenerados;
    }

    private String pathCompFirmados;

    @Basic
    @javax.persistence.Column(name = "\"PathCompFirmados\"")
    public String getPathCompFirmados() {
        return pathCompFirmados;
    }

    public void setPathCompFirmados(String pathCompFirmados) {
        this.pathCompFirmados = pathCompFirmados;
    }

    private String pathCompAutorizados;

    @Basic
    @javax.persistence.Column(name = "\"PathCompAutorizados\"")
    public String getPathCompAutorizados() {
        return pathCompAutorizados;
    }

    public void setPathCompAutorizados(String pathCompAutorizados) {
        this.pathCompAutorizados = pathCompAutorizados;
    }

    private String pathCompNoAutorizados;

    @Basic
    @javax.persistence.Column(name = "\"PathCompNoAutorizados\"")
    public String getPathCompNoAutorizados() {
        return pathCompNoAutorizados;
    }

    public void setPathCompNoAutorizados(String pathCompNoAutorizados) {
        this.pathCompNoAutorizados = pathCompNoAutorizados;
    }

    private String pathInfoRecibida;

    @Basic
    @javax.persistence.Column(name = "\"PathInfoRecibida\"")
    public String getPathInfoRecibida() {
        return pathInfoRecibida;
    }

    public void setPathInfoRecibida(String pathInfoRecibida) {
        this.pathInfoRecibida = pathInfoRecibida;
    }

    private String urlWebServices;

    @Basic
    @javax.persistence.Column(name = "\"UrlWebServices\"")
    public String getUrlWebServices() {
        return urlWebServices;
    }

    public void setUrlWebServices(String urlWebServices) {
        this.urlWebServices = urlWebServices;
    }

    private String colorEmpresa;

    @Basic
    @javax.persistence.Column(name = "\"ColorEmpresa\"")
    public String getColorEmpresa() {
        return colorEmpresa;
    }

    public void setColorEmpresa(String colorEmpresa) {
        this.colorEmpresa = colorEmpresa;
    }

    private String pathLogoEmpresa;

    @Basic
    @javax.persistence.Column(name = "\"PathLogoEmpresa\"")
    public String getPathLogoEmpresa() {
        return pathLogoEmpresa;
    }

    public void setPathLogoEmpresa(String pathLogoEmpresa) {
        this.pathLogoEmpresa = pathLogoEmpresa;
    }

    private String pathFirma;

    @Basic
    @javax.persistence.Column(name = "\"PathFirma\"")
    public String getPathFirma() {
        return pathFirma;
    }

    public void setPathFirma(String pathFirma) {
        this.pathFirma = pathFirma;
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

    private Integer puertoSmtp;

    @Basic
    @javax.persistence.Column(name = "\"puertoSMTP\"")
    public Integer getPuertoSmtp() {
        return puertoSmtp;
    }

    public void setPuertoSmtp(Integer puertoSmtp) {
        this.puertoSmtp = puertoSmtp;
    }

    private String servidorSmtp;

    @Basic
    @javax.persistence.Column(name = "\"servidorSMTP\"")
    public String getServidorSmtp() {
        return servidorSmtp;
    }

    public void setServidorSmtp(String servidorSmtp) {
        this.servidorSmtp = servidorSmtp;
    }

    private Boolean sslSmtp;

    @Basic
    @javax.persistence.Column(name = "\"sslSMTP\"")
    public Boolean getSslSmtp() {
        return sslSmtp;
    }

    public void setSslSmtp(Boolean sslSmtp) {
        this.sslSmtp = sslSmtp;
    }

    private String userSmtp;

    @Basic
    @javax.persistence.Column(name = "\"userSMTP\"")
    public String getUserSmtp() {
        return userSmtp;
    }

    public void setUserSmtp(String userSmtp) {
        this.userSmtp = userSmtp;
    }

    private String passSmtp;

    @Basic
    @javax.persistence.Column(name = "\"passSMTP\"")
    public String getPassSmtp() {
        return passSmtp;
    }

    public void setPassSmtp(String passSmtp) {
        this.passSmtp = passSmtp;
    }

    private String emailEnvio;

    @Basic
    @javax.persistence.Column(name = "\"emailEnvio\"")
    public String getEmailEnvio() {
        return emailEnvio;
    }

    public void setEmailEnvio(String emailEnvio) {
        this.emailEnvio = emailEnvio;
    }

    private String marcaAgua;

    @Basic
    @javax.persistence.Column(name = "\"marcaAgua\"")
    public String getMarcaAgua() {
        return marcaAgua;
    }

    public void setMarcaAgua(String marcaAgua) {
        this.marcaAgua = marcaAgua;
    }

    private String pathMarcaAgua;

    @Basic
    @javax.persistence.Column(name = "\"pathMarcaAgua\"")
    public String getPathMarcaAgua() {
        return pathMarcaAgua;
    }

    public void setPathMarcaAgua(String pathMarcaAgua) {
        this.pathMarcaAgua = pathMarcaAgua;
    }

    private String pathCompRecepcion;

    @Basic
    @javax.persistence.Column(name = "\"PathCompRecepcion\"")
    public String getPathCompRecepcion() {
        return pathCompRecepcion;
    }

    public void setPathCompRecepcion(String pathCompRecepcion) {
        this.pathCompRecepcion = pathCompRecepcion;
    }

    private String correoRecepcion;

    @Basic
    @javax.persistence.Column(name = "\"CorreoRecepcion\"")
    public String getCorreoRecepcion() {
        return correoRecepcion;
    }

    public void setCorreoRecepcion(String correoRecepcion) {
        this.correoRecepcion = correoRecepcion;
    }

    private String pathCompContingencia;

    @Basic
    @javax.persistence.Column(name = "\"PathCompContingencia\"")
    public String getPathCompContingencia() {
        return pathCompContingencia;
    }

    public void setPathCompContingencia(String pathCompContingencia) {
        this.pathCompContingencia = pathCompContingencia;
    }

    private String fechaResolucionContribEspecial;

    @Basic
    @javax.persistence.Column(name = "\"FechaResolucionContribEspecial\"")
    public String getFechaResolucionContribEspecial() {
        return fechaResolucionContribEspecial;
    }

    public void setFechaResolucionContribEspecial(String fechaResolucionContribEspecial) {
        this.fechaResolucionContribEspecial = fechaResolucionContribEspecial;
    }

    private String passFirma;

    @Basic
    @javax.persistence.Column(name = "\"PassFirma\"")
    public String getPassFirma() {
        return passFirma;
    }

    public void setPassFirma(String passFirma) {
        this.passFirma = passFirma;
    }

    private String typeFirma;

    @Basic
    @javax.persistence.Column(name = "\"TypeFirma\"")
    public String getTypeFirma() {
        return typeFirma;
    }

    public void setTypeFirma(String typeFirma) {
        this.typeFirma = typeFirma;
    }

    private String pathXsd;

    @Basic
    @javax.persistence.Column(name = "\"PathXSD\"")
    public String getPathXsd() {
        return pathXsd;
    }

    public void setPathXsd(String pathXsd) {
        this.pathXsd = pathXsd;
    }

    private String pathJasper;

    @Basic
    @javax.persistence.Column(name = "\"PathJasper\"")
    public String getPathJasper() {
        return pathJasper;
    }

    public void setPathJasper(String pathJasper) {
        this.pathJasper = pathJasper;
    }

    private String rucFirmante;

    @Basic
    @javax.persistence.Column(name = "\"ruc_firmante\"")
    public String getRucFirmante() {
        return rucFirmante;
    }

    public void setRucFirmante(String rucFirmante) {
        this.rucFirmante = rucFirmante;
    }

    private Integer numContribuyenteEspecial;

    @Basic
    @javax.persistence.Column(name = "\"numContribuyenteEspecial\"")
    public Integer getNumContribuyenteEspecial() {
        return numContribuyenteEspecial;
    }

    public void setNumContribuyenteEspecial(Integer numContribuyenteEspecial) {
        this.numContribuyenteEspecial = numContribuyenteEspecial;
    }

    private String ubicacion;

    @Basic
    @javax.persistence.Column(name = "\"ubicacion\"")
    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    private String casilla;

    @Basic
    @javax.persistence.Column(name = "\"casilla\"")
    public String getCasilla() {
        return casilla;
    }

    public void setCasilla(String casilla) {
        this.casilla = casilla;
    }

    private Timestamp fechaCreacion;

    @Basic
    @javax.persistence.Column(name = "\"fechaCreacion\"")
    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    private Timestamp fechaModificacion;

    @Basic
    @javax.persistence.Column(name = "\"fechaModificacion\"")
    public Timestamp getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Timestamp fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    private String userCreacion;

    @Basic
    @javax.persistence.Column(name = "\"userCreacion\"")
    public String getUserCreacion() {
        return userCreacion;
    }

    public void setUserCreacion(String userCreacion) {
        this.userCreacion = userCreacion;
    }

    private String userModificacion;

    @Basic
    @javax.persistence.Column(name = "\"userModificacion\"")
    public String getUserModificacion() {
        return userModificacion;
    }

    public void setUserModificacion(String userModificacion) {
        this.userModificacion = userModificacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacEmpresaEntity that = (FacEmpresaEntity) o;

        if (casilla != null ? !casilla.equals(that.casilla) : that.casilla != null) return false;
        if (colorEmpresa != null ? !colorEmpresa.equals(that.colorEmpresa) : that.colorEmpresa != null) return false;
        if (contribEspecial != null ? !contribEspecial.equals(that.contribEspecial) : that.contribEspecial != null)
            return false;
        if (correoRecepcion != null ? !correoRecepcion.equals(that.correoRecepcion) : that.correoRecepcion != null)
            return false;
        if (direccionMatriz != null ? !direccionMatriz.equals(that.direccionMatriz) : that.direccionMatriz != null)
            return false;
        if (emailEnvio != null ? !emailEnvio.equals(that.emailEnvio) : that.emailEnvio != null) return false;
        if (fechaCreacion != null ? !fechaCreacion.equals(that.fechaCreacion) : that.fechaCreacion != null)
            return false;
        if (fechaModificacion != null ? !fechaModificacion.equals(that.fechaModificacion) : that.fechaModificacion != null)
            return false;
        if (fechaResolucionContribEspecial != null ? !fechaResolucionContribEspecial.equals(that.fechaResolucionContribEspecial) : that.fechaResolucionContribEspecial != null)
            return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (marcaAgua != null ? !marcaAgua.equals(that.marcaAgua) : that.marcaAgua != null) return false;
        if (numContribuyenteEspecial != null ? !numContribuyenteEspecial.equals(that.numContribuyenteEspecial) : that.numContribuyenteEspecial != null)
            return false;
        if (obligContabilidad != null ? !obligContabilidad.equals(that.obligContabilidad) : that.obligContabilidad != null)
            return false;
        if (passFirma != null ? !passFirma.equals(that.passFirma) : that.passFirma != null) return false;
        if (passSmtp != null ? !passSmtp.equals(that.passSmtp) : that.passSmtp != null) return false;
        if (pathCompAutorizados != null ? !pathCompAutorizados.equals(that.pathCompAutorizados) : that.pathCompAutorizados != null)
            return false;
        if (pathCompContingencia != null ? !pathCompContingencia.equals(that.pathCompContingencia) : that.pathCompContingencia != null)
            return false;
        if (pathCompFirmados != null ? !pathCompFirmados.equals(that.pathCompFirmados) : that.pathCompFirmados != null)
            return false;
        if (pathCompGenerados != null ? !pathCompGenerados.equals(that.pathCompGenerados) : that.pathCompGenerados != null)
            return false;
        if (pathCompNoAutorizados != null ? !pathCompNoAutorizados.equals(that.pathCompNoAutorizados) : that.pathCompNoAutorizados != null)
            return false;
        if (pathCompRecepcion != null ? !pathCompRecepcion.equals(that.pathCompRecepcion) : that.pathCompRecepcion != null)
            return false;
        if (pathFirma != null ? !pathFirma.equals(that.pathFirma) : that.pathFirma != null) return false;
        if (pathInfoRecibida != null ? !pathInfoRecibida.equals(that.pathInfoRecibida) : that.pathInfoRecibida != null)
            return false;
        if (pathJasper != null ? !pathJasper.equals(that.pathJasper) : that.pathJasper != null) return false;
        if (pathLogoEmpresa != null ? !pathLogoEmpresa.equals(that.pathLogoEmpresa) : that.pathLogoEmpresa != null)
            return false;
        if (pathMarcaAgua != null ? !pathMarcaAgua.equals(that.pathMarcaAgua) : that.pathMarcaAgua != null)
            return false;
        if (pathXsd != null ? !pathXsd.equals(that.pathXsd) : that.pathXsd != null) return false;
        if (puertoSmtp != null ? !puertoSmtp.equals(that.puertoSmtp) : that.puertoSmtp != null) return false;
        if (razonComercial != null ? !razonComercial.equals(that.razonComercial) : that.razonComercial != null)
            return false;
        if (razonSocial != null ? !razonSocial.equals(that.razonSocial) : that.razonSocial != null) return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (rucFirmante != null ? !rucFirmante.equals(that.rucFirmante) : that.rucFirmante != null) return false;
        if (servidorSmtp != null ? !servidorSmtp.equals(that.servidorSmtp) : that.servidorSmtp != null) return false;
        if (sslSmtp != null ? !sslSmtp.equals(that.sslSmtp) : that.sslSmtp != null) return false;
        if (typeFirma != null ? !typeFirma.equals(that.typeFirma) : that.typeFirma != null) return false;
        if (ubicacion != null ? !ubicacion.equals(that.ubicacion) : that.ubicacion != null) return false;
        if (urlWebServices != null ? !urlWebServices.equals(that.urlWebServices) : that.urlWebServices != null)
            return false;
        if (userCreacion != null ? !userCreacion.equals(that.userCreacion) : that.userCreacion != null) return false;
        if (userModificacion != null ? !userModificacion.equals(that.userModificacion) : that.userModificacion != null)
            return false;
        if (userSmtp != null ? !userSmtp.equals(that.userSmtp) : that.userSmtp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (razonSocial != null ? razonSocial.hashCode() : 0);
        result = 31 * result + (razonComercial != null ? razonComercial.hashCode() : 0);
        result = 31 * result + (direccionMatriz != null ? direccionMatriz.hashCode() : 0);
        result = 31 * result + (contribEspecial != null ? contribEspecial.hashCode() : 0);
        result = 31 * result + (obligContabilidad != null ? obligContabilidad.hashCode() : 0);
        result = 31 * result + (pathCompGenerados != null ? pathCompGenerados.hashCode() : 0);
        result = 31 * result + (pathCompFirmados != null ? pathCompFirmados.hashCode() : 0);
        result = 31 * result + (pathCompAutorizados != null ? pathCompAutorizados.hashCode() : 0);
        result = 31 * result + (pathCompNoAutorizados != null ? pathCompNoAutorizados.hashCode() : 0);
        result = 31 * result + (pathInfoRecibida != null ? pathInfoRecibida.hashCode() : 0);
        result = 31 * result + (urlWebServices != null ? urlWebServices.hashCode() : 0);
        result = 31 * result + (colorEmpresa != null ? colorEmpresa.hashCode() : 0);
        result = 31 * result + (pathLogoEmpresa != null ? pathLogoEmpresa.hashCode() : 0);
        result = 31 * result + (pathFirma != null ? pathFirma.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (puertoSmtp != null ? puertoSmtp.hashCode() : 0);
        result = 31 * result + (servidorSmtp != null ? servidorSmtp.hashCode() : 0);
        result = 31 * result + (sslSmtp != null ? sslSmtp.hashCode() : 0);
        result = 31 * result + (userSmtp != null ? userSmtp.hashCode() : 0);
        result = 31 * result + (passSmtp != null ? passSmtp.hashCode() : 0);
        result = 31 * result + (emailEnvio != null ? emailEnvio.hashCode() : 0);
        result = 31 * result + (marcaAgua != null ? marcaAgua.hashCode() : 0);
        result = 31 * result + (pathMarcaAgua != null ? pathMarcaAgua.hashCode() : 0);
        result = 31 * result + (pathCompRecepcion != null ? pathCompRecepcion.hashCode() : 0);
        result = 31 * result + (correoRecepcion != null ? correoRecepcion.hashCode() : 0);
        result = 31 * result + (pathCompContingencia != null ? pathCompContingencia.hashCode() : 0);
        result = 31 * result + (fechaResolucionContribEspecial != null ? fechaResolucionContribEspecial.hashCode() : 0);
        result = 31 * result + (passFirma != null ? passFirma.hashCode() : 0);
        result = 31 * result + (typeFirma != null ? typeFirma.hashCode() : 0);
        result = 31 * result + (pathXsd != null ? pathXsd.hashCode() : 0);
        result = 31 * result + (pathJasper != null ? pathJasper.hashCode() : 0);
        result = 31 * result + (rucFirmante != null ? rucFirmante.hashCode() : 0);
        result = 31 * result + (numContribuyenteEspecial != null ? numContribuyenteEspecial.hashCode() : 0);
        result = 31 * result + (ubicacion != null ? ubicacion.hashCode() : 0);
        result = 31 * result + (casilla != null ? casilla.hashCode() : 0);
        result = 31 * result + (fechaCreacion != null ? fechaCreacion.hashCode() : 0);
        result = 31 * result + (fechaModificacion != null ? fechaModificacion.hashCode() : 0);
        result = 31 * result + (userCreacion != null ? userCreacion.hashCode() : 0);
        result = 31 * result + (userModificacion != null ? userModificacion.hashCode() : 0);
        return result;
    }
}
