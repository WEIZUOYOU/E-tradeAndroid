# UI 设计快速参考指南

## 🎨 颜色速查表

### 主色调 - 校园青绿
```xml
@color/primary_green          <!-- #2E8B57 海绿 -->
@color/primary_green_light    <!-- #3CB371 海洋绿 -->
@color/primary_green_dark     <!-- #267347 深绿色 -->
```

### 辅助色 - 木质暖黄
```xml
@color/accent_yellow          <!-- #F7D44A -->
@color/accent_yellow_light    <!-- #FAE06E -->
@color/accent_yellow_dark     <!-- #E5C23A -->
```

### 背景色
```xml
@color/background_light       <!-- #F0F9ED 极浅绿（页面背景）-->
@color/background_card        <!-- #FAFCFB 卡片背景 -->
@color/background_white       <!-- #FFFFFF 白色背景 -->
```

### 文字颜色
```xml
@color/text_primary           <!-- #1F2E1C 主文字 -->
@color/text_secondary         <!-- #5A6660 次要文字 -->
@color/text_hint              <!-- #A8B0AB 提示文字 -->
@color/text_price             <!-- #E5C23A 价格文字 -->
```

---

## 📐 圆角规范

| 组件类型 | 圆角大小 | 使用场景 |
|---------|---------|---------|
| 超大圆角 | 24dp | 按钮、搜索框 |
| 大圆角 | 20dp | 卡片、分类标签 |
| 中圆角 | 16dp | 输入框 |
| 小圆角 | 12dp | 价格标签、小徽章 |

---

## 🎯 常用Drawable资源

### 按钮样式
```xml
<!-- 主按钮（渐变绿色）-->
android:background="@drawable/bg_btn_primary"

<!-- 成功按钮（浅绿色）-->
android:background="@drawable/bg_btn_success"

<!-- 危险按钮（红色）-->
android:background="@drawable/bg_btn_danger"
```

### 卡片和容器
```xml
<!-- 卡片背景 -->
android:background="@drawable/bg_card"

<!-- 输入框 -->
android:background="@drawable/bg_input"

<!-- 分类标签 -->
android:background="@drawable/category_background"

<!-- 价格标签 -->
android:background="@drawable/bg_price_tag"
```

---

## 💡 MaterialCardView 使用示例

### 商品卡片
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="20dp"
    app:cardElevation="3dp"
    app:cardBackgroundColor="@color/background_card"
    app:strokeWidth="0.5dp"
    app:strokeColor="@color/divider">
    
    <!-- 内容 -->
    
</com.google.android.material.card.MaterialCardView>
```

### 搜索栏/悬浮卡片
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="24dp"
    app:cardElevation="4dp"
    app:cardBackgroundColor="@color/background_white"
    app:strokeWidth="1dp"
    app:strokeColor="@color/primary_green_light">
    
    <!-- 内容 -->
    
</com.google.android.material.card.MaterialCardView>
```

---

## 🎨 TextView 样式建议

### 价格显示
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="¥99.00"
    android:textSize="16sp"
    android:textColor="@color/text_price"
    android:textStyle="bold"
    android:paddingStart="10dp"
    android:paddingEnd="10dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp"
    android:background="@drawable/bg_price_tag" />
```

### 标题文字
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="标题"
    android:textSize="18sp"
    android:textColor="@color/text_primary"
    android:textStyle="bold" />
```

### 次要文字
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="说明文字"
    android:textSize="14sp"
    android:textColor="@color/text_secondary" />
```

---

## 🌈 状态颜色

```xml
@color/success_green   <!-- #2ECC71 成功 -->
@color/warning_orange  <!-- #F39C12 警告 -->
@color/error_red       <!-- #E74C3C 错误 -->
```

---

## 📏 间距建议

```xml
<!-- 外边距 -->
android:layout_margin="8dp"      <!-- 卡片之间 -->
android:layout_margin="16dp"     <!-- 页面边缘 -->

<!-- 内边距 -->
android:padding="12dp"           <!-- 卡片内部 -->
android:padding="16dp"           <!-- 对话框内部 -->

<!-- 元素间距 -->
android:layout_marginTop="8dp"   <!-- 垂直间距 -->
```

---

## ✨ 阴影高度

```xml
app:cardElevation="2dp"   <!-- 轻微浮起 -->
app:cardElevation="3dp"   <!-- 普通卡片 -->
app:cardElevation="4dp"   <!-- 悬浮元素 -->
app:cardElevation="6dp"   <!-- 重要悬浮 -->
```

---

## 🔧 快速应用主题

在 Activity 或 Fragment 的根布局中：
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/background_light">
    
    <!-- 内容 -->
    
</LinearLayout>
```

---

## 📱 响应式设计提示

1. **使用dp而非px** - 确保在不同密度屏幕上正确显示
2. **使用sp作为文字单位** - 支持用户字体大小设置
3. **测试多种屏幕尺寸** - 从小屏手机到大屏平板
4. **保持触摸区域足够大** - 按钮最小48x48dp

---

## 🎯 设计检查清单

创建新页面时，确保：
- [ ] 使用 background_light 作为页面背景
- [ ] 卡片使用 bg_card 或 MaterialCardView
- [ ] 按钮使用 bg_btn_primary（主操作）
- [ ] 价格使用 text_price 颜色和 bg_price_tag
- [ ] 圆角符合规范（20dp卡片，24dp按钮）
- [ ] 文字颜色使用 text_primary/secondary
- [ ] 保持足够的留白（8-16dp间距）
- [ ] 阴影柔和不突兀（2-4dp）

---

**记住核心理念**: 清新、自然、活力、信赖 🌿
