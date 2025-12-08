package com.ruoyi.club.mapper;

import java.util.List;
import com.ruoyi.club.domain.BattleMatchResult;

/**
 * 战绩匹配结果Mapper接口
 * 
 * @author lhb
 * @date 2025-11-19
 */
public interface BattleMatchResultMapper 
{
    /**
     * 查询战绩匹配结果
     * 
     * @param id 战绩匹配结果主键
     * @return 战绩匹配结果
     */
    public BattleMatchResult selectBattleMatchResultById(Long id);

    /**
     * 查询战绩匹配结果列表
     * 
     * @param battleMatchResult 战绩匹配结果
     * @return 战绩匹配结果集合
     */
    public List<BattleMatchResult> selectBattleMatchResultList(BattleMatchResult battleMatchResult);

    /**
     * 新增战绩匹配结果
     * 
     * @param battleMatchResult 战绩匹配结果
     * @return 结果
     */
    public int insertBattleMatchResult(BattleMatchResult battleMatchResult);

    /**
     * 修改战绩匹配结果
     * 
     * @param battleMatchResult 战绩匹配结果
     * @return 结果
     */
    public int updateBattleMatchResult(BattleMatchResult battleMatchResult);

    /**
     * 删除战绩匹配结果
     * 
     * @param id 战绩匹配结果主键
     * @return 结果
     */
    public int deleteBattleMatchResultById(Long id);

    /**
     * 批量删除战绩匹配结果
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBattleMatchResultByIds(Long[] ids);
}