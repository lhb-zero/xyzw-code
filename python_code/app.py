from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import numpy as np
import paddle
import cv2
from PIL import Image
from pathlib import Path
import io
import base64
import os
from typing import Optional, Dict, Any, List
import logging
import time
import json
from paddleocr import PaddleOCR

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="OCR Service API",
    description="基于PaddleOCR的文字识别服务",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 全局OCR实例
ocr_engine = None

class OCRService:
    def __init__(self):
        self.ocr = None
        
    def initialize_ocr(self, config: Dict[str, Any] = None):
        """初始化OCR引擎"""
        try:
            default_config = {
                'lang': 'ch',
                'ocr_version': 'PP-OCRv5',
                'use_textline_orientation': True,
                'text_recognition_batch_size': 8,
                'text_det_limit_side_len': 1200,      # ← 增大检测分辨率
                'text_det_limit_type': 'min',         # ← 限制长边
                'text_det_box_thresh': 0.4,           # ← 降低检测阈值
                'text_rec_score_thresh': 0.3,         # ← 降低识别阈值
                'text_det_thresh': 0.2,               # DB 检测阈值，默认 0.3
            }
            
            if config:
                default_config.update(config)
            
            # 设置GPU
            paddle.set_device('gpu')
                
            logger.info(f"正在初始化PaddleOCR，配置: {default_config}")
            self.ocr = PaddleOCR(**default_config)
            logger.info("PaddleOCR初始化成功")
            return True
        except Exception as e:
            logger.error(f"PaddleOCR初始化失败: {str(e)}")
            return False
    
    def preprocess_image(self, image_data: bytes) -> np.ndarray:
        """图像预处理"""
        try:
            # 将字节数据转换为PIL Image
            image = Image.open(io.BytesIO(image_data))
            
            # 转换为OpenCV格式
            image_array = np.array(image)
            
            # 如果是RGBA图像，转换为RGB
            if len(image_array.shape) == 3 and image_array.shape[2] == 4:
                image_array = cv2.cvtColor(image_array, cv2.COLOR_RGBA2RGB)
            
            return image_array
        except Exception as e:
            logger.error(f"图像预处理失败: {str(e)}")
            raise HTTPException(status_code=400, detail=f"图像预处理失败: {str(e)}")
    
    def extract_text(self, image: np.ndarray) -> Dict[str, Any]:
        """提取文字（适配新版 PaddleOCR 文档模式）"""
        try:
            if self.ocr is None:
                raise HTTPException(status_code=500, detail="OCR引擎未初始化")

            start_time = time.time()
            results = self.ocr.ocr(image)
            cost_time = time.time() - start_time

            texts = []

            if not results or len(results) == 0:
                logger.warning("OCR 未返回任何结果")
            else:
                # 取第一页结果
                page_result = results[0]
                if isinstance(page_result, dict):
                    # 新版文档模式：包含 rec_texts, rec_scores, rec_polys
                    rec_texts = page_result.get("rec_texts", [])
                    rec_scores = page_result.get("rec_scores", [])
                    # 不需要置信度？那就只取文本！
                    texts = [text.strip() for text in rec_texts if isinstance(text, str) and text.strip()]
                elif isinstance(page_result, list):
                    # 旧版或简单模式：直接是 [[box, (text, score)], ...]
                    for item in page_result:
                        if not item:
                            continue
                        if isinstance(item, list) and len(item) >= 2:
                            second = item[1]
                            if isinstance(second, tuple) and len(second) == 2:
                                text = second[0]
                            elif isinstance(second, str):
                                text = second
                            elif isinstance(second, dict) and "text" in second:
                                text = second["text"]
                            else:
                                text = str(second)
                            if text.strip():
                                texts.append(text.strip())

            return {
                'text': '\n'.join(texts),
                'lines': texts,
                'cost_time': round(cost_time, 3),
                'success': True
            }

        except Exception as e:
            logger.error(f"文字提取失败: {str(e)}", exc_info=True)
            return {
                'text': '',
                'lines': [],
                'cost_time': 0,
                'success': False,
                'error': str(e)
            }

# 创建OCR服务实例
ocr_service = OCRService()

@app.on_event("startup")
async def startup_event():
    """应用启动时初始化OCR"""
    success = ocr_service.initialize_ocr()
    if not success:
        logger.error("OCR服务启动失败")

