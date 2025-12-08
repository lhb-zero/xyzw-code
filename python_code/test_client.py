import requests
import base64
import json

# API基础URL
BASE_URL = "http://localhost:5111"

def test_health_check():
    """测试健康检查"""
    try:
        response = requests.get(f"{BASE_URL}/health")
        print("健康检查:", response.json())
        return response.status_code == 200
    except Exception as e:
        print(f"健康检查失败: {e}")
        return False

def test_ocr_with_file(image_path):
    """测试文件上传OCR"""
    try:
        with open(image_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(f"{BASE_URL}/ocr/extract", files=files)
        
        if response.status_code == 200:
            result = response.json()
            print("OCR识别结果:")
            print(f"识别文字: {result['text']}")
            print(f"行数: {len(result['lines'])}")
            return result
        else:
            print(f"OCR识别失败: {response.text}")
            return None
    except Exception as e:
        print(f"文件OCR测试失败: {e}")
        return None

def test_ocr_with_base64(image_path):
    """测试Base64 OCR"""
    try:
        # 读取图片并编码为Base64
        with open(image_path, 'rb') as f:
            image_data = base64.b64encode(f.read()).decode('utf-8')
        
        data = {
            'image_data': image_data
        }
        
        response = requests.post(f"{BASE_URL}/ocr/extract_base64", data=data)
        
        if response.status_code == 200:
            result = response.json()
            print("Base64 OCR识别结果:")
            print(f"识别文字: {result['text']}")
            print(f"行数: {len(result['lines'])}")
            return result
        else:
            print(f"Base64 OCR识别失败: {response.text}")
            return None
    except Exception as e:
        print(f"Base64 OCR测试失败: {e}")
        return None

def test_ocr_with_custom_config(image_path):
    """测试自定义配置OCR"""
    try:
        config = {
            "use_angle_cls": True,
            "lang": "ch",
            "use_gpu": False,
            "show_log": False
        }
        
        with open(image_path, 'rb') as f:
            files = {'file': f}
            data = {
                'config': json.dumps(config)
            }
            response = requests.post(f"{BASE_URL}/ocr/extract_with_config", files=files, data=data)
        
        if response.status_code == 200:
            result = response.json()
            print("自定义配置OCR识别结果:")
            print(f"识别文字: {result['text']}")
            print(f"使用配置: {result['config']}")
            return result
        else:
            print(f"自定义配置OCR识别失败: {response.text}")
            return None
    except Exception as e:
        print(f"自定义配置OCR测试失败: {e}")
        return None

def create_sample_image():
    """创建一个简单的测试图片"""
    try:
        from PIL import Image, ImageDraw, ImageFont
        import numpy as np
        
        # 创建白色背景图片
        img = Image.new('RGB', (400, 200), color='white')
        draw = ImageDraw.Draw(img)
        
        # 添加文字
        text = "Hello World!\n这是一个OCR测试\nPaddleOCR文字识别"
        try:
            font = ImageFont.truetype("arial.ttf", 24)
        except:
            font = ImageFont.load_default()
        
        draw.text((20, 50), text, fill='black', font=font)
        
        # 保存图片
        img.save("test_image.png")
        print("测试图片已创建: test_image.png")
        return "test_image.png"
    except Exception as e:
        print(f"创建测试图片失败: {e}")
        return None

if __name__ == "__main__":
    print("开始测试OCR服务...")
    
    # 1. 健康检查
    if not test_health_check():
        print("服务未运行，请先启动OCR服务")
        exit(1)
    
    # 2. 创建测试图片
    test_image = create_sample_image()
    if not test_image:
        print("无法创建测试图片")
        exit(1)
    
    print("\n" + "="*50)
    
    # 3. 测试文件上传OCR
    print("\n1. 测试文件上传OCR:")
    test_ocr_with_file(test_image)
    
    print("\n" + "="*50)
    
    # 4. 测试Base64 OCR
    print("\n2. 测试Base64 OCR:")
    test_ocr_with_base64(test_image)
    
    print("\n" + "="*50)
    
    # 5. 测试自定义配置OCR
    print("\n3. 测试自定义配置OCR:")
    test_ocr_with_custom_config(test_image)
    
    print("\n测试完成!")