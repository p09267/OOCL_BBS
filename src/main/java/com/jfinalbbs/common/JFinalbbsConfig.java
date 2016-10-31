package com.jfinalbbs.common;

import com.jfinalbbs.collect.Collect;
import com.jfinalbbs.collect.CollectController;
import com.jfinalbbs.handler.HtmlHandler;

import com.jfinalbbs.index.IndexController;
import com.jfinalbbs.interceptor.CommonInterceptor;
import com.jfinalbbs.label.Label;
import com.jfinalbbs.label.LabelController;
import com.jfinalbbs.label.LabelTopicId;
import com.jfinalbbs.notification.Notification;
import com.jfinalbbs.notification.NotificationController;
import com.jfinalbbs.reply.Reply;

import com.jfinalbbs.reply.ReplyController;
import com.jfinalbbs.section.Section;
import com.jfinalbbs.topic.Topic;

import com.jfinalbbs.topic.TopicController;
import com.jfinalbbs.valicode.ValiCode;
import com.jfinal.config.Constants;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.HashKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinalbbs.user.*;


public class JFinalbbsConfig extends JFinalConfig {
	
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用getProperty(...)获取值
		loadPropertyFile("config.properties");
		me.setDevMode(getPropertyToBoolean("devMode", false));
        me.setBaseUploadPath(com.jfinalbbs.common.Constants.UPLOAD_DIR);
		me.setMaxPostSize(2048000);
    }
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/", IndexController.class, "page");	 // 第三个参数为该Controller的视图存放路径
		me.add("/topic", TopicController.class, "page");
		me.add("/user", UserController.class, "page");
		me.add("/reply", ReplyController.class, "page");
		me.add("/collect", CollectController.class, "page");
		me.add("/notification", NotificationController.class, "page");
		me.add("/label", LabelController.class, "page");
	}

	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		//配置Druid数据库池
        DruidPlugin druidPlugin = new DruidPlugin(getProperty("jdbcUrl"), getProperty("user"), getProperty("password").trim());
        druidPlugin.setFilters("stat,wall");

		me.add(druidPlugin);

        me.add(new EhCachePlugin());
		
		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setShowSql(getPropertyToBoolean("showSql", false));
		me.add(arp);
		arp.addMapping("topic", Topic.class);	// 映射blog 表到 Blog模型
		arp.addMapping("reply", Reply.class);
		arp.addMapping("user", User.class);
		arp.addMapping("collect", Collect.class);
		arp.addMapping("notification", Notification.class);
		arp.addMapping("section", Section.class);
		arp.addMapping("valicode", ValiCode.class);
		arp.addMapping("label", Label.class);
		arp.addMapping("label_topic_id", LabelTopicId.class);
	}

	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
        me.add(new SessionInViewInterceptor());
        me.add(new CommonInterceptor());
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
        //配置druid的监听，可以在浏览器里输入http://localhost:8080/druid 查看druid监听的数据
        me.add(new DruidStatViewHandler("/druid"));
        me.add(new HtmlHandler());
	}
	
	/**
	 * 建议使用 JFinal 手册推荐的方式启动项目
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		// System.out.println(HashKit.md5("123456"));  开发测试用
		JFinal.start("src/main/webapp", 8080, "/", 5);
	}
}