@app.get("/")
async def root():
    """根路径"""
    return {"message": "OCR Service is running", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    """健康检查"""
    return {
        "status": "healthy",
        "ocr_initialized": ocr_service.ocr is not None
    }

@app.post("/ocr/initialize")
async def initialize_ocr(
    lang: str = Form("ch"),
    ocr_version: str = Form("PP-OCRv5"),
    use_textline_orientation: bool = Form(True),
    text_recognition_batch_size: int = Form(8),
):
    """初始化OCR引擎"""
    config = {
        'lang': lang,
        'ocr_version': ocr_version,
        'use_textline_orientation': use_textline_orientation,
        'text_recognition_batch_size': text_recognition_batch_size,
    }
    
    success = ocr_service.initialize_ocr(config)
    if success:
        return {"message": "OCR初始化成功", "config": config}
    else:
        raise HTTPException(status_code=500, detail="OCR初始化失败")

@app.post("/ocr/extract")
async def extract_text_from_image(
    file: UploadFile = File(...)
):
    """从上传的图片中提取文字"""
    try:
        # 验证文件类型
        if not file.content_type.startswith('image/'):
            raise HTTPException(status_code=400, detail="请上传图片文件")
        
        # 读取图片数据
        image_data = await file.read()
        
        # 预处理图片
        image = ocr_service.preprocess_image(image_data)
        
        # 提取文字
        result = ocr_service.extract_text(image)
        
        if result['success']:
            return {
                "filename": file.filename,
                "text": result['text'],
                "lines": result['lines'],
                "cost_time": result['cost_time']
            }
        else:
            raise HTTPException(status_code=500, detail=result.get('error', 'OCR识别失败'))
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"处理图片时发生错误: {str(e)}")
        raise HTTPException(status_code=500, detail=f"处理图片时发生错误: {str(e)}")

@app.post("/ocr/extract_base64")
async def extract_text_from_base64(
    image_data: str = Form(...)
):
    """从Base64编码的图片中提取文字"""
    try:
        # 解码Base64
        try:
            # 移除可能的数据URL前缀
            if image_data.startswith('data:image/'):
                image_data = image_data.split(',')[1]
            
            image_bytes = base64.b64decode(image_data)
        except Exception as e:
            raise HTTPException(status_code=400, detail=f"Base64解码失败: {str(e)}")
        
        # 预处理图片
        image = ocr_service.preprocess_image(image_bytes)
        
        # 提取文字
        result = ocr_service.extract_text(image)
        
        if result['success']:
            return {
                "text": result['text'],
                "lines": result['lines'],
                "cost_time": result['cost_time']
            }
        else:
            raise HTTPException(status_code=500, detail=result.get('error', 'OCR识别失败'))
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"处理Base64图片时发生错误: {str(e)}")
        raise HTTPException(status_code=500, detail=f"处理Base64图片时发生错误: {str(e)}")

@app.post("/ocr/extract_with_config")
async def extract_text_with_config(
    file: UploadFile = File(...),
    config: str = Form("{}")  # JSON字符串
):
    """使用自定义配置提取文字"""
    try:
        import json
        
        # 解析配置
        try:
            custom_config = json.loads(config)
        except json.JSONDecodeError:
            raise HTTPException(status_code=400, detail="配置格式错误，需要有效的JSON字符串")
        
        # 重新初始化OCR引擎
        if custom_config:
            success = ocr_service.initialize_ocr(custom_config)
            if not success:
                raise HTTPException(status_code=500, detail="使用自定义配置初始化OCR失败")
        
        # 验证文件类型
        if not file.content_type.startswith('image/'):
            raise HTTPException(status_code=400, detail="请上传图片文件")
        
        # 读取图片数据
        image_data = await file.read()
        
        # 预处理图片
        image = ocr_service.preprocess_image(image_data)
        
        # 提取文字
        result = ocr_service.extract_text(image)
        
        if result['success']:
            return {
                "filename": file.filename,
                "config": custom_config,
                "text": result['text'],
                "lines": result['lines'],
                "cost_time": result['cost_time']
            }
        else:
            raise HTTPException(status_code=500, detail=result.get('error', 'OCR识别失败'))
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"处理图片时发生错误: {str(e)}")
        raise HTTPException(status_code=500, detail=f"处理图片时发生错误: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5111)
