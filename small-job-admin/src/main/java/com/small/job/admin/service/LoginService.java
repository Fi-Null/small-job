package com.small.job.admin.service;

import com.small.job.admin.dao.SmallJobUserDao;
import com.small.job.admin.model.SmallJobUser;
import com.small.job.admin.util.CookieUtil;
import com.small.job.admin.util.JacksonUtil;
import com.small.job.core.biz.model.ReturnT;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 2:08 PM
 */
@Configuration
public class LoginService {

    public static final String LOGIN_IDENTITY_KEY = "SMALL_JOB_LOGIN_IDENTITY";

    @Resource
    private SmallJobUserDao userDao;


    private String makeToken(SmallJobUser smallJobUser) {
        String tokenJson = JacksonUtil.writeValueAsString(smallJobUser);
        String tokenHex = new BigInteger(tokenJson.getBytes()).toString(16);
        return tokenHex;
    }

    private SmallJobUser parseToken(String tokenHex) {
        SmallJobUser smallJobUser = null;
        if (tokenHex != null) {
            String tokenJson = new String(new BigInteger(tokenHex, 16).toByteArray());      // username_password(md5)
            smallJobUser = JacksonUtil.readValue(tokenJson, SmallJobUser.class);
        }
        return smallJobUser;
    }


    public ReturnT<String> login(HttpServletRequest request, HttpServletResponse response, String username, String password, boolean ifRemember) {

        // param
        if (username == null || username.trim().length() == 0 || password == null || password.trim().length() == 0) {
            return new ReturnT<>(500, "login_param_empty");
        }

        // valid passowrd
        SmallJobUser SmallJobUser = userDao.loadByUserName(username);
        if (SmallJobUser == null) {
            return new ReturnT<>(500, "login_param_unvalid");
        }
        String passwordMd5 = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!passwordMd5.equals(SmallJobUser.getPassword())) {
            return new ReturnT<>(500, "login_param_unvalid");
        }

        String loginToken = makeToken(SmallJobUser);

        // do login
        CookieUtil.set(response, LOGIN_IDENTITY_KEY, loginToken, ifRemember);
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @param response
     */
    public ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @return
     */
    public SmallJobUser ifLogin(HttpServletRequest request, HttpServletResponse response) {
        String cookieToken = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
        if (cookieToken != null) {
            SmallJobUser cookieUser = null;
            try {
                cookieUser = parseToken(cookieToken);
            } catch (Exception e) {
                logout(request, response);
            }
            if (cookieUser != null) {
                SmallJobUser dbUser = userDao.loadByUserName(cookieUser.getUsername());
                if (dbUser != null) {
                    if (cookieUser.getPassword().equals(dbUser.getPassword())) {
                        return dbUser;
                    }
                }
            }
        }
        return null;
    }

}
