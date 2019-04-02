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
public class ErrorModel {

    @XmlElement(name = "statusCode")
    private String statusCode;

    @XmlElementWrapper
    @XmlElement(name="message")
    public List<MessageModel> messages;

    public ErrorModel() {
        this.statusCode = "";
        this.messages = new ArrayList<MessageModel>();
    }

    public ErrorModel(String statusCode, String messages) {
        this.statusCode = statusCode;
        this.messages = new ArrayList<MessageModel>();
        this.addMessage(messages);
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public List<MessageModel> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageModel> messages) {
        this.messages = messages;
    }

    public void addMessage(String message) {
        this.messages.add(new MessageModel(message));
    }
}