# Mock Model Server for ChatPA Testing

这是一个用于测试 ChatPA 系统的 Python mock 模型服务器。它模拟真实的 AI 模型，通过 WebSocket 与 ChatPA 服务器通信。

## 功能特性

- ✅ 完整支持 `ModelSendProtocol` (查询请求)
- ✅ 完整支持 `ModelSummaryProtocol` (摘要请求)
- ✅ 返回符合 `ModelReturnProtocol` 的响应
- ✅ 模拟流式生成 (token-by-token)
- ✅ 模拟参考文献来源
- ✅ 详细的日志输出

## 安装依赖

```bash
pip install websockets
```

或使用 requirements.txt:

```bash
pip install -r requirements.txt
```

## 使用方法

### 1. 启动 Mock 服务器

```bash
python mock_model_server.py
```

默认配置：
- Host: `0.0.0.0`
- Port: `8081`

### 2. 配置 ChatPA 服务器

修改 `application.yaml` 中的模型配置：

```yaml
model:
  host: "localhost"
  port: "8081"
```

### 3. 启动 ChatPA 服务器

```bash
./gradlew run
```

## 协议说明

### 接收的消息格式

#### 1. 查询请求 (ModelSendProtocol)
```json
{
  "prompt": "用户的问题",
  "token": "用户token",
  "user": "用户名",
  "history": [
    {
      "role": "user",
      "content": "历史消息",
      "time": 1234567890
    }
  ]
}
```

#### 2. 摘要请求 (ModelSummaryProtocol)
```json
{
  "summary": true,
  "token": "用户token",
  "history": [...]
}
```

### 发送的消息格式 (ModelReturnProtocol)

#### 1. 生成开始
```json
{
  "type": "GEN_STARTED",
  "token": "用户token",
  "generated_token": "",
  "content": "",
  "source": []
}
```

#### 2. 新 Token
```json
{
  "type": "NEW_TOKEN",
  "token": "用户token",
  "generated_token": "生成的文本片段",
  "content": "",
  "source": []
}
```

#### 3. 生成完成
```json
{
  "type": "GEN_FINISHED",
  "token": "用户token",
  "generated_token": "",
  "content": "",
  "source": ["文档A.pdf", "参考资料B.docx"]
}
```

#### 4. 摘要响应
```json
{
  "type": "SUMMARY",
  "token": "用户token",
  "generated_token": "",
  "content": "对话摘要内容",
  "source": []
}
```

## 配置选项

在 `mock_model_server.py` 中可以修改以下配置：

```python
HOST = "0.0.0.0"        # 监听地址
PORT = 8081             # 监听端口
LOG_LEVEL = logging.INFO  # 日志级别 (DEBUG, INFO, WARNING, ERROR)
```

## 自定义响应

### 修改模拟响应文本

编辑 `MOCK_RESPONSES` 列表：

```python
MOCK_RESPONSES = [
    "你的自定义响应1",
    "你的自定义响应2",
    # ...
]
```

### 修改模拟参考文献

编辑 `MOCK_SOURCES` 列表：

```python
MOCK_SOURCES = [
    ["文档1.pdf", "文档2.pdf"],
    ["文档3.pdf"],
    # ...
]
```

### 修改生成速度

调整 token 生成延迟：

```python
await asyncio.sleep(random.uniform(0.05, 0.15))  # 秒
```

## 测试场景

### 1. 基本查询测试
启动服务器后，在 ChatPA 客户端发送消息，观察：
- 是否收到 GEN_STARTED
- Token 是否逐个返回
- 是否收到 GEN_FINISHED 和参考文献

### 2. 摘要功能测试
发送 `$$` 触发摘要请求，观察：
- 是否收到 SUMMARY 响应
- 摘要内容是否正确

### 3. 并发测试
多个客户端同时连接，测试：
- 多用户并发请求
- Token 路由是否正确
- 响应是否发送到正确的用户

### 4. 异常处理测试
- 断开连接后重连
- 发送无效 JSON
- 高频请求

## 日志输出

服务器会输出详细日志，包括：
- 🔵 INFO: 连接建立、请求处理、响应发送
- 🟢 DEBUG: 详细的消息内容、Token 计数
- 🔴 ERROR: 错误信息和堆栈跟踪

示例日志：
```
2025-10-26 10:30:00 - __main__ - INFO - New connection from 127.0.0.1:12345
2025-10-26 10:30:01 - __main__ - INFO - Processing query for user 'TestUser' (token: abc123)
2025-10-26 10:30:01 - __main__ - DEBUG - Sent GEN_STARTED to abc123
2025-10-26 10:30:02 - __main__ - DEBUG - Sent 150 tokens to abc123
2025-10-26 10:30:02 - __main__ - INFO - Completed query for abc123 with 2 sources
```

## 故障排查

### 问题：ChatPA 无法连接到 mock 服务器

**解决方案：**
1. 检查 mock 服务器是否运行
2. 确认端口配置一致
3. 检查防火墙设置
4. 查看日志输出

### 问题：收不到响应

**解决方案：**
1. 检查 JSON 格式是否正确
2. 确认 token 字段存在
3. 查看服务器日志的错误信息

### 问题：WebSocket 连接断开

**解决方案：**
1. 检查网络稳定性
2. 增加超时时间
3. 查看异常堆栈

## 开发建议

### 添加新的响应类型

在 `process_message` 方法中添加处理逻辑：

```python
async def process_message(self, websocket, message, client_id):
    data = json.loads(message)
    
    if data.get("custom_type"):
        await self.handle_custom_request(websocket, data)
```

### 保存测试日志

```python
# 添加文件日志处理器
file_handler = logging.FileHandler('mock_server.log')
logger.addHandler(file_handler)
```

### 添加统计信息

```python
self.stats = {
    "total_requests": 0,
    "total_tokens": 0,
    "active_connections": 0
}
```

## 注意事项

⚠️ **仅用于测试目的**
- 此服务器不包含真实的 AI 模型
- 响应内容是预定义的模拟数据
- 不要在生产环境使用

⚠️ **性能考虑**
- 适合开发和功能测试
- 不适合压力测试（可能需要优化）

⚠️ **安全性**
- 没有认证机制
- 应在受信任的网络环境中使用

## 许可证

与 ChatPA 项目保持一致
