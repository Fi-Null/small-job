package com.small.job.admin.dao;

import com.small.job.admin.model.SmallJobUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 2:01 PM
 */
@Mapper
public interface SmallJobUserDao {

    public List<SmallJobUser> pageList(@Param("offset") int offset,
                                       @Param("pagesize") int pagesize,
                                       @Param("username") String username,
                                       @Param("role") int role);

    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("username") String username,
                             @Param("role") int role);

    public SmallJobUser loadByUserName(@Param("username") String username);

    public int save(SmallJobUser SmallJobUser);

    public int update(SmallJobUser SmallJobUser);

    public int delete(@Param("id") int id);

}
