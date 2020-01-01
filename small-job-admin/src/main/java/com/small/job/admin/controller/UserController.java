package com.small.job.admin.controller;

import com.small.job.admin.controller.annotation.PermissionLimit;
import com.small.job.admin.dao.SmallJobGroupDao;
import com.small.job.admin.dao.SmallJobUserDao;
import com.small.job.admin.model.SmallJobGroup;
import com.small.job.admin.model.SmallJobUser;
import com.small.job.admin.service.LoginService;
import com.small.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author xiangke
 * @Date 2020/1/1 22:56
 * @Version 1.0
 **/
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private SmallJobUserDao smallJobUserDao;
    @Resource
    private SmallJobGroupDao smallJobGroupDao;

    @RequestMapping
    @PermissionLimit(adminuser = true)
    public String index(Model model) {

        // 执行器列表
        List<SmallJobGroup> groupList = smallJobGroupDao.findAll();
        model.addAttribute("groupList", groupList);

        return "user/user.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String username, int role) {

        // page list
        List<SmallJobUser> list = smallJobUserDao.pageList(start, length, username, role);
        int list_count = smallJobUserDao.pageListCount(start, length, username, role);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);    // 过滤后的总记录数
        maps.put("data", list);                    // 分页列表
        return maps;
    }

    @RequestMapping("/add")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> add(SmallJobUser smallJobUser) {

        // valid username
        if (!StringUtils.hasText(smallJobUser.getUsername())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "system_please_input" + "user_username");
        }
        smallJobUser.setUsername(smallJobUser.getUsername().trim());
        if (!(smallJobUser.getUsername().length() >= 4 && smallJobUser.getUsername().length() <= 20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "system_lengh_limit" + "[4-20]");
        }
        // valid password
        if (!StringUtils.hasText(smallJobUser.getPassword())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "system_please_input" + "user_password");
        }
        smallJobUser.setPassword(smallJobUser.getPassword().trim());
        if (!(smallJobUser.getPassword().length() >= 4 && smallJobUser.getPassword().length() <= 20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "system_lengh_limit" + "[4-20]");
        }
        // md5 password
        smallJobUser.setPassword(DigestUtils.md5DigestAsHex(smallJobUser.getPassword().getBytes()));

        // check repeat
        SmallJobUser existUser = smallJobUserDao.loadByUserName(smallJobUser.getUsername());
        if (existUser != null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "user_username_repeat");
        }

        // write
        smallJobUserDao.save(smallJobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/update")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> update(HttpServletRequest request, SmallJobUser smallJobUser) {

        // avoid opt login seft
        SmallJobUser loginUser = (SmallJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
        if (loginUser.getUsername().equals(smallJobUser.getUsername())) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "user_update_loginuser_limit");
        }

        // valid password
        if (StringUtils.hasText(smallJobUser.getPassword())) {
            smallJobUser.setPassword(smallJobUser.getPassword().trim());
            if (!(smallJobUser.getPassword().length() >= 4 && smallJobUser.getPassword().length() <= 20)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "system_lengh_limit" + "[4-20]");
            }
            // md5 password
            smallJobUser.setPassword(DigestUtils.md5DigestAsHex(smallJobUser.getPassword().getBytes()));
        } else {
            smallJobUser.setPassword(null);
        }

        // write
        smallJobUserDao.update(smallJobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/remove")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> remove(HttpServletRequest request, int id) {

        // avoid opt login seft
        SmallJobUser loginUser = (SmallJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
        if (loginUser.getId() == id) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "user_update_loginuser_limit");
        }

        smallJobUserDao.delete(id);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public ReturnT<String> updatePwd(HttpServletRequest request, String password) {

        // valid password
        if (password == null || password.trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), "密码不可为空");
        }
        password = password.trim();
        if (!(password.length() >= 4 && password.length() <= 20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "system_lengh_limit" + "[4-20]");
        }

        // md5 password
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // update pwd
        SmallJobUser loginUser = (SmallJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);

        // do write
        SmallJobUser existUser = smallJobUserDao.loadByUserName(loginUser.getUsername());
        existUser.setPassword(md5Password);
        smallJobUserDao.update(existUser);

        return ReturnT.SUCCESS;
    }

}
