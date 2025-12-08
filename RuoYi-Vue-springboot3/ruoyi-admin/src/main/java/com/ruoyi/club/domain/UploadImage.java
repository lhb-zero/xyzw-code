package com.ruoyi.club.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 图片上传管理对象 upload_image
 * 
 * @author lhb
 * @date 2025-11-19
 */
public class UploadImage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;
    /**
     * 访问路径
     */
    private String accessPath;
    /** 原始文件名 */
    @Excel(name = "原始文件名")
    private String originalName;

    /** 服务器存储路径 */
    @Excel(name = "服务器存储路径")
    private String storagePath;

    /** 文件大小（字节） */
    @Excel(name = "文件大小")
    private Long fileSize;

    /** 文件类型（jpg/png/jpeg） */
    @Excel(name = "文件类型")
    private String fileType;

    /** 图片宽度（px） */
    @Excel(name = "图片宽度")
    private Long imageWidth;

    /** 图片高度（px） */
    @Excel(name = "图片高度")
    private Long imageHeight;

    /** 上传类型 */
    @Excel(name = "上传类型", readConverterExp = "battle=盐场战绩,resource=资源日志,other=其他")
    private String uploadType;

    /** 上传状态 */
    @Excel(name = "上传状态", readConverterExp = "uploaded=已上传,processing=处理中,processed=已处理,deleted=已删除")
    private String uploadStatus;

    /** 文件MD5值 */
    private String md5Hash;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上传时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOriginalName(String originalName) 
    {
        this.originalName = originalName;
    }

    public String getOriginalName() 
    {
        return originalName;
    }

    public void setStoragePath(String storagePath) 
    {
        this.storagePath = storagePath;
    }

    public String getStoragePath() 
    {
        return storagePath;
    }

    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }

    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }

    public void setImageWidth(Long imageWidth) 
    {
        this.imageWidth = imageWidth;
    }

    public Long getImageWidth() 
    {
        return imageWidth;
    }

    public void setImageHeight(Long imageHeight) 
    {
        this.imageHeight = imageHeight;
    }

    public Long getImageHeight() 
    {
        return imageHeight;
    }

    public void setUploadType(String uploadType) 
    {
        this.uploadType = uploadType;
    }

    public String getUploadType() 
    {
        return uploadType;
    }

    public void setUploadStatus(String uploadStatus) 
    {
        this.uploadStatus = uploadStatus;
    }

    public String getUploadStatus() 
    {
        return uploadStatus;
    }

    public void setMd5Hash(String md5Hash) 
    {
        this.md5Hash = md5Hash;
    }

    public String getMd5Hash() 
    {
        return md5Hash;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("originalName", getOriginalName())
            .append("storagePath", getStoragePath())
            .append("fileSize", getFileSize())
            .append("fileType", getFileType())
            .append("imageWidth", getImageWidth())
            .append("imageHeight", getImageHeight())
            .append("uploadType", getUploadType())
            .append("uploadStatus", getUploadStatus())
            .append("md5Hash", getMd5Hash())
            .append("createdAt", getCreatedAt())
            .append("accessPath", getAccessPath())
            .toString();
    }

    public String getAccessPath() {
        return accessPath;
    }

    public void setAccessPath(String accessPath) {
        this.accessPath = accessPath;
    }
}