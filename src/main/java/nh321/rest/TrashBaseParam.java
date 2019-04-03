package nh321.rest;

import org.springframework.util.StringUtils;

public class TrashBaseParam {

    private String type;
    private String endDays;
    private int limit;
    private ErrorModel errorModel;


    public TrashBaseParam(String type, String endDaysStr, String limitStr, int maxLimit) {
        this.errorModel = new ErrorModel();
        this.type = type;
        this.endDays = endDaysStr;
        validateLimit(limitStr, maxLimit);

    }

    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getEndDays() {
        return endDays;
    }


    public void setEndDays(String endDays) {
        this.endDays = endDays;
    }


    public int getLimit() {
        return limit;
    }


    public void setLimit(int limit) {
        this.limit = limit;
    }


    public ErrorModel getErrorModel() {
        return errorModel;
    }

    private void validateLimit(String limitStr, int maxLimit) {
        if(StringUtils.isEmpty(limitStr)) {
            limitStr = String.valueOf(maxLimit);
        }
        try {
            this.limit = Integer.parseInt(limitStr);
            if (limit < 1 || limit > maxLimit) {
                this.errorModel.setStatusCode("400 Bad Request");
                this.errorModel.addMessage("limit is invalid. : limit=1～" + String.valueOf(maxLimit));
            }
        } catch (NumberFormatException e) {
            this.errorModel.setStatusCode("400 Bad Request");
            this.errorModel.addMessage("limit is invalid. : limit=1～" + String.valueOf(maxLimit));
        }
    }

}
