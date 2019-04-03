package nh321.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseModel {

    @XmlElement
    public String itemType;
    @XmlElement
    public String endDays;
    @XmlElement
    public int itemCountLimit;
    @XmlElement
    public int targetItemCount;
    @XmlElement
    public long deletedItemCount;
    @XmlElement
    public long deletedVersionCount;
    @XmlElement
    public Object result;

    public ResponseModel(String itemType, int endDays, int itemCountLimit, int targetItemCount, long deletedItemCount, long deletedVersionCount, Object result) {
        this.itemType = itemType;
        this.endDays = String.valueOf(endDays);
        this.itemCountLimit = itemCountLimit;
        this.targetItemCount = targetItemCount;
        this.deletedItemCount = deletedItemCount;
        this.deletedVersionCount = deletedVersionCount;
        this.result = result;
    }

    public ResponseModel(String itemType, String endDaysStr, int itemCountLimit, int targetItemCount, long deletedItemCount, long deletedVersionCount, Object result) {
        this.itemType = itemType;
        this.endDays = endDaysStr;
        this.itemCountLimit = itemCountLimit;
        this.targetItemCount = targetItemCount;
        this.deletedItemCount = deletedItemCount;
        this.deletedVersionCount = deletedVersionCount;
        this.result = result;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getEndDays() {
        return endDays;
    }

    public void setEndDays(String endDays) {
        this.endDays = endDays;
    }

    public int getItemCountLimit() {
        return itemCountLimit;
    }

    public void setItemCountLimit(int itemCountLimit) {
        this.itemCountLimit = itemCountLimit;
    }

    public long getTargetItemCount() {
        return targetItemCount;
    }

    public void setTargetItemCount(int targetItemCount) {
        this.targetItemCount = targetItemCount;
    }

    public long getDeletedItemCount() {
        return deletedItemCount;
    }

    public void setDeletedItemCount(long deletedItemCount) {
        this.deletedItemCount = deletedItemCount;
    }

    public long getDeletedVersionCount() {
        return deletedVersionCount;
    }

    public void setDeletedVersionCount(long deletedVersionCount) {
        this.deletedVersionCount = deletedVersionCount;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
