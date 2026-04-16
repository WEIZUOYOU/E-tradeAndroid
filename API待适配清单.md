# E-Trade Android 端 API 待适配清单

> **文档说明**：本文档记录了Android前端已调用但需要后端确认或实现的API接口  
> **最后更新**：2026-04-10  
> **负责人**：前端开发团队

---

## 📋 目录

- [一、已实现且可用的接口](#一已实现且可用的接口)
- [二、需要后端确认的接口](#二需要后端确认的接口)
- [三、缺失的核心接口](#三缺失的核心接口)
- [四、接口详细定义](#四接口详细定义)
- [五、测试建议](#五测试建议)

---

## 一、已实现且可用的接口 ✅

以下接口已在Android端实现并测试通过：

### 1.1 用户认证模块

| 接口路径 | 方法 | 用途 | 状态 |
|---------|------|------|------|
| `/api/user/login` | POST | 用户登录 | ✅ 可用 |
| `/api/user/register` | POST | 用户注册 | ✅ 可用 |
| `/api/user/profile` | GET | 获取用户信息 | ✅ 可用 |

### 1.2 商品管理模块

| 接口路径 | 方法 | 用途 | 状态 |
|---------|------|------|------|
| `/api/product/list` | GET | 商品列表（分页） | ✅ 可用 |
| `/api/product/detail/{id}` | GET | 商品详情 | ✅ 可用 |
| `/api/product/publish` | POST | 发布商品 | ✅ 可用 |

### 1.3 订单管理模块

| 接口路径 | 方法 | 用途 | 状态 |
|---------|------|------|------|
| `/api/order/create` | POST | 创建订单 | ✅ 可用 |
| `/api/order/my-orders` | GET | 我的订单列表 | ✅ 可用 |

---

## 二、需要后端确认的接口 ⚠️

以下接口前端已实现调用逻辑，但需要后端确认是否已实现：

### 2.1 商品管理扩展接口

#### 2.1.1 获取我发布的商品列表

```http
GET /api/product/my-products?page=1&size=20
```

**使用位置**：`MyProductsActivity.java:104`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 是 | 页码，从1开始 |
| size | Integer | 是 | 每页数量 |

**期望响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "商品名称",
      "price": 99.00,
      "stock": 10,
      "description": "商品描述",
      "imageUrls": "url1,url2",
      "status": 1,
      "createTime": "2026-04-10 10:00:00"
    }
  ]
}
```

**优先级**：P0（高）

---

#### 2.1.2 下架商品

```http
POST /api/product/offshelf/{id}
```

**使用位置**：`EditProductActivity.java:177`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID（路径参数） |

**期望响应**：
```json
{
  "code": 200,
  "message": "下架成功",
  "data": null
}
```

**优先级**：P0（高）

---

### 2.2 订单管理扩展接口

#### 2.2.1 获取订单详情

```http
GET /api/order/detail/{id}
```

**使用位置**：`OrderActivity.java:71`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

**期望响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "ORD202604100001",
    "productId": 1,
    "productName": "商品名称",
    "quantity": 1,
    "totalAmount": 99.00,
    "status": 0,
    "location": "食堂门口",
    "time": "明天下午3点",
    "contact": "13800138000",
    "createTime": "2026-04-10 10:00:00"
  }
}
```

**优先级**：P0（高）

---

#### 2.2.2 取消订单

```http
POST /api/order/cancel/{id}
```

**使用位置**：`OrderActivity.java:157`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

**业务规则**：
- 仅允许取消状态为0（待支付）或1（已支付待发货）的订单
- 取消后库存应恢复

**期望响应**：
```json
{
  "code": 200,
  "message": "订单已取消",
  "data": null
}
```

**优先级**：P0（高）

---

#### 2.2.3 确认收货

```http
POST /api/order/confirm/{id}
```

**使用位置**：`OrderActivity.java:188`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单ID（路径参数） |

**业务规则**：
- 仅允许确认状态为2（已发货）的订单
- 确认后订单状态变为3（已完成）

**期望响应**：
```json
{
  "code": 200,
  "message": "确认收货成功",
  "data": null
}
```

**优先级**：P0（高）

---

## 三、缺失的核心接口 ❌

以下接口前端已有UI交互，但后端尚未实现：

### 3.1 实名认证接口

```http
POST /api/user/realname-auth
```

**使用位置**：`MyProfileActivity.java:showRealnameAuthDialog()`

**当前状态**：前端仅有Toast提示，未实际调用API

**请求参数**：
```json
{
  "studentId": "2021001234"
}
```

**期望响应**：
```json
{
  "code": 200,
  "message": "认证申请已提交，等待审核",
  "data": {
    "authStatus": 0,
    "submitTime": "2026-04-10 10:00:00"
  }
}
```

**业务规则**：
- 学号应与学校数据库对接验证
- 认证后不可修改
- 认证状态：0-待审核，1-已通过，2-已拒绝

**优先级**：P0（高）

---

### 3.2 商品更新接口

```http
PUT /api/product/update/{id}
```

**使用位置**：`EditProductActivity.java:updateProduct()`

**当前状态**：前端有TODO标记，未实现API调用

**请求参数**：
```json
{
  "id": 1,
  "name": "更新后的商品名称",
  "price": 88.00,
  "stock": 5,
  "description": "更新后的描述"
}
```

**期望响应**：
```json
{
  "code": 200,
  "message": "商品更新成功",
  "data": null
}
```

**业务规则**：
- 仅允许商品所有者编辑
- 已产生订单的商品某些字段不可修改

**优先级**：P0（高）

---

## 四、接口详细定义

### 4.1 通用响应格式

所有接口应遵循统一的响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

**状态码说明**：
| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token失效 |
| 403 | 无权限操作 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

### 4.2 数据模型定义

#### Product（商品）
```java
{
  "id": Long,              // 商品ID
  "name": String,          // 商品名称
  "price": BigDecimal,     // 价格
  "stock": Integer,        // 库存
  "description": String,   // 描述
  "imageUrls": String,     // 图片URL，逗号分隔
  "status": Integer,       // 状态：0-下架，1-在售
  "viewCount": Integer,    // 浏览量
  "sellerId": Long,        // 卖家ID
  "createTime": String     // 创建时间
}
```

#### Order（订单）
```java
{
  "id": Long,              // 订单ID
  "orderNo": String,       // 订单号
  "productId": Long,       // 商品ID
  "productName": String,   // 商品名称
  "quantity": Integer,     // 数量
  "totalAmount": BigDecimal, // 总金额
  "status": Integer,       // 状态：0-待支付，1-待发货，2-已发货，3-已完成，4-已取消
  "location": String,      // 交易地点
  "time": String,          // 交易时间
  "contact": String,       // 联系方式
  "buyerId": Long,         // 买家ID
  "sellerId": Long,        // 卖家ID
  "createTime": String     // 创建时间
}
```

#### User（用户）
```java
{
  "id": Long,              // 用户ID
  "studentId": String,     // 学号
  "username": String,      // 用户名
  "phone": String,         // 手机号
  "avatar": String,        // 头像URL
  "creditScore": Integer,  // 信用分
  "authStatus": Integer,   // 认证状态：0-未认证，1-待审核，2-已认证
  "createTime": String     // 注册时间
}
```

---

## 五、测试建议

### 5.1 接口测试顺序

建议按以下顺序开发和测试接口：

1. **第一阶段**（核心功能）
   - ✅ 已完成：登录、注册、商品列表、商品详情、发布商品、创建订单、订单列表
   - ⚠️ 待确认：我的商品列表、下架商品、订单详情、取消订单、确认收货

2. **第二阶段**（完善功能）
   - ❌ 待开发：实名认证、商品更新

3. **第三阶段**（P1功能）
   - 即时通讯相关接口
   - 商品收藏接口
   - 评价系统接口

### 5.2 测试工具推荐

- **Apifox**：接口文档管理和测试（当前使用）
- **Postman**：接口调试
- **Charles**：抓包调试Android端请求

### 5.3 联调注意事项

1. **BASE_URL配置**
   ```java
   // ApiClient.java
   public static final String BASE_URL = "http://10.0.2.2:4523/m1/8086391-7842204-default/api/";
   ```
   - 模拟器使用 `10.0.2.2` 访问本地后端
   - 真机测试需改为后端服务器IP

2. **Cookie管理**
   - Android端已实现Cookie自动持久化
   - 登录后无需手动传递Token
   - 确保后端正确设置Cookie

3. **图片URL处理**
   - 后端返回的图片URL应为相对路径
   - Android端会自动拼接BASE_URL
   - 示例：`/uploads/product/xxx.jpg` → `http://10.0.2.2:4523/.../api/uploads/product/xxx.jpg`

4. **分页参数**
   - 统一使用 `page` 和 `size` 参数
   - page从1开始（不是0）
   - 默认size建议为20

---

## 六、问题反馈

如发现接口问题或需要调整，请联系：

- **前端负责人**：王圣博
- **后端负责人**：钮锌茹
- **文档维护**：前端开发团队

---

## 七、更新日志

| 日期 | 版本 | 更新内容 | 负责人 |
|------|------|---------|--------|
| 2026-04-10 | v1.0 | 初始版本，记录所有待适配接口 | 王圣博 |

---

**备注**：本文档会随着开发进度持续更新，请保持同步。
