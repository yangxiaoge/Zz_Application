package com.yjn.greendaodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.yjn.greendaodemo.bean.User;
import com.yjn.greendaodemo.bean.UserDao;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userDao = MyApplication.getDaoInstant().getUserDao();

    }

    //查
    private void queryData() {
        List<User> user = userDao.queryBuilder().where(UserDao.Properties.Id.between(6, 20)).limit(5).build().list();
        if (user != null && user.size() > 0) {
            Toast.makeText(this, String.format("查询到%s条符合条件的数据", user.size()), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未查到符合条件的数据", Toast.LENGTH_SHORT).show();
        }
    }

    //改
    private void updateData() {
        //更改一条满足条件的数据
        User user = userDao.queryBuilder().where(UserDao.Properties.Id.eq(2)).build().unique();
        if (user != null) {
            user.setUserName("珠珠珠");
            userDao.update(user);
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "用户数据不存在", Toast.LENGTH_SHORT).show();
        }
    }

    //删
    private void deleteData() {
        //查询id>8的数据
        List<User> list = userDao.queryBuilder().where(UserDao.Properties.Id.gt(8)).build().list();
        for (User user : list) {
            userDao.delete(user);
        }
        //查询id=1的一条数据
        User uniqueUser = userDao.queryBuilder().where(UserDao.Properties.Id.eq(1)).build().unique();
        if (uniqueUser != null) {
            userDao.deleteByKey(uniqueUser.getId());
        }
    }

    //增
    private void insertData() {
        User user = new User(null, "羊" + new Random().nextInt(20), 18, new Random().nextInt(20) % 2 == 2 ? "男" : "女"/*, "中国"*/);
        userDao.insert(user);
        userDao.queryBuilder().list();
        Toast.makeText(this, "插入成功", Toast.LENGTH_SHORT).show();
    }

    public void insertData(View view) {
        insertData();
    }

    public void deleteData(View view) {
        deleteData();
    }

    public void updateData(View view) {
        updateData();
    }

    public void queryData(View view) {
        queryData();
    }
}
