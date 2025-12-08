package com.ruoyi.common.utils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 字符串数组类型处理器
 * 用于处理数据库中的JSON字符串与Java中的字符串数组之间的转换
 * 
 * @author lhb
 */
@MappedTypes(String[].class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> 
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException 
    {
        try 
        {
            // 将字符串数组转换为JSON字符串
            String jsonValue = objectMapper.writeValueAsString(parameter);
            ps.setString(i, jsonValue);
        } 
        catch (JsonProcessingException e) 
        {
            throw new SQLException("Error converting String[] to JSON string", e);
        }
    }

    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException 
    {
        String jsonValue = rs.getString(columnName);
        return parseJsonToStringArray(jsonValue);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException 
    {
        String jsonValue = rs.getString(columnIndex);
        return parseJsonToStringArray(jsonValue);
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException 
    {
        String jsonValue = cs.getString(columnIndex);
        return parseJsonToStringArray(jsonValue);
    }

    /**
     * 将JSON字符串转换为字符串数组
     * 
     * @param jsonValue JSON字符串
     * @return 字符串数组
     */
    private String[] parseJsonToStringArray(String jsonValue) 
    {
        if (jsonValue == null || jsonValue.trim().isEmpty()) 
        {
            return new String[0];
        }
        
        try 
        {
            // 尝试解析为数组
            String[] result = objectMapper.readValue(jsonValue, String[].class);
            return result != null ? result : new String[0];
        } 
        catch (Exception e) 
        {
            // 如果解析失败，返回包含原始字符串的单元素数组
            return new String[] { jsonValue };
        }
    }
}