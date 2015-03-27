package com.cimait.invoicec.entity;

import javax.persistence.*;


@Entity
@Table(name = "fac_general")
public class FacGeneralEntity {
    private String codTabla;
    private String codUnico;
    private String descripcion;
    private String isActive;
    private int idGeneral;
    private Integer porcentaje;
    private String resort;
    private String valor;

    @Basic
    @Column(name = "codTabla")
    public String getCodTabla() {
        return codTabla;
    }

    public void setCodTabla(String codTabla) {
        this.codTabla = codTabla;
    }

    @Basic
    @Column(name = "codUnico")
    public String getCodUnico() {
        return codUnico;
    }

    public void setCodUnico(String codUnico) {
        this.codUnico = codUnico;
    }

    @Basic
    @Column(name = "descripcion")
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Basic
    @Column(name = "isActive")
    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    @Id
    @Column(name = "idGeneral")
    public int getIdGeneral() {
        return idGeneral;
    }

    public void setIdGeneral(int idGeneral) {
        this.idGeneral = idGeneral;
    }

    @Basic
    @Column(name = "porcentaje")
    public Integer getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Integer porcentaje) {
        this.porcentaje = porcentaje;
    }

    @Basic
    @Column(name = "resort")
    public String getResort() {
        return resort;
    }

    public void setResort(String resort) {
        this.resort = resort;
    }

    @Basic
    @Column(name = "valor")
    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacGeneralEntity that = (FacGeneralEntity) o;

        if (idGeneral != that.idGeneral) return false;
        if (codTabla != null ? !codTabla.equals(that.codTabla) : that.codTabla != null) return false;
        if (codUnico != null ? !codUnico.equals(that.codUnico) : that.codUnico != null) return false;
        if (descripcion != null ? !descripcion.equals(that.descripcion) : that.descripcion != null) return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (porcentaje != null ? !porcentaje.equals(that.porcentaje) : that.porcentaje != null) return false;
        if (resort != null ? !resort.equals(that.resort) : that.resort != null) return false;
        if (valor != null ? !valor.equals(that.valor) : that.valor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = codTabla != null ? codTabla.hashCode() : 0;
        result = 31 * result + (codUnico != null ? codUnico.hashCode() : 0);
        result = 31 * result + (descripcion != null ? descripcion.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + idGeneral;
        result = 31 * result + (porcentaje != null ? porcentaje.hashCode() : 0);
        result = 31 * result + (resort != null ? resort.hashCode() : 0);
        result = 31 * result + (valor != null ? valor.hashCode() : 0);
        return result;
    }
}
