package com.cimait.invoicec.entity;

import javax.persistence.*;
import java.sql.Timestamp;


@Entity
@Table(name = "fac_establecimiento")
@IdClass(FacEstablecimientoEntityPK.class)
public class FacEstablecimientoEntity {
    private String ruc;
    private String codEstablecimiento;
    private String direccionEstablecimiento;
    private String isActive;
    private String correo;
    private String mensaje;
    private String pathAnexo;
    private String operaResort;
    private Timestamp fechaCreacion;
    private Timestamp fechaModificacion;
    private String userCreacion;
    private String userModificacion;
    private String local;

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

    @Basic
    @Column(name = "\"DireccionEstablecimiento\"")
    public String getDireccionEstablecimiento() {
        return direccionEstablecimiento;
    }

    public void setDireccionEstablecimiento(String direccionEstablecimiento) {
        this.direccionEstablecimiento = direccionEstablecimiento;
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
    @Column(name = "\"Correo\"")
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Basic
    @Column(name = "\"Mensaje\"")
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Basic
    @Column(name = "\"PathAnexo\"")
    public String getPathAnexo() {
        return pathAnexo;
    }

    public void setPathAnexo(String pathAnexo) {
        this.pathAnexo = pathAnexo;
    }

    @Basic
    @Column(name = "\"OPERA_RESORT\"")
    public String getOperaResort() {
        return operaResort;
    }

    public void setOperaResort(String operaResort) {
        this.operaResort = operaResort;
    }

    @Basic
    @Column(name = "\"fechaCreacion\"")
    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Basic
    @Column(name = "\"fechaModificacion\"")
    public Timestamp getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Timestamp fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    @Basic
    @Column(name = "\"userCreacion\"")
    public String getUserCreacion() {
        return userCreacion;
    }

    public void setUserCreacion(String userCreacion) {
        this.userCreacion = userCreacion;
    }

    @Basic
    @Column(name = "\"userModificacion\"")
    public String getUserModificacion() {
        return userModificacion;
    }

    public void setUserModificacion(String userModificacion) {
        this.userModificacion = userModificacion;
    }

    @Basic
    @Column(name = "\"local\"")
    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacEstablecimientoEntity that = (FacEstablecimientoEntity) o;

        if (codEstablecimiento != null ? !codEstablecimiento.equals(that.codEstablecimiento) : that.codEstablecimiento != null)
            return false;
        if (correo != null ? !correo.equals(that.correo) : that.correo != null) return false;
        if (direccionEstablecimiento != null ? !direccionEstablecimiento.equals(that.direccionEstablecimiento) : that.direccionEstablecimiento != null)
            return false;
        if (fechaCreacion != null ? !fechaCreacion.equals(that.fechaCreacion) : that.fechaCreacion != null)
            return false;
        if (fechaModificacion != null ? !fechaModificacion.equals(that.fechaModificacion) : that.fechaModificacion != null)
            return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (local != null ? !local.equals(that.local) : that.local != null) return false;
        if (mensaje != null ? !mensaje.equals(that.mensaje) : that.mensaje != null) return false;
        if (operaResort != null ? !operaResort.equals(that.operaResort) : that.operaResort != null) return false;
        if (pathAnexo != null ? !pathAnexo.equals(that.pathAnexo) : that.pathAnexo != null) return false;
        if (ruc != null ? !ruc.equals(that.ruc) : that.ruc != null) return false;
        if (userCreacion != null ? !userCreacion.equals(that.userCreacion) : that.userCreacion != null) return false;
        if (userModificacion != null ? !userModificacion.equals(that.userModificacion) : that.userModificacion != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ruc != null ? ruc.hashCode() : 0;
        result = 31 * result + (codEstablecimiento != null ? codEstablecimiento.hashCode() : 0);
        result = 31 * result + (direccionEstablecimiento != null ? direccionEstablecimiento.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (correo != null ? correo.hashCode() : 0);
        result = 31 * result + (mensaje != null ? mensaje.hashCode() : 0);
        result = 31 * result + (pathAnexo != null ? pathAnexo.hashCode() : 0);
        result = 31 * result + (operaResort != null ? operaResort.hashCode() : 0);
        result = 31 * result + (fechaCreacion != null ? fechaCreacion.hashCode() : 0);
        result = 31 * result + (fechaModificacion != null ? fechaModificacion.hashCode() : 0);
        result = 31 * result + (userCreacion != null ? userCreacion.hashCode() : 0);
        result = 31 * result + (userModificacion != null ? userModificacion.hashCode() : 0);
        result = 31 * result + (local != null ? local.hashCode() : 0);
        return result;
    }
}
