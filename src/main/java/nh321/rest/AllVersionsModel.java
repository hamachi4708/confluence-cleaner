package nh321.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AllVersionsModel {

    @XmlElementWrapper
    @XmlElement(name="space")
    public List<SpaceVersionsModel> spaces;

    public AllVersionsModel() {
        this.spaces = new ArrayList<SpaceVersionsModel>();
    }

    public AllVersionsModel(List<SpaceVersionsModel> spaces) {
        this.spaces = spaces;
    }

    public List<SpaceVersionsModel> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<SpaceVersionsModel> spaces) {
        this.spaces = spaces;
    }
}