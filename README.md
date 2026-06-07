# 玉衡API Java SDK 使用文档

## 简介

玉衡API Java SDK 是用于调用玉衡API服务的Java客户端库，提供了简洁的API接口，支持数据查询、分页查询、自动获取所有数据等功能。

## 依赖配置

### Maven

```xml
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'org.apache.httpcomponents.client5:httpclient5:5.2.1'
implementation 'com.fasterxml.jackson.core:jackson-databind:2.16.1'
```

## 快速开始

### 1. 初始化客户端

```java
import com.compass.yuhengapi.sdk.YuhengClient;

// 初始化客户端
YuhengClient client = new YuhengClient("http://localhost:8080", "myClientId", "mySecret");

try {
    // 使用客户端进行API调用
    // ...
} finally {
    // 关闭客户端
    client.close();
}
```

**参数说明：**
- `baseUrl`: API服务器根地址（如 `http://localhost:8080`）
- `clientId`: 客户端ID，用于身份认证
- `secret`: 客户端密钥，用于身份认证

### 2. 基础数据查询

#### 无参数查询

```java
YuhengResponse response = client.queryData("/api/user/list");

if (response.isSuccess()) {
    Object data = response.getData();
    System.out.println("查询成功: " + data);
} else {
    System.out.println("查询失败: " + response.getMessage());
}
```

#### 带参数查询

```java
import java.util.HashMap;
import java.util.Map;

Map<String, Object> params = new HashMap<>();
params.put("name", "张三");
params.put("status", 1);

YuhengResponse response = client.queryData("/api/user/search", params);

if (response.isSuccess()) {
    Object data = response.getData();
    System.out.println("查询结果: " + data);
}
```

### 3. 分页查询

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "李四");

// 查询第1页，每页20条
YuhengResponse response = client.queryPage("/api/user/page", params, 1, 20);

if (response.isSuccess()) {
    Object data = response.getData();
    PageInfo pageInfo = response.getPageInfo();

    System.out.println("当前页: " + pageInfo.getCurrentPage());
    System.out.println("每页记录数: " + pageInfo.getRowsPerPage());
    System.out.println("当前页记录数: " + pageInfo.getRowsInPage());
    System.out.println("总记录数: " + pageInfo.getTotalRows());
    System.out.println("总页数: " + pageInfo.getTotalPages());
}
```

**注意：** 如果不指定 `page` 参数，默认值为 20。

### 4. 自动获取所有数据

当需要获取所有分页数据时，可以使用 `queryAllPages` 方法自动遍历所有分页。

#### 使用默认每页20条

```java
YuhengResponse response = client.queryAllPages("/api/user/list", null);

if (response.isSuccess()) {
    List<Object> allData = (List<Object>) response.getData();
    PageInfo pageInfo = response.getPageInfo();

    System.out.println("总共获取 " + allData.size() + " 条记录");
    System.out.println("总记录数: " + pageInfo.getTotalRows());
}
```

#### 自定义每页条数

```java
// 每页100条，减少请求次数
YuhengResponse response = client.queryAllPages("/api/user/list", params, 100);

if (response.isSuccess()) {
    List<Object> allData = (List<Object>) response.getData();
    System.out.println("获取到 " + allData.size() + " 条数据");
}
```

## 响应结构

所有方法统一返回 `YuhengResponse` 对象，包含以下字段：

```java
public class YuhengResponse {
    private Integer code;      // 状态码，200表示成功
    private String message;    // 响应消息
    private Object data;       // 业务数据
    private PageInfo pageInfo; // 分页信息（分页查询时返回）
    private Long timestamp;    // 时间戳
}
```

### PageInfo 分页信息

```java
public class PageInfo {
    private Integer currentPage;  // 当前页码
    private Integer rowsInPage;   // 当前页记录数
    private Integer rowsPerPage;  // 每页记录数
    private Long totalRows;       // 总记录数
    private Integer totalPages;   // 总页数
}
```

## 错误处理

SDK会自动处理各种错误情况，并通过 `YuhengResponse` 返回错误信息。

### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| -1 | 网络错误或响应体为空 |
| -2 | HTTP状态码非200 |

### 错误处理示例

```java
YuhengResponse response = client.queryData("/api/user/list");

