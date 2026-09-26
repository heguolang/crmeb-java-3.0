package com.qxkj.service.service.impl;

import com.qxkj.service.service.WechatUserService;
import org.springframework.stereotype.Service;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Service
public class WechatUserServiceImpl implements WechatUserService {


//    /**
//     * 消息推送
//     * @param userId 用户id
//     * @param newsId 图文消息id
//     * @author Mr.Zhang
//     * @since 2020-04-11
//     * @return Boolean
//     */
//    @Override
//    public void push(String userId, Integer newsId) {
//        //检查文章是否存在
//        Article article = articleService.getById(newsId);
//        if(article == null){
//            throw new QianxuException("你选择的文章不存在！");
//        }
//
////        {
////            "touser":"od9iXwsAl3c0e3POY39awOq0nnJ4",
////            "msgtype":"news",
////            "news":{
////                "articles": [
////                    {
////                        "title":"Happy Day",
////                        "description":"Is Really A Happy Day",
// （已移除演示站注释）
////                        "picurl":"https://wuht-1300909283.cos.ap-chengdu.myqcloud.com/image/wechat/2020/06/16/003b595d6cc544dd981d3468d5caafa38p24bq7sa7.jpg"
////                    }
////                ]
////            }
////        }
//        List<Integer> userIdList = QianxuUtil.stringToArray(userId);
//        List<UserToken> userList = userTokenService.getList(userIdList);
//        if(null == userList){
//            throw new QianxuException("没有用户关注微信号");
//        }
//
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("msgtype", "news");
//
//        HashMap<String, Object> articleInfo = new HashMap<>();
//        ArrayList<Object> articleList = new ArrayList<>();
//
//        HashMap<String, String> articleInfoItem = new HashMap<>();
//
//        for (UserToken userToken : userList) {
//            map.put("touser", userToken.getToken());
//
//            articleInfoItem.put("title", article.getTitle());
//            articleInfoItem.put("description", article.getSynopsis());
//            articleInfoItem.put("url", article.getUrl()); //前端地址或者三方地址
//            articleInfoItem.put("picurl", article.getImageInput());
//            articleList.add(articleInfoItem);
//            articleInfo.put("articles", articleList);
//            map.put("news", articleInfo);
//            weChatService.pushKfMessage(map);
//        }
//    }

}
