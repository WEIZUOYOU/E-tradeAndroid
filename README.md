# E-Trade 校园二手交易平台 - Android 客户端

## 📱 项目简介

**E-Trade** 是一款专为高校师生打造的二手交易平台移动端应用，基于 **Android 原生 Java** 开发。本项目旨在通过真实的学号认证体系，构建安全、便捷、高效的校内闲置物品交易生态，让每一份闲置都能物尽其用。

本仓库为 **Android 前端** 代码，配套的后端服务基于 Spring Boot + MySQL，提供 RESTful API 接口。

---

## ✨ 功能特性

| 模块 | 功能 |
|------|------|
| **用户中心** | 学号注册/登录、Cookie 会话管理、个人信息展示（信用分、交易记录） |
| **商品浏览** | 商品列表分页展示、下拉刷新、商品详情查看（图文、价格、库存、卖家信息） |
| **商品发布** | 支持多图上传、实时表单校验、自动压缩图片 |
| **交易流程** | 一键下单、订单创建（原子扣减库存）、订单状态查询 |
| **网络交互** | 基于 OkHttp 的统一网络请求、JSON 数据解析、Cookie 自动持久化 |
| **UI 组件** | RecyclerView 列表、Material Design 风格、FloatingActionButton 快速发布 |

> 更多高级功能（即时通讯、信用互评、后台管理）正在开发中...

---

## 🛠 技术栈

- **语言**：Java 8
- **UI 框架**：Android SDK、Material Design Components
- **网络**：OkHttp 4.11.0（含日志拦截器）、Gson 2.10.1
- **图片加载**：Glide 4.15.1
- **异步处理**：原生 Handler + 回调
- **构建工具**：Gradle (Kotlin DSL)
- **最低兼容**：Android 8.0 (API 26)
- **目标 SDK**：Android 13 (API 33)

---

## 📁 项目结构

```
E-tradeAndroid/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/e_tradeandroid/
│   │   │   │   ├── ui/                      # 界面层
│   │   │   │   │   ├── LoginActivity.java   # 登录/注册页
│   │   │   │   │   ├── MainActivity.java    # 商品列表主界面
│   │   │   │   │   ├── ProductDetailActivity.java  # 商品详情
│   │   │   │   │   ├── PublishActivity.java        # 发布商品
│   │   │   │   │   └── OrderActivity.java          # 订单详情（待实现）
│   │   │   │   ├── adapter/                 # 适配器
│   │   │   │   │   └── ProductAdapter.java  # 商品列表适配器
│   │   │   │   ├── model/                   # 数据模型
│   │   │   │   │   ├── BaseResponse.java    # 统一响应包装
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Product.java
│   │   │   │   │   ├── Order.java
│   │   │   │   │   └── *Request.java        # 请求体
│   │   │   │   └── network/                 # 网络层
│   │   │   │       └── ApiClient.java       # OkHttp 单例 + Cookie 管理
│   │   │   └── res/                         # 资源文件
│   │   │       ├── layout/                  # 布局文件
│   │   │       ├── drawable/                # 图标、图片资源
│   │   │       └── values/                  # 颜色、字符串、主题
│   │   └── AndroidManifest.xml              # 清单文件（含权限、Activity声明）
│   └── build.gradle.kts                     # 模块依赖
├── gradle/
└── ...                                       # 其他 Gradle 配置文件
```

---

## 💻 环境要求

- **Android Studio**：2022.3 及以上版本
- **JDK**：11 或更高
- **Gradle**：8.0+（项目自带 wrapper）
- **Android 设备**：Android 8.0 (API 26) 及以上真机或模拟器

---

## 🚀 快速开始

### 1. 克隆仓库
```bash
git clone https://github.com/your-org/E-tradeAndroid.git
cd E-tradeAndroid
```

### 2. 导入项目
- 打开 Android Studio，选择 `Open an Existing Project`，定位到项目根目录。
- 等待 Gradle 同步完成（首次可能需要下载依赖）。

### 3. 配置后端地址
在 `app/src/main/java/com/example/e_tradeandroid/network/ApiClient.java` 中修改 `BASE_URL`：
```java
public static final String BASE_URL = "http://10.0.2.2:8080/api/"; // 模拟器访问本机后端
// 真机请改为局域网 IP，如 "http://192.168.1.100:8080/api/"
```

### 4. 运行应用
- 连接 Android 设备或启动模拟器。
- 点击 Android Studio 的 `Run` 按钮（绿色三角形）即可编译安装。

---

## ⚙️ 配置说明

### 网络权限
应用需要访问互联网，已在 `AndroidManifest.xml` 中声明：
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 图片上传
发布商品时，用户需授予存储读取权限（Android 6.0+ 动态申请）。代码示例可在 `PublishActivity` 中扩展。

### Cookie 管理
- 登录成功后，后端返回 `JSESSIONID`，`ApiClient` 会自动保存并携带至后续请求。
- 退出登录时，可调用 `ApiClient.clearCookies()` 清除本地 Cookie。

---

## 📚 开发注意事项

1. **API 接口约定**：所有接口返回格式均为 `BaseResponse<T>`，包含 `code`、`message`、`data` 字段。
2. **异常处理**：网络请求回调中已包含 `onFailure` 和业务错误处理，UI 上通过 `Toast` 提示。
3. **图片加载**：商品图片 URL 为相对路径（如 `/uploads/xxx.jpg`），需拼接 `BASE_URL` 再使用 Glide 加载。
4. **多图上传**：当前版本仅支持单图，如需多图可修改 `PublishActivity` 中的 `MultipartBody.Builder`，循环添加多个 `Part`。
5. **并发与事务**：创建订单接口已由后端保证原子扣减库存，前端无需额外处理。
6. **兼容性**：应用仅支持 Android 8.0+，若需兼容更低版本，请修改 `minSdk` 并适配相关 API。

---

## 👥 团队与分工

| 角色 | 姓名 | 主要负责模块 |
|------|------|--------------|
| 组长/项目经理 | 王智妍 | 整体协调、代码评审、文档审核 |
| Android 开发 | 刘延寒、王圣博、钮锌茹、王佳艺 | 网络层封装、UI 实现、业务逻辑 |
| 测试与文档 | 王畅、马俊宇、希嘉妍、王博研 | 测试用例、项目文档、PPT 制作 |

---

## 🧪 测试与质量

- **单元测试**：`app/src/test` 目录（待完善）
- **UI 测试**：`app/src/androidTest` 目录（待完善）
- **Bug 跟踪**：使用 GitHub Issues 记录，按 P0-P3 分级处理。

---

## 📅 后续计划

- [ ] 实现即时通讯（WebSocket 或轮询）
- [ ] 添加订单列表与状态跟踪
- [ ] 集成校园地图位置预约功能
- [ ] 增加商品搜索与分类筛选
- [ ] 用户信用积分体系可视化
- [ ] 性能优化（图片缓存、列表预加载）

---

## 📄 许可证

本项目仅供学习交流使用，未经授权不得用于商业目的。
