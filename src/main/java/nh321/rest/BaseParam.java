package nh321.rest;

import org.springframework.util.StringUtils;

public class BaseParam {

    private String type;
    private int endDays;
    private int limit;
    private ErrorModel errorModel;


    public BaseParam(String type, String endDaysStr, String limitStr, int maxLimit) {
        this.errorModel = new ErrorModel();
        validateType(type);
        validateEndDays(endDaysStr);
        validateLimit(limitStr, maxLimit);

    }

    public BaseParam(String limitStr, int maxLimit) {
        this.errorModel = new ErrorModel();
        validateLimit(limitStr, maxLimit);
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


    public ErrorModel getErrorModel() {
        return errorModel;
    }

    private void validateType(String type) {
        if(StringUtils.isEmpty(type)) {
            type = "all";
        }
        if ("all".equals(type) || "page".equals(type) || "attachment".equals(type)) {
            this.type = type;
        } else {
            this.errorModel.setStatusCode("400 Bad Request");
            this.errorModel.addMessage("type is invalid. : type=all or type=page or type=attachment");
        }
    }

    private void validateEndDays(String endDaysStr) {
        if(StringUtils.isEmpty(endDaysStr)) {
            endDaysStr = "0";
        }
        try {
            this.endDays = Integer.parseInt(endDaysStr);
            if (endDays < 0 ) {
                this.errorModel.setStatusCode("400 Bad Request");
                this.errorModel.addMessage("endDays is invalid. : endDays=0～ ");
            }
        } catch (NumberFormatException e) {
            this.errorModel.setStatusCode("400 Bad Request");
            this.errorModel.addMessage("endDays is invalid. : endDays=0～ ");
        }
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
