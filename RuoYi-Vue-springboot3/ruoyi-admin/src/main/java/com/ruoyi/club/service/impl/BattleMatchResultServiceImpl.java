package com.ruoyi.club.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.club.mapper.BattleMatchResultMapper;
import com.ruoyi.club.domain.BattleMatchResult;
import com.ruoyi.club.service.IBattleMatchResultService;

/**
 * 战绩匹配结果Service业务层处理
 * 
 * @author lhb
 * @date 2025-11-19
 */
@Service
public class BattleMatchResultServiceImpl implements IBattleMatchResultService 
{
    @Autowired
    private BattleMatchResultMapper battleMatchResultMapper;

    /**
     * 查询战绩匹配结果
     * 
     * @param id 战绩匹配结果主键
     * @return 战绩匹配结果
     */
    @Override
    public BattleMatchResult selectBattleMatchResultById(Long id)
    {
        return battleMatchResultMapper.selectBattleMatchResultById(id);
    }

    /**
     * 查询战绩匹配结果列表
     * 
     * @param battleMatchResult 战绩匹配结果
     * @return 战绩匹配结果
     */
    @Override
    public List<BattleMatchResult> selectBattleMatchResultList(BattleMatchResult battleMatchResult)
    {
        return battleMatchResultMapper.selectBattleMatchResultList(battleMatchResult);
    }

    /**
     * 新增战绩匹配结果
     * 
     * @param battleMatchResult 战绩匹配结果
     * @return 结果
     */
    @Override
    public int insertBattleMatchResult(BattleMatchResult battleMatchResult)
    {
        battleMatchResult.setCreateTime(DateUtils.getNowDate());
        return battleMatchResultMapper.insertBattleMatchResult(battleMatchResult);
    }

    /**
     * 修改战绩匹配结果
     * 
     * @param battleMatchResult 战绩匹配结果
     * @return 结果
     */
    @Override
    public int updateBattleMatchResult(BattleMatchResult battleMatchResult)
    {
        battleMatchResult.setUpdateTime(DateUtils.getNowDate());
        return battleMatchResultMapper.updateBattleMatchResult(battleMatchResult);
    }

    /**
     * 批量删除战绩匹配结果
     * 
     * @param ids 需要删除的战绩匹配结果主键
     * @return 结果
     */
    @Override
    public int deleteBattleMatchResultByIds(Long[] ids)
    {
        return battleMatchResultMapper.deleteBattleMatchResultByIds(ids);
    }

    /**
     * 删除战绩匹配结果信息
     * 
     * @param id 战绩匹配结果主键
     * @return 结果
     */
    @Override
    public int deleteBattleMatchResultById(Long id)
    {
        return battleMatchResultMapper.deleteBattleMatchResultById(id);
    }
}