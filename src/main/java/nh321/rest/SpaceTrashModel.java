package nh321.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SpaceTrashModel {

    @XmlElement
    public String spaceKey;
    @XmlElement
    public String title;
    @XmlElement
    public int numberOfItemsInTrash;

    public SpaceTrashModel() {
        this.spaceKey = "";
        this.title = "";
        this.numberOfItemsInTrash = 0;
    }

    public SpaceTrashModel(String spaceKey, String title, int numberOfItemsInTrash) {
        this.spaceKey = spaceKey;
        this.title = title;
        this.numberOfItemsInTrash = numberOfItemsInTrash;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumberOfItemsInTrash() {
        return numberOfItemsInTrash;
    }

    public void setNumberOfItemsInTrash(int numberOfItemsInTrash) {
        this.numberOfItemsInTrash = numberOfItemsInTrash;
    }
}