#!/usr/bin/env python3
"""
PP-OCRv5 本地图片测试工具
严格按照 PaddleOCR 3.2.0 的参数列表编写
"""

import sys
import json
import paddle
from pathlib import Path
from paddleocr import PaddleOCR

def test_ocr(image_path):
    """测试 OCR"""
    
    # 检查图片
    img_path = Path(image_path).expanduser().resolve()
    if not img_path.exists():
        print(f"❌ 图片不存在: {img_path}")
        return
    
    print(f"\n🖼️  正在处理: {img_path.name}")
    print(f"📏 文件大小: {img_path.stat().st_size / 1024:.2f} KB\n")
    
    # 设置 GPU
    paddle.set_device('gpu')
    
    # 初始化（只用源码中支持的参数！）
    print("🚀 初始化 PP-OCRv5...\n")
    
    ocr = PaddleOCR(
        lang='ch',                           # ✅ 第 27 行
        ocr_version='PP-OCRv5',              # ✅ 第 28 行
        use_textline_orientation=True,       # ✅ 第 17 行
        text_recognition_batch_size=8        # ✅ 第 14 行（可选，提升速度）
    )
    
    # 执行识别
    print("🔍 开始识别...\n")
    
    import time
    start = time.time()
    result = ocr.predict(str(img_path))
    cost = time.time() - start
    
    print(f"✅ 推理耗时: {cost:.3f} 秒\n")
    
    # 解析结果
    ocr_result = result[0]
    res = ocr_result.json['res']
    
    texts = res['rec_texts']
    scores = res['rec_scores']
    boxes = res['rec_boxes']
    
    # 显示结果
    print(f"{'='*70}")
    print(f"📄 识别结果（共 {len(texts)} 行）")
    print(f"{'='*70}\n")
    
    for i, (text, score, box) in enumerate(zip(texts, scores, boxes), 1):
        print(f"{i:3d}. [{score:.4f}] {text}")
        print(f"     坐标: x={box[0]}-{box[2]}, y={box[1]}-{box[3]}\n")
    
    # 统计
    avg_score = sum(scores) / len(scores) if scores else 0
    print(f"{'='*70}")
    print(f"📊 统计信息")
    print(f"{'='*70}")
    print(f"总行数:       {len(texts)}")
    print(f"平均置信度:   {avg_score:.4f} ({avg_score*100:.2f}%)")
    print(f"总字符数:     {sum(len(t) for t in texts)}")
    
    # 保存
    print(f"\n{'='*70}")
    print(f"💾 保存结果")
    print(f"{'='*70}")
    
    base_name = img_path.stem
    
    # JSON
    json_file = img_path.parent / f"{base_name}_result.json"
    with open(json_file, 'w', encoding='utf-8') as f:
        json.dump(ocr_result.json, f, ensure_ascii=False, indent=2)
    print(f"✅ {json_file}")
    
    # 纯文本
    txt_file = img_path.parent / f"{base_name}_result.txt"
    with open(txt_file, 'w', encoding='utf-8') as f:
        for text in texts:
            f.write(f"{text}\n")
    print(f"✅ {txt_file}")
    
    # 可视化（如果支持）
    try:
        vis_file = img_path.parent / f"{base_name}_visual.jpg"
        ocr_result.save_to_img(str(vis_file))
        print(f"✅ {vis_file}")
    except:
        pass
    
    print(f"\n{'='*70}")
    print(f"✅ 完成！")
    print(f"{'='*70}\n")


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("\n用法: python ocr_local.py <图片路径>")
        print("\n示例:")
        print("  python ocr_local.py ./test.png")
        print("  python ocr_local.py ~/imgs/document.jpg\n")
        sys.exit(1)
    
    test_ocr(sys.argv[1])
