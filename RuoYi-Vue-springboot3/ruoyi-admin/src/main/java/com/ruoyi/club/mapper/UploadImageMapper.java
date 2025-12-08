package com.ruoyi.club.mapper;

import java.util.List;
import com.ruoyi.club.domain.UploadImage;

/**
 * 图片上传管理Mapper接口
 * 
 * @author lhb
 * @date 2025-11-19
 */
public interface UploadImageMapper 
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
     * 删除图片上传管理
     * 
     * @param id 图片上传管理主键
     * @return 结果
     */
    public int deleteUploadImageById(Long id);

    /**
     * 批量删除图片上传管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUploadImageByIds(Long[] ids);
}