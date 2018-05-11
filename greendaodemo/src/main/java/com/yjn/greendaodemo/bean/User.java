package com.yjn.greendaodemo.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by yangjianan on 2018/5/10.
 */

@Entity(nameInDb = "HAHHA")
public class User {
    @Id
    private Long id;
    @Property(nameInDb = "USERNAME")
    private String userName;
    @Property(nameInDb = "AGE")
    private int age;
    @Property(nameInDb = "SEX")
    private String sex;
    @Generated(hash = 1887126858)
    public User(Long id, String userName, int age, String sex) {
        this.id = id;
        this.userName = userName;
        this.age = age;
        this.sex = sex;
    }
    @Generated(hash = 586692638)
    public User() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public int getAge() {
        return this.age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getSex() {
        return this.sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }

}
