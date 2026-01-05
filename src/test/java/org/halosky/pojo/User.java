package org.halosky.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class User {

    private String userName;

    private Integer age;

    private String email;

    private String workAddress;


    public static List<User> generatorUserList() {
        List<User> userList = new ArrayList<>();

        User user = new User();
        user.setUserName("yanghuan");
        user.setAge(30);
        user.setEmail("82828282882@qq.com");
        user.setWorkAddress("china");

        User user1 = new User();
        user1.setUserName("yanghuan");
        user1.setAge(30);
        user1.setEmail("82828282882@qq.com");
        user1.setWorkAddress("china");

        User user2 = new User();
        user2.setUserName("yanghuan");
        user2.setAge(30);
        user2.setEmail("82828282882@qq.com");
        user2.setWorkAddress("china");

        userList.add(user);
        userList.add(user1);
        userList.add(user2);

        return userList;
    }

}
