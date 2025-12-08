#!/usr/bin/env python3
"""
离线环境准备脚本 - 一次性下载所有需要的包和模型
适用于 PaddleOCR + PP-OCRv5 在 WSL GPU 环境
"""

import subprocess
import sys
import os
from pathlib import Path

print("=" * 70)
print("🔧 PaddleOCR 离线环境准备工具")
print("=" * 70)

# ========== 1. 核心依赖包（必需） ==========
CORE_PACKAGES = [
    'paddlepaddle-gpu==3.2.2',  # 已安装，确保版本正确
    'paddleocr==3.2.0',          # 已安装
    'paddlex',                   # OCR 依赖
]

# ========== 2. 图像处理相关（强烈推荐） ==========
IMAGE_PACKAGES = [
    'opencv-python>=4.5.0',      # 图像读取/处理
    'opencv-contrib-python',     # OpenCV 扩展功能
    'pillow>=9.0.0',             # PIL/Pillow
    'scikit-image',              # 图像增强/预处理
    'imageio',                   # 支持更多图像格式
]

# ========== 3. 数据处理相关（常用） ==========
DATA_PACKAGES = [
    'numpy',                     # 已安装
    'pandas',                    # CSV/Excel 处理
    'openpyxl',                  # Excel 写入
    'xlrd',                      # Excel 读取（旧格式）
    'python-docx',               # Word 文档处理
    'PyPDF2',                    # PDF 读取
    'pdf2image',                 # PDF 转图片
    'pypdfium2',                 # 更快的 PDF 处理
]

# ========== 4. 可视化相关（可选） ==========
VIS_PACKAGES = [
    'matplotlib',                # 绘图
    'seaborn',                   # 统计图
    'plotly',                    # 交互式图表
]

# ========== 5. Web 服务相关（如果要做 API） ==========
WEB_PACKAGES = [
    'flask',                     # 轻量级 Web 框架
    'fastapi',                   # 现代异步 API 框架
    'uvicorn',                   # FastAPI 服务器
    'requests',                  # HTTP 请求（已安装）
    'aiohttp',                   # 异步 HTTP
]

# ========== 6. 实用工具 ==========
UTIL_PACKAGES = [
    'tqdm',                      # 进度条（已安装）
    'colorama',                  # 终端彩色输出
    'python-dotenv',             # 环境变量管理
    'click',                     # 命令行工具
    'rich',                      # 美化终端输出
    'loguru',                    # 更好的日志
]

# ========== 7. 性能优化 ==========
PERF_PACKAGES = [
    'numba',                     # JIT 加速
    'cython',                    # C 扩展编译
    'psutil',                    # 系统监控
]


def check_package(package_name):
    """检查包是否已安装"""
    try:
        pkg = package_name.split('==')[0].split('>=')[0].split('[')[0]
        __import__(pkg.replace('-', '_'))
        return True
    except ImportError:
        return False


def install_packages(packages, category_name):
    """安装一组包"""
    print(f"\n{'='*70}")
    print(f"📦 {category_name}")
    print(f"{'='*70}")
    
    to_install = []
    for pkg in packages:
        pkg_name = pkg.split('==')[0].split('>=')[0].split('[')[0]
        if check_package(pkg_name):
            print(f"  ✅ {pkg_name} 已安装")
        else:
            print(f"  ❌ {pkg_name} 未安装")
            to_install.append(pkg)
    
    if to_install:
        print(f"\n📥 准备安装 {len(to_install)} 个包...")
        cmd = [
            sys.executable, '-m', 'pip', 'install', '-U',
            *to_install,
            '-i', 'https://pypi.tuna.tsinghua.edu.cn/simple',
            '--default-timeout', '300'
        ]
        print(f"执行命令: {' '.join(cmd)}")
        
        try:
            subprocess.run(cmd, check=True)
            print(f"✅ 安装成功！")
        except subprocess.CalledProcessError as e:
            print(f"❌ 安装失败: {e}")
            return False
    else:
        print(f"  ✅ 所有包已安装")
    
    return True


def check_models():
    """检查 PP-OCRv5 模型是否已下载"""
    print(f"\n{'='*70}")
    print(f"🤖 检查 PP-OCRv5 模型")
    print(f"{'='*70}")
    
    model_dir = Path.home() / '.paddlex' / 'official_models'
    
    required_models = [
        'PP-OCRv5_server_det',
        'PP-OCRv5_server_rec',
        'PP-LCNet_x1_0_textline_ori',
        'PP-LCNet_x1_0_doc_ori',
        'UVDoc',
    ]
    
    all_exist = True
    for model_name in required_models:
        model_path = model_dir / model_name
        if model_path.exists():
            size = sum(f.stat().st_size for f in model_path.rglob('*') if f.is_file())
            print(f"  ✅ {model_name}: {size / 1024 / 1024:.1f} MB")
        else:
            print(f"  ❌ {model_name}: 未下载")
            all_exist = False
    
    if not all_exist:
        print("\n⚠️  部分模型未下载，运行一次推理会自动下载：")
        print("     python -c \"from paddleocr import PaddleOCR; ocr=PaddleOCR(lang='ch', ocr_version='PP-OCRv5'); ocr.predict('https://paddleocr.bj.bcebos.com/dygraph_v2.0/test_images/11.jpg')\"")
    
    return all_exist


