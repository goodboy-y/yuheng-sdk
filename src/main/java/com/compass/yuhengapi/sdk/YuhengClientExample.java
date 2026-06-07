package com.compass.yuhengapi.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YuhengClientExample {

    public static void main(String[] args) {
        YuhengClient client = new YuhengClient("http://127.0.0.1:8520/yuheng-api/api", "w8hpmb7T1xbWmsGr", "XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW");

        try {
//            YuhengResponse response1 = client.queryData("/person");
//            System.out.println("无参数查询结果: " + response1.getMessage());
//            System.out.println(response1.getData().getClass());
//
//           if(response1.getData() instanceof ArrayList a){
//               for (Object o : a) {
//                   System.out.println(o.getClass());
//               }
//           }
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("name", "张三");
//            params.put("status", 1);
//            YuhengResponse response2 = client.queryData("/person", params);
//            System.out.println("带参数查询结果: " + response2.getMessage());
//
//            YuhengResponse response3 = client.queryPage("/person", params, 1, 20);
//            System.out.println("分页查询结果: " + response3.getMessage());

            YuhengResponse yuhengResponse = client.queryAllPages("/person",new HashMap<>(),200);
            System.out.println("查询所有结果: " + yuhengResponse.getMessage());
            System.out.println(yuhengResponse.getData());

        } finally {
            client.close();
        }
    }
}
