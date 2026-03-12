package xyz.lzw.nestful.ref.model;

/**
 * 方法参数的元数据信息：
 * - 参数来源（路径 / 上下文 / 请求 / body）
 * - 参数类型
 * - 路径变量名称（仅当来源为 PATH 时有效）
 */
public final class ParamMeta {

    public enum ParamSource {
        PATH,           // @Path
        CTX,            // ChannelHandlerContext
        REQ,            // FullHttpRequest
        BODY_OR_DEFAULT // 从 body 解析，或默认构造
    }

    private final ParamSource source;
    private final Class<?> type;
    private final String pathGroupName; // 仅 PATH 时有效

    public ParamMeta(ParamSource source, Class<?> type, String pathGroupName) {
        this.source = source;
        this.type = type;
        this.pathGroupName = pathGroupName;
    }

    public ParamSource getSource() {
        return source;
    }

    public Class<?> getType() {
        return type;
    }

    public String getPathGroupName() {
        return pathGroupName;
    }
}
