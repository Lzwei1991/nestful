package xyz.lzw.nestful.ref.model;

/**
 * 方法参数的元数据信息：
 * - 参数来源（路径 / 上下文 / 请求 / body / query）
 * - 参数类型
 * - 对应名称（PATH 时为路径组名，QUERY 时为参数名）
 */
public final class ParamMeta {

    public enum ParamSource {
        PATH,           // @Path
        CTX,            // ChannelHandlerContext
        REQ,            // FullHttpRequest
        QUERY,          // @Param, 从 query string 读取
        BODY_OR_DEFAULT // 从 body 解析，或默认构造
    }

    private final ParamSource source;
    private final Class<?> type;
    private final String name; // PATH: 分组名；QUERY: 参数名；其他来源可为 null

    public ParamMeta(ParamSource source, Class<?> type, String name) {
        this.source = source;
        this.type = type;
        this.name = name;
    }

    public ParamSource getSource() {
        return source;
    }

    public Class<?> getType() {
        return type;
    }

    /**
     * PATH: 分组名；QUERY: 参数名；其他情况可能为 null。
     */
    public String getName() {
        return name;
    }
}
