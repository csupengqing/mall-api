package org.csu.api;

import org.csu.api.domain.User;
import org.csu.api.persistence.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CsuMallApiApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {

        User user = userMapper.selectById(1);
        System.out.println(user);
    }

}
