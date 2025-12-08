package com.ruoyi.club.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.club.domain.UploadImage;
import com.ruoyi.club.mapper.UploadImageMapper;
import com.ruoyi.club.service.IUploadImageService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.StringUtils;

import java.awt.image.BufferedImage;

/**
 * 图片上传管理Service业务层处理
 *
 * @author lhb
 * @date 2025-11-19
 */
@Service
public class UploadImageServiceImpl implements IUploadImageService
{
    @Autowired
    private UploadImageMapper uploadImageMapper;

    @Value("${club.upload.path}")
    private String uploadPath;

    @Value("${club.upload.domain}")
    private String domain;

    @Autowired
    private RuoYiConfig ruoYiConfig;

    /**
     * 查询图片上传管理
     *
     * @param id 图片上传管理主键
     * @return 图片上传管理
     */
    @Override
    public UploadImage selectUploadImageById(Long id)
    {
        return uploadImageMapper.selectUploadImageById(id);
    }

    /**
     * 查询图片上传管理列表
     *
     * @param uploadImage 图片上传管理
     * @return 图片上传管理
     */
    @Override
    public List<UploadImage> selectUploadImageList(UploadImage uploadImage)
    {
        return uploadImageMapper.selectUploadImageList(uploadImage);
    }

    /**
     * 新增图片上传管理
     *
     * @param uploadImage 图片上传管理
     * @return 结果
     */
    @Override
    public int insertUploadImage(UploadImage uploadImage)
    {
        return uploadImageMapper.insertUploadImage(uploadImage);
    }

    /**
     * 修改图片上传管理
     *
     * @param uploadImage 图片上传管理
     * @return 结果
     */
    @Override
    public int updateUploadImage(UploadImage uploadImage)
    {
        return uploadImageMapper.updateUploadImage(uploadImage);
    }

    /**
     * 批量删除图片上传管理
     *
     * @param ids 需要删除的图片上传管理主键
     * @return 结果
     */
    @Override
    public int deleteUploadImageByIds(Long[] ids)
    {
        return uploadImageMapper.deleteUploadImageByIds(ids);
    }

    /**
     * 删除图片上传管理信息
     *
     * @param id 图片上传管理主键
     * @return 结果
     */
    @Override
    public int deleteUploadImageById(Long id)
    {
        UploadImage image = selectUploadImageById(id);
        if (image != null && StringUtils.isNotEmpty(image.getStoragePath())) {
            // 删除文件系统中的图片文件
            try {
                Path filePath = Paths.get(image.getStoragePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                // 记录日志，但不影响数据库删除
                System.err.println("删除图片文件失败: " + e.getMessage());
            }
        }
        return uploadImageMapper.deleteUploadImageById(id);
    }

    /**
     * 上传图片并记录
     *
     * @param file 上传的文件
     * @param uploadType 上传类型
     * @return 上传的图片信息
     */
    @Override
    public UploadImage uploadImage(MultipartFile file, String uploadType)
    {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 构建带类型目录的上传路径
            String uploadDir = RuoYiConfig.getProfile() + "/images/" + uploadType;
            
            // 使用统一的文件上传工具类，传入自定义上传目录
            String accessPath = FileUploadUtils.upload(uploadDir, file);
            
            // 构建实际存储路径
            String relativePath = StringUtils.substringAfter(accessPath, Constants.RESOURCE_PREFIX + "/");
            String storagePath = RuoYiConfig.getProfile() + "/" + relativePath;

            // 获取图片信息
            BufferedImage image = ImageIO.read(file.getInputStream());
            int width = image.getWidth();
            int height = image.getHeight();

            // 计算MD5
            String md5 = calculateMD5(file);

            // 创建图片记录对象
            UploadImage uploadImage = new UploadImage();
            uploadImage.setOriginalName(file.getOriginalFilename());
            uploadImage.setStoragePath(storagePath);  // 实际存储路径
            uploadImage.setAccessPath(accessPath);    // 访问路径
            uploadImage.setFileSize(file.getSize());
            uploadImage.setFileType(getFileExtension(file.getOriginalFilename()));
            uploadImage.setImageWidth((long) width);
            uploadImage.setImageHeight((long) height);
            uploadImage.setUploadType(uploadType);
            uploadImage.setUploadStatus("uploaded");
            uploadImage.setMd5Hash(md5);
            uploadImage.setCreatedAt(new Date());

            // 保存到数据库
            insertUploadImage(uploadImage);

            return uploadImage;
        } catch (Exception e) {
            System.err.println("图片上传失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取图片访问URL
     *
     * @param imageId 图片ID
     * @return 图片URL
     */
    @Override
    public String getImageUrl(Long imageId)
    {
        UploadImage image = selectUploadImageById(imageId);
        if (image == null) {
            return null;
        }

        // 优先返回访问路径，如果没有则使用存储路径
        if (StringUtils.isNotEmpty(image.getAccessPath())) {
            String accessPath = image.getAccessPath();
            
            // 确保访问路径以/profile开头，不重复斜杠
            if (!accessPath.startsWith(Constants.RESOURCE_PREFIX)) {
                accessPath = Constants.RESOURCE_PREFIX + "/" + StringUtils.removeStart(accessPath, "/");
            }
            
            // 移除多余的斜杠
            accessPath = accessPath.replaceAll("/+", "/");
            
            // 如果配置了域名，返回完整URL
            if (StringUtils.isNotEmpty(domain)) {
                // 确保域名不重复斜杠
                String cleanDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
                return cleanDomain + accessPath;
            }
            
            return accessPath;
        }

        // 如果配置了域名，返回完整URL，否则返回相对路径
        if (StringUtils.isNotEmpty(domain)) {
            // 确保域名不重复斜杠
            String cleanDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
            String path = image.getStoragePath();
            // 确保路径以/profile开头
            if (!path.startsWith(Constants.RESOURCE_PREFIX)) {
                path = Constants.RESOURCE_PREFIX + "/" + StringUtils.removeStart(path, "/");
            }
            // 移除多余的斜杠
            path = path.replaceAll("/+", "/");
            return cleanDomain + path;
        } else {
            return image.getStoragePath();
        }
    }

    /**
     * 获取实际上传根路径
     */
    private String getUploadRealPath() {
        // 如果配置了自定义上传路径，使用配置的路径
        if (StringUtils.isNotEmpty(uploadPath)) {
            return uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        }
        // 否则使用RuoYi默认配置
        return RuoYiConfig.getUploadPath();
    }

    /**
     * 获取文件访问路径
     */
    private String getUploadAccessPath() {
        // 如果配置了域名，返回空路径（因为会拼接域名）
        if (StringUtils.isNotEmpty(domain)) {
            return "/";
        }
        // 否则使用RuoYi默认的资源映射路径，避免重复前缀
        return "/";
    }

    /**
     * 计算文件的MD5值
     *
     * @param file 文件
     * @return MD5值
     */
    private String calculateMD5(MultipartFile file)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename)
    {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(dotIndex + 1).toLowerCase();
    }
}