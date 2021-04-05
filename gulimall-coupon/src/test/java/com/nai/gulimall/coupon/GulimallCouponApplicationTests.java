package com.nai.gulimall.coupon;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// @SpringBootTest
class GulimallCouponApplicationTests {

  @Test
  void contextLoads() {
    // 2021-04-03 00:00:00   2021-04-05 23:59:59

    System.out.println(
        LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
            .format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss")));
  }
}
