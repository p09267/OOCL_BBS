package com.jfinalbbs.interceptor;

import com.jfinalbbs.common.Constants;
import com.jfinalbbs.user.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *  用户拦截器
 */
public class UserInterceptor implements Interceptor {
    public void intercept(Invocation ai) {
        HttpServletRequest request = ai.getController().getRequest();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Constants.USER_SESSION);
        if(user == null) {
            String uri = request.getRequestURI();
            String param = "";
            if(request.getQueryString() != null) {
                try {
                    param = new String(request.getQueryString().getBytes("ISO8859-1"),"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if(!param.equals("")) uri += "?"+param;
            session.setAttribute(Constants.BEFORE_URL, uri);
            ai.getController().getResponse().setCharacterEncoding("utf-8");
            try {
                ai.getController().getResponse().getWriter().write("<script>alert('请先登录');location.href=\'/\'</script>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ai.invoke();
        }
    }
}
