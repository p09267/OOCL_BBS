package com.jfinalbbs.user;

import com.jfinalbbs.collect.Collect;
import com.jfinalbbs.common.BaseController;
import com.jfinalbbs.common.Constants;
import com.jfinalbbs.interceptor.UserInterceptor;
import com.jfinalbbs.notification.Notification;
import com.jfinalbbs.topic.Topic;
import com.jfinalbbs.utils.ImageUtil;
import com.jfinalbbs.utils.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;

import java.util.List;


public class UserController extends BaseController {

    public void index() {
        String id = getPara(0);
        User user = User.me.findById(id);
        if(user != null) {
            setAttr("current_user", user);
            Page<Collect> collectPage = Collect.me.findByAuthorId(getParaToInt("p", 1),
                    PropKit.use("config.properties").getInt("page_size"), user.getStr("id"));
            setAttr("collectPage", collectPage);
            Page<Topic> topics = Topic.me.paginateByAuthorId(1, 5, user.getStr("id"));
            setAttr("topics", topics);
            //查询我回复的话题
            Page<Topic> myReplyTopics = Topic.me.paginateMyReplyTopics(1, 5, user.getStr("id"));
            setAttr("myReplyTopics", myReplyTopics);
            render("front/user/index.html");
        } else {
            renderText(Constants.OP_ERROR_MESSAGE);
        }
    }

    public void collects() {
        String uid = getPara(0);
        User user = User.me.findById(uid);
        if(user == null) {
            renderText(Constants.OP_ERROR_MESSAGE);
        } else {
            setAttr("current_user", user);
            Page<Collect> collectPage = Collect.me.findByAuthorIdWithTopic(getParaToInt("p", 1),
                    PropKit.use("config.properties").getInt("page_size"), user.getStr("id"));
            setAttr("page", collectPage);
            render("front/user/collects.html");
        }
    }

    public void topics() {
        String uid = getPara(0);
        User user = User.me.findById(uid);
        if(user == null) {
            renderText(Constants.OP_ERROR_MESSAGE);
        } else {
            setAttr("current_user", user);
            Page<Topic> page = Topic.me.paginateByAuthorId(getParaToInt("p", 1),
                    PropKit.use("config.properties").getInt("page_size"), user.getStr("id"));
            setAttr("page", page);
            render("front/user/topics.html");
        }
    }

    public void replies() {
        String uid = getPara(0);
        User user = User.me.findById(uid);
        if(user == null) {
            renderText(Constants.OP_ERROR_MESSAGE);
        } else {
            setAttr("current_user", user);
            Page<Topic> myReplyTopics = Topic.me.paginateMyReplyTopics(getParaToInt("p", 1), PropKit.use("config.properties").getInt("page_size"), user.getStr("id"));
            setAttr("page", myReplyTopics);
            render("front/user/replies.html");
        }
    }

    public void top100() {
        List<User> top100 = User.me.findBySize(100);
        setAttr("top100", top100);
        render("front/user/top100.html");
    }

    @Before(UserInterceptor.class)
    public void message() {
        String uid = getPara(0);
        if(StrUtil.isBlank(uid)) renderText(Constants.OP_ERROR_MESSAGE);
        List<Notification> notifications = Notification.me.findNotReadByAuthorId(uid);
        setAttr("notifications", notifications);
        Page<Notification> oldMessages = Notification.me.paginate(getParaToInt("p", 1),
                PropKit.use("config.properties").getInt("page_size"), uid);
        setAttr("oldMessages", oldMessages);
        //将消息置为已读
        Notification.me.updateNotification(uid);
        render("front/user/message.html");
    }


    @Before(UserInterceptor.class)
    public void cancelBind() {
        String pt = getPara("pt");
        if(StrUtil.isBlank(pt)) {
            error("非法操作");
        } else {
            User user = (User) getSession().getAttribute(Constants.USER_SESSION);
            if(pt.equalsIgnoreCase(Constants.ThirdLogin.QQ)) {
                user.set("qq_open_id", null)
                        .set("qq_avatar", null)
                        .set("qq_nickname", null)
                        .update();
            } else if(pt.equalsIgnoreCase(Constants.ThirdLogin.SINA)) {
                user.set("sina_open_id", null)
                        .set("sina_avatar", null)
                        .set("sina_nickname", null)
                        .update();
            }/* else if(pt.equalsIgnoreCase(Constants.ThirdLogin.WECHAT)) {
                user.set("wechat_open_id", null)
                        .set("wechat_avatar", null)
                        .update();
            }*/
            setSessionAttr(Constants.USER_SESSION, user);
            setCookie(Constants.USER_COOKIE, StrUtil.getEncryptionToken(user.getStr("token")), 30 * 24 * 60 * 60);
            success();
        }
    }


}
