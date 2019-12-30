package com.small.job.admin.dao;

import com.small.job.admin.model.SmallJobGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 1:58 PM
 */
@Mapper
public interface SmallJobGroupDao {

    public List<SmallJobGroup> findAll();

    public List<SmallJobGroup> findByAddressType(@Param("addressType") int addressType);

    public int save(SmallJobGroup SmallJobGroup);

    public int update(SmallJobGroup SmallJobGroup);

    public int remove(@Param("id") int id);

    public SmallJobGroup load(@Param("id") int id);
}
