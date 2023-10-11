package com.example.wechat.web;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class WxController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello wechat";
    }

    @GetMapping("/")
    public String check(String signature, String timestamp, String nonce, String echostr) {
//        System.out.println("signature:" + signature);
//        System.out.println("timestamp:" + timestamp);
//        System.out.println("nonce:" + nonce);
//        System.out.println("echostr:" + echostr);

        // 将token，timestamp，nonce三个参数进行字典排序
        String token = "full_stack";
        List<String> list = Arrays.asList(token, timestamp, nonce);
        Collections.sort(list);

        // 将三个参数字符串拼接成一个字符串进行sha1加密
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : list) {
            stringBuilder.append(s);
        }

        // 加密
        try {
            MessageDigest instance = MessageDigest.getInstance("sha1");

            // 使用sha1进行加密获得byte数组
            byte[] digest = instance.digest(stringBuilder.toString().getBytes());
            StringBuilder sum = new StringBuilder();

            for (byte b: digest) {
                sum.append(Integer.toHexString((b >> 4) & 15));
                sum.append(Integer.toHexString(b & 15));
            }
            System.out.println("signature: " + signature);
            System.out.println("sum: " + sum);

            if (!StringUtils.isEmpty(signature) && signature.equals(sum.toString())) {
                return echostr;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return  null;
    }
}
