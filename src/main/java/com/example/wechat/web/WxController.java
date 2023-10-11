package com.example.wechat.web;

import com.example.wechat.message.TextMessage;
import com.thoughtworks.xstream.XStream;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    @PostMapping("/")
    public String receiveMessage(HttpServletRequest request) throws IOException {
        System.out.println("收到用户的消息");

        ServletInputStream inputStream = request.getInputStream();
//        byte[] b = new byte[1024];
//        int len;
//        while ((len = inputStream.read(b)) != -1) {
//            System.out.println(new String(b, 0, len));
//        }

        Map<String, String> map = new HashMap<>();
        SAXReader reader = new SAXReader();

        try {
            // 读取Request输入流，获取document对象
            Document document = reader.read(inputStream);
            // 获得root节点
            Element root = document.getRootElement();
            // 获取所有子节点
            List<Element> elements = root.elements();

            //遍历
            for (Element element : elements) {
                map.put(element.getName(), element.getStringValue());
            }

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        System.out.println(map);

        // 回复消息
//        String message = "<xml><ToUserName><![CDATA[oJT3_6mjvcPe7KcV76OA0GaYjm2k]]></ToUserName><FromUserName><![CDATA[gh_b1f97d7f1e08]]></FromUserName><CreateTime>12345678</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[你好]]></Content></xml>";
        String message = getReplyMessage(map);
        System.out.println(message);
        return message;
    }

    /**
     * 获得回复的消息内容
     * @param map
     * @return xml格式的字符串
     */
    private String getReplyMessage(Map<String, String> map) {
        TextMessage textMessage = new TextMessage();
        textMessage.setToUserName(map.get("FromUserName"));
        textMessage.setFromUserName(map.get("ToUserName"));
        textMessage.setMsgType("text");
        textMessage.setContent("欢迎关注本公众号！");
        textMessage.setCreateTime(System.currentTimeMillis()/1000);

        // XStream将java对象转换成xml字符串
        XStream xStream = new XStream();
        xStream.processAnnotations(TextMessage.class);
        String xml = xStream.toXML(textMessage);
        return xml;
    }
}
