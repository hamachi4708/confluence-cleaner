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
public class AllTrashModel {

    @XmlElementWrapper
    @XmlElement(name="trash")
    public List<SpaceTrashModel> trashList;

    public AllTrashModel() {
        this.trashList = new ArrayList<SpaceTrashModel>();
    }

    public AllTrashModel(List<SpaceTrashModel> trashList) {
        this.trashList = trashList;
    }

    public List<SpaceTrashModel> getTrashList() {
        return trashList;
    }

    public void setTrashList(List<SpaceTrashModel> trashList) {
        this.trashList = trashList;
    }
}