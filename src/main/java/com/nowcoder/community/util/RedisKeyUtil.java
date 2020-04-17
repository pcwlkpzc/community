package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";

    /**
     * 表示被点赞的实体
     */
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    /**
     * 表示点赞的用户
     */
    private static final String PREFIX_USER_LIKE = "like:user";

    /**
     * followee:表示被关注的实体
     */
    private static final String PREFIX_FOLLOWEE = "followee";

    /**
     * follower:表示粉丝
     */
    private static final String PREFIX_FOLLOWER = "follower";

    /**
     * 表示验证码的前缀
     */
    private static final String PREFIX_KAPTCHA = "kaptcha";

    /**
     * 登录凭证
     */
    private static final String PREFIX_TICKET = "ticket";

    /**
     * 查询user
     */
    private static final String PREFIX_USER = "user";

    /**
     * 独立访问量
     */
    private static final String PREFIX_UV = "uv";

    /**
     * 日活跃用户
     */
    private static final String PREFIX_DAU = "dau";

    /**
     * 帖子的id
     */
    private static final String PREFIX_POST="post";

    /**
     * 某个实体类的赞
     * 存放的键值对：like:entity:entityType:entityId ---- > set(userId)
     * 表示某个给某个实体类点赞的用户的集合
     * @param entityType 实体类的类型
     * @param entityId 实体类的id
     * @return
     */
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户收到的所有的赞
     * 存放的键值对：like:user:userId ---> int
     * 表示某个用户收到的点赞的总数量
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体
     * 存放的键值对: followee:userId:entityType --> zSet(entityId,now)
     * 表示某个用户，关注此实体类的所有实体对象,
     * value对应的是用户关注的全部内容
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT +userId + SPLIT + entityType;
    }

    /**
     * 某个实体对象所拥有的全部粉丝
     * 存放的键值对:follower:entityType:entityId --> zSet(userId,now)
     * 表示关注此实体类的所有用户的集合,
     * value对应的是全部粉丝
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 登录验证码的key
     *
     * 每一个验证码应该与其对应的
     * @param owner
     * @return
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 获取登录凭证的key
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 查询用户的key
     * @param userId
     * @return
     */
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 单日UV
     * @param date
     * @return
     */
    public static  String getUVKey (String date){
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 区间UV
     * 需要统计一段时间以内的所有UV
     * @param startDate
     * @param endDate
     * @return
     */
    public static  String getUVKey (String startDate, String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 单日活跃用户
     * @param date
     * @return
     */
    public static  String getDAUKey (String date){
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 区间活跃用户
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getDAUKey(String startDate, String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 传入一个查询帖子分数的key
     * @return
     */
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
