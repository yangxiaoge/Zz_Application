package com.bruce.kotlin.readhub.bean

/**
 * Created by yangjianan on 2018/8/8.
 */
class HotTopic {

    /**
     * id : 5rK4DPDokNF
     * createdAt : 2018-08-08T08:11:44.259Z
     * nelData : {"state":true,"result":[{"weight":0.3318900465965271,"nerName":"小米","entityId":22,"entityName":"小米","entityType":"company","entityUniqueId":"baike_3250213"},{"weight":0.48195597529411316,"nerName":"TTC","entityId":-1,"entityName":"","entityType":"company","entityUniqueId":-1},{"weight":0.25386419892311096,"nerName":"Sonix","entityId":-1,"entityName":"","entityType":"product","entityUniqueId":-1},{"weight":0.8593307137489319,"nerName":"小米游戏键盘","entityId":-1,"entityName":"","entityType":"product","entityUniqueId":-1}],"nerResult":{"person":{},"company":{"TTC":{"weight":0.48195597529411316},"小米":{"weight":0.3318900465965271}},"product":{"Sonix":{"weight":0.25386419892311096},"小米游戏键盘":{"weight":0.8593307137489319}},"location":{}}}
     * eventData : {"result":[{"eventId":1,"entityId":22,"eventType":10,"entityName":"小米","entityType":"company","entityUniqueId":"baike_3250213"}]}
     * newsArray : [{"id":19577432,"url":"https://tech.sina.com.cn/n/k/2018-08-08/doc-ihhkuskt4922559.shtml","title":"小米游戏键盘上架：全新轴体，RGB灯效","groupId":1,"siteName":"新浪","siteSlug":"rss_sina","mobileUrl":"https://tech.sina.com.cn/n/k/2018-08-08/doc-ihhkuskt4922559.shtml","authorName":"","duplicateId":1,"publishDate":"2018-08-08T00:05:09.000Z"}]
     * order : 59619
     * publishDate : 2018-08-08T08:11:44.288Z
     * summary : 小米游戏键盘今天正式发售，售价229元。小米游戏键盘是一款104键的机械键盘，全键RGB幻彩背光，轴体采用了与TTC合作定制的“全新独立游戏手感轴体”，键盘上盖材质为5052韩系铝材，键帽是双色注塑的ABS键帽，配有2种高度的脚垫 ... 小米游戏键盘搭载了来自Sonix的32位ARM主控，主频48M，回报率可达1000Hz，支持左手大区33键无冲。
     *
     *
     * title : 小米游戏键盘发售：104键RGB、TTC轴、售229元
     *
     *
     * updatedAt : 2018-08-08T08:18:26.746Z
     * timeline : null
     * extra : {"instantView":false}
     */

     val id: String? = null
     val createdAt: String? = null
    //    private NelDataBean nelData;
    //    private EventDataBean eventData;
     val order: Long = 0
     val publishDate: String? = null
    var summary: String? = null
    var title: String? = null
     val updatedAt: String? = null
     val timeline: Any? = null
    //    private ExtraBean extra;
    //    private java.util.List<NewsArrayBean> newsArray;
}
