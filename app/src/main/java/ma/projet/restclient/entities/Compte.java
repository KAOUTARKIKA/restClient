package ma.projet.restclient.entities;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "item", strict = false)
public class Compte {
    @Element(name = "id", required = false)
    private Long id;

    @Element(name = "solde", required = false)
    private double solde;

    @Element(name = "type", required = false)
    private String type;

    @Element(name = "dateCreation", required = false)
    private String dateCreation;

    public Compte() {}

    public Compte(Long id, double solde, String type, String dateCreation) {
        this.id = id;
        this.solde = solde;
        this.type = type;
        this.dateCreation = dateCreation;
    }

    public Long getId() {
        return id;
    }

    public double getSolde() {
        return solde;
    }

    public String getType() {
        return type;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "Compte{" +
                "id=" + id +
                ", solde=" + solde +
                ", type='" + type + '\'' +
                ", dateCreation='" + dateCreation + '\'' +
                '}';
    }
}