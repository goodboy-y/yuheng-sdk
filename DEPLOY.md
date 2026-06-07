# Maven Central 发布指南

## 前置准备

### 1. Sonatype JIRA 账号
1. 访问 https://issues.sonatype.org 创建账号
2. 创建 Issue 请求发布权限:
   - Project: Community Support - Open Source Project Repository Hosting (OSSRH)
   - Issue Type: New Project
   - Summary: `yuheng-sdk`
   - Group Id: `com.compass`
   - Project URL: `https://github.com/compass/yuheng-sdk`
   - SCM URL: `https://github.com/compass/yuheng-sdk.git`
   - 等待审核通过（通常需要1-2个工作日）

### 2. GPG 密钥配置
1. 安装 GPG (Windows: https://www.gpg4win.org/)
2. 生成密钥对:
   ```bash
   gpg --gen-key
   ```
   - 姓名: your-name
   - 邮箱: your-email@example.com
   - 密码: 设置一个强密码（务必记住）

3. 查看密钥 ID:
   ```bash
   gpg --list-keys
   ```
   输出类似: `pub   rsa2048 2024-01-01 [SC] ABCD1234567890`

4. 发布公钥到密钥服务器:
   ```bash
   gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234567890
   ```

### 3. Maven Settings 配置
编辑 `~/.m2/settings.xml` (Windows: `C:\Users\YourUser\.m2\settings.xml`):

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>你的Sonatype用户名</username>
      <password>你的Sonatype密码</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>你的GPG密钥密码</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

## 发布流程

### 快照版本测试（可选）
```bash
mvn clean deploy -DskipTests
```

### 正式版本发布

1. **更新版本号** (移除 -SNAPSHOT):
   编辑 pom.xml:
   ```xml
   <version>0.0.1</version>  <!-- 移除 -SNAPSHOT -->
   ```

2. **执行发布**:
   ```bash
   mvn clean deploy -DskipTests -Dgpg.skip=false
   ```

3. **发布后操作**:
   - 访问 https://s01.oss.sonatype.org/ 查看发布状态
   - 约10分钟后可在 https://repo1.maven.org/maven2/ 查到

4. **GitHub Release**:
   ```bash
   git tag v0.0.1
   git push origin v0.0.1
   ```

### 回滚版本号
发布完成后，记得在 pom.xml 中更新到下一个版本:
```xml
<version>0.0.2-SNAPSHOT</version>
```

## 常见问题

### GPG 签名失败
- 确保 gpg-agent 正在运行
- Windows: 首次使用可能需要重启终端

### 权限不足
- 确认 Sonatype Issue 已审批通过
- 确认 Group Id 与 pom.xml 中一致

### 网络超时
- 切换 Maven 镜像源
- 配置代理: `<proxy>` in settings.xml
