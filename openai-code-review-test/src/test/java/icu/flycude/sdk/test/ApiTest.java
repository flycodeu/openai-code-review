package icu.flycude.sdk.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Test
    public void test() {
        System.out.println(Integer.parseInt("aaa1"));
        System.out.println(Integer.parseInt("111"));
        System.out.println(Integer.parseInt("s221"));
        System.out.println(Integer.parseInt("a+a"));
        System.out.println( 0 == 1);
    }
}