def download_models():
    """触发模型自动下载"""
    print(f"\n{'='*70}")
    print(f"📥 触发模型自动下载")
    print(f"{'='*70}")
    
    test_code = """
import paddle
paddle.set_device('gpu')
from paddleocr import PaddleOCR

print("初始化 PP-OCRv5...")
ocr = PaddleOCR(lang='ch', ocr_version='PP-OCRv5', use_textline_orientation=True)

print("下载测试图片...")
result = ocr.predict('https://paddleocr.bj.bcebos.com/dygraph_v2.0/test_images/11.jpg')
print(f"识别到 {len(result[0].json['res']['rec_texts'])} 行文本")
print("✅ 模型下载完成！")
"""
    
    try:
        subprocess.run([sys.executable, '-c', test_code], check=True)
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ 模型下载失败: {e}")
        return False


def check_system_libs():
    """检查系统库"""
    print(f"\n{'='*70}")
    print(f"🔧 检查系统库")
    print(f"{'='*70}")
    
    libs_to_check = [
        ('libgl1', 'libGL.so.1'),
        ('libglib2.0-0', 'libglib-2.0.so.0'),
        ('libgomp1', 'libgomp.so.1'),
    ]
    
    missing = []
    for pkg, lib in libs_to_check:
        result = subprocess.run(['ldconfig', '-p'], capture_output=True, text=True)
        if lib in result.stdout:
            print(f"  ✅ {pkg} ({lib})")
        else:
            print(f"  ❌ {pkg} ({lib}) 未安装")
            missing.append(pkg)
    
    if missing:
        print(f"\n⚠️  缺少系统库，建议安装：")
        print(f"     sudo apt install -y {' '.join(missing)}")
    
    return len(missing) == 0


def generate_requirements_txt():
    """生成 requirements.txt 备份"""
    print(f"\n{'='*70}")
    print(f"💾 生成依赖列表备份")
    print(f"{'='*70}")
    
    req_file = Path('requirements_full.txt')
    
    try:
        result = subprocess.run(
            [sys.executable, '-m', 'pip', 'freeze'],
            capture_output=True,
            text=True,
            check=True
        )
        
        with open(req_file, 'w') as f:
            f.write(result.stdout)
        
        print(f"✅ 已保存到: {req_file.absolute()}")
        print(f"   包含 {len(result.stdout.splitlines())} 个包")
        print(f"\n💡 离线时可用此文件恢复环境：")
        print(f"   pip install -r {req_file}")
        
    except subprocess.CalledProcessError as e:
        print(f"❌ 生成失败: {e}")


def main():
    """主流程"""
    
    print("\n🎯 选择要安装的组件：")
    print("  1️⃣  核心依赖（必需）")
    print("  2️⃣  图像处理库（强烈推荐）")
    print("  3️⃣  数据处理库（推荐）")
    print("  4️⃣  可视化库（可选）")
    print("  5️⃣  Web 服务库（可选，如需做 API）")
    print("  6️⃣  实用工具（推荐）")
    print("  7️⃣  性能优化库（可选）")
    print("  8️⃣  全部安装")
    print("  9️⃣  仅检查模型")
    print("  0️⃣  跳过，直接生成 requirements.txt")
    
    choice = input("\n请输入选项（多个用逗号分隔，如 1,2,3）: ").strip()
    
    if choice == '0':
        generate_requirements_txt()
        return
    
    if choice == '9':
        if not check_models():
            download_models()
        generate_requirements_txt()
        return
    
    # 解析选项
    selections = []
    if choice == '8':
        selections = ['1', '2', '3', '4', '5', '6', '7']
    else:
        selections = [s.strip() for s in choice.split(',')]
    
    # 安装对应的包
    package_groups = {
        '1': (CORE_PACKAGES, "核心依赖"),
        '2': (IMAGE_PACKAGES, "图像处理库"),
        '3': (DATA_PACKAGES, "数据处理库"),
        '4': (VIS_PACKAGES, "可视化库"),
        '5': (WEB_PACKAGES, "Web 服务库"),
        '6': (UTIL_PACKAGES, "实用工具"),
        '7': (PERF_PACKAGES, "性能优化库"),
    }
    
    for sel in selections:
        if sel in package_groups:
            pkgs, name = package_groups[sel]
            install_packages(pkgs, name)
    
    # 检查模型
    if not check_models():
        if input("\n是否现在下载模型？(y/n): ").lower() == 'y':
            download_models()
    
    # 检查系统库
    check_system_libs()
    
    # 生成备份
    generate_requirements_txt()
    
    print(f"\n{'='*70}")
    print(f"✅ 离线环境准备完成！")
    print(f"{'='*70}")
    print(f"\n📌 重要提示：")
    print(f"  1. 模型缓存位置: ~/.paddlex/official_models/")
    print(f"  2. pip 缓存位置: ~/.cache/pip/")
    print(f"  3. 依赖列表备份: requirements_full.txt")
    print(f"\n💡 如需备份整个环境（用于其他机器）：")
    print(f"  tar -czf paddle_env_backup.tar.gz ~/paddle-env ~/.paddlex ~/.cache/pip")


if __name__ == '__main__':
    main()
