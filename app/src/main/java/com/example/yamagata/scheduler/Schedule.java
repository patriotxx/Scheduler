package com.example.yamagata.scheduler;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yamagata on 西暦16/08/23.
 */
public class Schedule extends RealmObject{
    @PrimaryKey
    private long id;
    private Date date;
    private String title;
    private String company;
    private String detail;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany(){
        return company;
    }

    public void setCompany(String company){
        this.company = company;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}

