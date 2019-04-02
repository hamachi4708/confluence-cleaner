package nh321.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseModel {

    @XmlElement
    public String type;
    @XmlElement
    public int endDays;
    @XmlElement
    public int limit;
    @XmlElement
    public long targetCount;
    @XmlElement
    public long deletedCount;
    @XmlElement
    public Object result;

    public ResponseModel(String type, int endDays, int limit, long targetCount, long deletedCount, Object result) {
        this.type = type;
        this.endDays = endDays;
        this.limit = limit;
        this.targetCount = targetCount;
        this.deletedCount = deletedCount;
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getEndDays() {
        return endDays;
    }

    public void setEndDays(int endDays) {
        this.endDays = endDays;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(long targetCount) {
        this.targetCount = targetCount;
    }

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
