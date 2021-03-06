package com.tongji.boying.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.tongji.boying.common.exception.Asserts;
import com.tongji.boying.config.BoyingUserDetails;
import com.tongji.boying.model.User;
import com.tongji.boying.security.util.JwtTokenUtil;
import com.tongji.boying.service.UserCacheService;
import com.tongji.boying.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Random;


/**
 * 用户管理Service实现类
 */
@Service
public class UserServiceImpl implements UserService {
    //    便于日志的打印
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);


    /**
     * 用于密码加密
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * token工具类
     */
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 对用户信息进行一些缓存操作
     */
    @Autowired
    private UserCacheService userCacheService;
    /**
     * 验证码的前缀与过期时间
     */
    @Value("${redis.key.authCode}")
    private String REDIS_KEY_PREFIX_AUTH_CODE;
    @Value("${redis.expire.authCode}")
    private Long AUTH_CODE_EXPIRE_SECONDS;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public User getByUsername(String username) {
        User user = userCacheService.getUser(username);
        if (user != null) return user; //缓存里面有数据

        String sql = "select * from boying_user where username = ?";
        try {
            user = jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                User tempUser = new User();
                tempUser.setUserId(resultSet.getInt("user_id"));
                tempUser.setUsername(resultSet.getString("username"));
                tempUser.setPhone(resultSet.getString("phone"));
                tempUser.setPassword(resultSet.getString("password"));
//                tempUser.setRealName(resultSet.getString("real_name"));
//                tempUser.setIdentityNumber(resultSet.getString("identity_number"));
//                tempUser.setEmail(resultSet.getString("email"));
//                tempUser.setAge(resultSet.getInt("age"));
//                tempUser.setGender(resultSet.getBoolean("gender"));
                tempUser.setStatus(resultSet.getBoolean("status"));
                tempUser.setIcon(resultSet.getString("icon"));
                return tempUser;
            }, username);
            System.out.println(user);
        }
        catch (Exception e) {
            Asserts.fail("用户不存在!");
        }
        //账号未启用
        if (!user.getStatus()) {
            Asserts.fail("账号未启用,请联系管理员!");
        }
        userCacheService.setUser(user);//将查询到的数据放入缓存中
        return user;
    }


    @Override
    @DateTimeFormat
    public void register(String username, String password, String telephone, String authCode, String icon) {
        //验证验证码
        if (!verifyAuthCode(authCode, telephone)) {
            Asserts.fail("验证码错误");
        }
        String sql = "select count(*) from boying_user where username = ? or phone = ?";
        Integer userCount = jdbcTemplate.queryForObject(sql, Integer.class, username, telephone);

        if (userCount != null && userCount != 0) {
            Asserts.fail("用户名或手机号已存在！");
        }

        try {
            //没有该用户进行添加操作
            sql = "select max(user_id) from boying_user";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            if (count == null) {
                count = 1;
            }
            if (icon == null) {
                icon = "https://tongji4m3.oss-cn-beijing.aliyuncs.com/f_f_object_156_s512_f_object_156_0.png";
            }
            sql = "INSERT INTO boying_user (user_id,username,password,phone,icon,status)VALUES (?, ?, ?, ?, ?,?)";
            int update = jdbcTemplate.update(sql, count + 1, username, passwordEncoder.encode(password), telephone, icon, "1");
            //注册完删除验证码,每个验证码只能使用一次
            userCacheService.delAuthCode(telephone);
        }
        catch (Exception e) {

        }
    }

    @Override
    public void generateAuthCode(String telephone) {
        //简单生成6位验证码
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        //为该手机号生成验证码
        userCacheService.setAuthCode(telephone, sb.toString());

//        阿里云 短信服务代码
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI4G94MzD6ozcAAS3yq3zS", "xdeS40tkkA3zC3F7d8szDJ7fu1N3Ch");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", telephone);
        request.putQueryParameter("SignName", "博影娱乐票务平台");
        request.putQueryParameter("TemplateCode", "SMS_205120016");
        request.putQueryParameter("TemplateParam", "{\"code\":" + sb.toString() + "}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        }
        catch (ServerException e) {
            e.printStackTrace();
        }
        catch (ClientException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void updatePassword(String telephone, String password, String authCode) {
        //代做
    }

    @Override
    public User getCurrentUser() {
//        获取之前登录存储的用户上下文信息
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = ctx.getAuthentication();
        BoyingUserDetails userDetails = (BoyingUserDetails) auth.getPrincipal();
        return userDetails.getUser();
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = getByUsername(username);
        if (user != null) {
            return new BoyingUserDetails(user);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    @Override
    public String login(String username, String password) {
        String token = null;
        //密码需要客户端加密后传递,但是传递的仍然是明文
        try {
            UserDetails userDetails = loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new BadCredentialsException("密码不正确");
            }
//            获取该用户的上下文信息（如他的角色列表）
//            username和password被获得后封装到一个UsernamePasswordAuthenticationToken
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//            围绕该用户建立安全上下文（security context）
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
        }
        catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }


    @Override
    public String telephoneLogin(String telephone, String password) {
        String token = null;
        //密码需要客户端加密后传递,但是传递的仍然是明文
        try {
            User user = userCacheService.getUserByTelephone(telephone);
            //缓存里面没有数据
            if (user == null) {
                String sql = "select * from boying_user where phone = ?";
                try {
                    user = jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                        User tempUser = new User();
                        tempUser.setUserId(resultSet.getInt("user_id"));
                        tempUser.setUsername(resultSet.getString("username"));
                        tempUser.setPhone(resultSet.getString("phone"));
                        tempUser.setPassword(resultSet.getString("password"));
                        tempUser.setStatus(resultSet.getBoolean("status"));
                        return tempUser;
                    }, telephone);
                }
                catch (Exception e) {
                    Asserts.fail("手机号不存在!");
                }
                userCacheService.setUser(user);//将查询到的数据放入缓存中

                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new BadCredentialsException("密码不正确");
                }
            }
            UserDetails userDetails = loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
        }
        catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }

    @Override
    public String authCodeLogin(String telephone, String authCode) {
        //验证验证码
        if (!verifyAuthCode(authCode, telephone)) {
            Asserts.fail("验证码错误");
        }
        String token = null;
        try {
            User user = userCacheService.getUserByTelephone(telephone);
            //缓存里面没有数据
            if (user == null) {
                String sql = "select * from boying_user where phone = ?";
                try {
                    user = jdbcTemplate.queryForObject(sql, (resultSet, i) -> {
                        User tempUser = new User();
                        tempUser.setUserId(resultSet.getInt("user_id"));
                        tempUser.setUsername(resultSet.getString("username"));
                        tempUser.setPhone(resultSet.getString("phone"));
                        tempUser.setPassword(resultSet.getString("password"));
                        tempUser.setStatus(resultSet.getBoolean("status"));
                        return tempUser;
                    }, telephone);
                }
                catch (Exception e) {
                    Asserts.fail("手机号不存在!");
                }

            }
            UserDetails userDetails = loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
        }
        catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        //注册完删除验证码,每个验证码只能使用一次
        userCacheService.delAuthCode(telephone);
        return token;
    }

    @Override
    public String refreshToken(String token) {
        return jwtTokenUtil.refreshHeadToken(token);
    }

    @Override
    public void updateInfo(String realName, String identityNumber, String email, String icon, int age, boolean gender) {
        //代做
    }

    //对输入的验证码进行校验
    private boolean verifyAuthCode(String authCode, String telephone) {
        if (StringUtils.isEmpty(authCode)) {
            return false;
        }
//        redis中存储了该手机号未过期的验证码
        String realAuthCode = userCacheService.getAuthCode(telephone);
        return authCode.equals(realAuthCode);
    }
}
