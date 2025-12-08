import paddle
import time

paddle.set_device('gpu')

from paddleocr import PaddleOCR

print("🚀 初始化 PP-OCRv5...")

ocr = PaddleOCR(
    lang='ch',
    ocr_version='PP-OCRv5',
    use_textline_orientation=True
)

img_url = 'https://cos.waveportaihub.com/20251115/69188cf2aa5b0.jpg'

start = time.time()
result = ocr.predict(img_url)  # ✅ 使用 predict（官方推荐，避免 DeprecationWarning）
cost = time.time() - start

print(f"✅ 推理耗时: {cost:.3f} 秒\n")

# 🔍 探索 OCRResult 对象的结构
print("📊 OCRResult 对象分析:")
print(f"type: {type(result[0])}")
print(f"dir: {[x for x in dir(result[0]) if not x.startswith('_')]}")
print()

# 尝试常见属性
ocr_result = result[0]

# 方法 1: 尝试 .json 属性
if hasattr(ocr_result, 'json'):
    print("✅ 找到 .json 属性:")
    print(ocr_result.json)
    print()

# 方法 2: 尝试 to_dict() 方法
if hasattr(ocr_result, 'to_dict'):
    print("✅ 找到 .to_dict() 方法:")
    print(ocr_result.to_dict())
    print()

# 方法 3: 尝试 boxes/texts 等属性
for attr in ['boxes', 'texts', 'text', 'dt_polys', 'rec_texts', 'rec_scores', 'data']:
    if hasattr(ocr_result, attr):
        val = getattr(ocr_result, attr)
        print(f"✅ 找到 .{attr}: type={type(val)}")
        if isinstance(val, (list, tuple)) and len(val) > 0:
            print(f"   示例: {val[0]}")
        else:
            print(f"   值: {val}")
        print()

# 方法 4: 尝试迭代
print("🔄 尝试迭代 OCRResult:")
try:
    for i, item in enumerate(ocr_result):
        print(f"{i}: {item}")
        if i >= 2:  # 只显示前 3 个
            print("...")
            break
except TypeError as e:
    print(f"不可迭代: {e}")
