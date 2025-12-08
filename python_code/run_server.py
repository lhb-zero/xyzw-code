#!/usr/bin/env python3
"""
OCR服务启动脚本
提供便捷的启动选项和配置
"""

import uvicorn
import argparse
import os
import sys

def main():
    parser = argparse.ArgumentParser(description="启动OCR服务")
    parser.add_argument("--host", default="0.0.0.0", help="服务器地址 (default: 0.0.0.0)")
    parser.add_argument("--port", type=int, default=5111, help="端口号 (default: 5111)")
    parser.add_argument("--reload", action="store_true", help="启用自动重载（开发模式）")
    parser.add_argument("--workers", type=int, default=1, help="工作进程数 (default: 1)")
    parser.add_argument("--log-level", choices=["debug", "info", "warning", "error"], 
                       default="info", help="日志级别 (default: info)")
    
    args = parser.parse_args()
    
    # 检查app.py是否存在
    if not os.path.exists("app.py"):
        print("错误: 找不到app.py文件")
        sys.exit(1)
    
    print(f"启动OCR服务...")
    print(f"地址: http://{args.host}:{args.port}")
    print(f"API文档: http://{args.host}:{args.port}/docs")
    print(f"重载模式: {'开启' if args.reload else '关闭'}")
    print(f"工作进程: {args.workers}")
    print(f"日志级别: {args.log_level}")
    print("-" * 50)
    
    try:
        uvicorn.run(
            "app:app",
            host=args.host,
            port=args.port,
            reload=args.reload,
            workers=1 if args.reload else args.workers,
            log_level=args.log_level
        )
    except KeyboardInterrupt:
        print("\n服务已停止")
    except Exception as e:
        print(f"启动失败: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()