package nh321.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PageVersionsModel {

    @XmlElement
    public String id;
    @XmlElement
    public String title;
    @XmlElement
    public long totalVersionCount;
    @XmlElement
    public int versionCount;
    @XmlElement
    public String lastModifier;
    @XmlElement
    public String lastModified;
    @XmlElementWrapper
    @XmlElement(name="attachment")
    public List<AttachmentModel> attachments;

    public PageVersionsModel() {
        this.id = "";
        this.title = "";
        this.versionCount = -1;
        this.totalVersionCount = -1;
        this.lastModifier = "";
        this.lastModified = "";
        this.attachments = new ArrayList<AttachmentModel>();
    }

    public PageVersionsModel(Long id, String title, long totalVersionCount, int versionCount, String lastModifier, Date lastModified, List<AttachmentModel> attachments) {
        this.id = String.valueOf(id);
        this.title = title;
        this.totalVersionCount = totalVersionCount;
        this.versionCount = versionCount;
        this.lastModifier = lastModifier;
        this.lastModified = String.valueOf(lastModified);
        this.attachments = attachments;
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

    public List<AttachmentModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentModel> attachments) {
        this.attachments = attachments;
    }

    public long getTotalVersionCount() {
        return this.totalVersionCount;

    }

    public void setTotalVersionCount(long totalVersionCount) {
        this.totalVersionCount = totalVersionCount;

    }
}