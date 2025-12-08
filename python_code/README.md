# OCR服务API

基于FastAPI和PaddleOCR的本地OCR文字识别服务

## 功能特性

- 支持多种图片格式上传
- 支持Base64图片编码识别
- 可配置OCR参数
- 返回简洁的识别结果（文字、行数组、耗时）
- 健康检查接口
- CORS支持
- 内存高效处理（无临时文件）
- GPU加速支持

## 安装依赖

```bash
pip install -r requirements.txt
```

## 启动服务

### 方式1：直接启动（推荐）

```bash
python app.py
```

### 方式2：使用启动脚本（可配置参数）

```bash
# 基本启动
python run_server.py

# 自定义端口
python run_server.py --port 5111

# 开发模式（自动重载）
python run_server.py --reload

# 多进程模式
python run_server.py --workers 4
```

## 服务访问

服务将在 `http://localhost:5111` 启动

- **API文档**: `http://localhost:5111/docs` (Swagger UI)
- **健康检查**: `http://localhost:5111/health`

## API接口

### 1. 健康检查

**GET** `/health`

返回服务状态和OCR引擎初始化状态

### 2. OCR初始化

**POST** `/ocr/initialize`

参数：
- `lang`: 语言 (default: "ch")
- `ocr_version`: OCR版本 (default: "PP-OCRv5")
- `use_textline_orientation`: 文本行方向检测 (default: True)
- `text_recognition_batch_size`: 批处理大小 (default: 8)
- `show_log`: 是否显示日志 (default: False)

### 3. 文件上传OCR

**POST** `/ocr/extract`

参数：
- `file`: 图片文件 (multipart/form-data)

### 4. Base64图片OCR

**POST** `/ocr/extract_base64`

参数：
- `image_data`: Base64编码的图片数据

### 5. 自定义配置OCR

**POST** `/ocr/extract_with_config`

参数：
- `file`: 图片文件 (multipart/form-data)
- `config`: JSON格式的OCR配置字符串

## 返回格式

```json
{
    "filename": "test.png",
    "text": "识别出的完整文字",
    "lines": ["第一行文字", "第二行文字"],
    "cost_time": 0.156
}
```

## 测试

运行测试客户端：

```bash
python test_client.py
```

测试客户端会：
1. 检查服务健康状态
2. 创建测试图片
3. 测试文件上传OCR
4. 测试Base64 OCR
5. 测试自定义配置OCR

## 使用示例

### Python requests示例

```python
import requests

# 文件上传方式
with open('image.png', 'rb') as f:
    files = {'file': f}
    response = requests.post('http://localhost:5111/ocr/extract', files=files)
    print(response.json())

# Base64方式
import base64
with open('image.png', 'rb') as f:
    img_data = base64.b64encode(f.read()).decode('utf-8')
    data = {'image_data': img_data}
    response = requests.post('http://localhost:5111/ocr/extract_base64', data=data)
    print(response.json())
```

### cURL示例

```bash
# 文件上传
curl -X POST "http://localhost:5111/ocr/extract" \
     -H "accept: application/json" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@image.png"

# Base64
curl -X POST "http://localhost:5111/ocr/extract_base64" \
     -H "accept: application/json" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "image_data=iVBORw0KGgoAAAANSUhEUgAA..."
```

## 项目文件结构

```
.
├── app.py                 # 主服务文件
├── run_server.py         # 启动脚本
├── test_client.py        # 测试客户端
├── requirements.txt      # 依赖文件
└── README.md            # 说明文档
```

## 支持的图片格式

- PNG
- JPEG
- JPG
- BMP
- TIFF
- WEBP

## 环境要求

- Python 3.8+
- GPU支持（推荐）
- 足够的显存（至少2GB）

## 注意事项

1. **首次运行**: 会自动下载PaddleOCR模型文件，请保持网络连接
2. **GPU支持**: 默认使用GPU加速，如需CPU运行请修改 `app.py` 中的 `paddle.set_device('gpu')` 为 `paddle.set_device('cpu')`
3. **图片大小**: 建议不超过10MB
4. **性能**: 大图片处理时间较长，建议适当压缩后使用

## 配置选项

可配置的PaddleOCR参数包括但不限于：

- `lang`: 支持的语言（ch, en等）
- `ocr_version`: OCR版本（PP-OCRv3, PP-OCRv4, PP-OCRv5）
- `use_textline_orientation`: 文本行方向检测
- `text_recognition_batch_size`: 批处理大小
- `show_log`: 日志显示

详细参数请参考PaddleOCR官方文档。

## 常见问题

### Q: 启动失败怎么办？
A: 检查：
1. Python版本是否为3.8+
2. 是否正确安装所有依赖
3. GPU驱动和CUDA是否正确安装

### Q: 如何切换到CPU模式？
A: 修改 `app.py` 第58行：
```python
paddle.set_device('cpu')  # 改为cpu
```

### Q: 如何更改端口？
A: 使用启动脚本：
```bash
python run_server.py --port 8080
```

### Q: 识别效果不好怎么办？
A: 尝试：
1. 提高图片分辨率和清晰度
2. 调整图片方向
3. 更换OCR版本参数