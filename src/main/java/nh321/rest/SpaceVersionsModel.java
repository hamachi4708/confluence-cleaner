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
public class SpaceVersionsModel {

    @XmlElement
    public String spaceKey;
    @XmlElement
    public String title;
    @XmlElement
    public long totalVersionCount;
    @XmlElementWrapper
    @XmlElement(name="page")
    public List<PageVersionsModel> pages;

    public SpaceVersionsModel() {
        this.spaceKey = "";
        this.title = "";
        this.totalVersionCount = -1;
        this.pages = new ArrayList<PageVersionsModel>();
    }

    public SpaceVersionsModel(String spaceKey, String title, long totalVersionCount, List<PageVersionsModel> pages) {
        this.spaceKey = spaceKey;
        this.title = title;
        this.totalVersionCount = totalVersionCount;
        this.pages = pages;
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

    public long getTotalVersionCount() {
        return totalVersionCount;
    }

    public void setTotalVersionCount(long totalVersionCount) {
        this.totalVersionCount = totalVersionCount;
    }

    public List<PageVersionsModel> getPages() {
        return pages;
    }

    public void setPages(List<PageVersionsModel> pages) {
        this.pages = pages;
    }
}