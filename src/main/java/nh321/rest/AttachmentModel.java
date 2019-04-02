package nh321.rest;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AttachmentModel {

    @XmlElement
    public String id;
    @XmlElement
    public String title;
    @XmlElement
    public int versionCount;
    @XmlElement
    public String lastModifier;
    @XmlElement
    public String lastModified;

    public AttachmentModel() {
        this.id = "";
        this.title = "";
        this.versionCount = -1;
        this.lastModifier = "";
        this.lastModified = "";
    }

    public AttachmentModel(Long id, String title, int versionCount, String lastModifier, Date lastModified) {
        this.id = String.valueOf(id);
        this.title = title;
        this.versionCount = versionCount;
        this.lastModifier = lastModifier;
        this.lastModified = String.valueOf(lastModified);
    }

    public String getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = String.valueOf(id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = String.valueOf(lastModified);
    }
}