if (response.getCode() == 200) {
    // 处理成功响应
    Object data = response.getData();
} else if (response.getCode() == -1) {
    // 网络错误
    System.err.println("网络错误: " + response.getMessage());
} else if (response.getCode() == -2) {
    // HTTP错误
    System.err.println("HTTP错误: " + response.getMessage());
} else {
    // 业务错误
    System.err.println("业务错误: " + response.getMessage());
}
```

### 参数校验

如果传入的参数不合法，SDK会抛出 `IllegalArgumentException`：

```java
try {
    YuhengClient client = new YuhengClient("", "clientId", "secret");
} catch (IllegalArgumentException e) {
    System.err.println("参数错误: " + e.getMessage());
    // 输出: 参数错误: baseUrl不能为空
}
```

## 完整示例

```java
import com.compass.yuhengapi.sdk.YuhengClient;
import com.compass.yuhengapi.sdk.YuhengResponse;
import com.compass.yuhengapi.sdk.PageInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YuhengClientExample {
    public static void main(String[] args) {
        // 1. 初始化客户端
        YuhengClient client = new YuhengClient("http://localhost:8080", "myClientId", "mySecret");

        try {
            // 2. 无参数查询
            System.out.println("=== 无参数查询 ===");
            YuhengResponse response1 = client.queryData("/person");
            printResponse(response1);

            // 3. 带参数查询
            System.out.println("\n=== 带参数查询 ===");
            Map<String, Object> params = new HashMap<>();
            params.put("name", "张三");
            YuhengResponse response2 = client.queryData("/api/user/search", params);
            printResponse(response2);

            // 4. 分页查询
            System.out.println("\n=== 分页查询 ===");
            YuhengResponse response3 = client.queryPage("/api/user/page", params, 1, 20);
            printResponse(response3);

            // 5. 获取所有数据
            System.out.println("\n=== 获取所有数据 ===");
            YuhengResponse response4 = client.queryAllPages("/api/user/list", null, 100);
            if (response4.isSuccess()) {
                List<Object> allData = (List<Object>) response4.getData();
                PageInfo pageInfo = response4.getPageInfo();
                System.out.println("总记录数: " + pageInfo.getTotalRows());
                System.out.println("实际获取: " + allData.size() + " 条");
            }

        } finally {
            // 6. 关闭客户端
            client.close();
        }
    }

    private static void printResponse(YuhengResponse response) {
        if (response.isSuccess()) {
            System.out.println("请求成功");
            System.out.println("数据: " + response.getData());
            if (response.getPageInfo() != null) {
                System.out.println("分页信息: " + response.getPageInfo());
            }
        } else {
            System.out.println("请求失败: " + response.getMessage());
        }
    }
}
```

## 最佳实践

### 1. 资源管理

使用 `try-finally` 或 `try-with-resources` 确保客户端正确关闭：

```java
YuhengClient client = new YuhengClient(baseUrl, clientId, secret);
try {
    // 使用客户端
} finally {
    client.close();
}
```

### 2. 连接池配置

SDK内部使用连接池管理HTTP连接，默认配置：
- 最大连接数：100
- 每个路由最大连接数：20
- 连接超时：30秒
- 读取超时：30秒

如需自定义配置，可以修改 `YuhengClient` 的构造函数。

### 3. 大数据量处理

当数据量很大时，建议：
- 使用 `queryAllPages` 方法自动分页获取
- 适当增加 `pageSize` 以减少请求次数
- 考虑分批处理数据，避免内存溢出

### 4. 错误重试

对于网络错误，可以实现重试机制：

```java
int maxRetries = 3;
YuhengResponse response = null;

for (int i = 0; i < maxRetries; i++) {
    response = client.queryData("/api/user/list");
    if (response.getCode() != -1) {
        break;
    }
    Thread.sleep(1000); // 等待1秒后重试
}
```

## 注意事项

1. **线程安全**：`YuhengClient` 是线程安全的，可以在多线程环境中共享使用。
2. **超时设置**：默认超时时间为30秒，如需调整请修改源码。
3. **编码格式**：SDK使用UTF-8编码处理请求和响应。
4. **请求头**：SDK会自动添加 `clientId`、`secret` 和 `Content-Type` 请求头。
5. **分页参数**：分页查询时，`page` 参数默认值为20（不是1）。

## 技术支持

如有问题或建议，请联系技术支持团队。