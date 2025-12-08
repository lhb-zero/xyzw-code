package com.ruoyi.club.service;

import java.util.List;
import com.ruoyi.club.domain.UploadImage;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传管理Service接口
 * 
 * @author lhb
 * @date 2025-11-19
 */
public interface IUploadImageService 
{
    /**
     * 查询图片上传管理
     * 
     * @param id 图片上传管理主键
     * @return 图片上传管理
     */
    public UploadImage selectUploadImageById(Long id);

    /**
     * 查询图片上传管理列表
     * 
     * @param uploadImage 图片上传管理
     * @return 图片上传管理集合
     */
    public List<UploadImage> selectUploadImageList(UploadImage uploadImage);

    /**
     * 新增图片上传管理
     * 
     * @param uploadImage 图片上传管理
     * @return 结果
     */
    public int insertUploadImage(UploadImage uploadImage);

    /**
     * 修改图片上传管理
     * 
     * @param uploadImage 图片上传管理
     * @return 结果
     */
    public int updateUploadImage(UploadImage uploadImage);

    /**
     * 批量删除图片上传管理
     * 
     * @param ids 需要删除的图片上传管理主键集合
     * @return 结果
     */
    public int deleteUploadImageByIds(Long[] ids);

    /**
     * 删除图片上传管理信息
     * 
     * @param id 图片上传管理主键
     * @return 结果
     */
    public int deleteUploadImageById(Long id);
    
    /**
     * 上传图片并记录
     * 
     * @param file 上传的文件
     * @param uploadType 上传类型 (battle/resource/other)
     * @return 上传的图片信息
     */
    public UploadImage uploadImage(MultipartFile file, String uploadType);
    
    /**
     * 获取图片访问URL
     * 
     * @param imageId 图片ID
     * @return 图片URL
     */
    public String getImageUrl(Long imageId);
}