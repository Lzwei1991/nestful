# nestful
本项目基于 netty、 guice ，是一个 IOT http 框架，适合用于构建 restful 的 api。
与其他基于 netty 的 http 框架不一样的是，其他 http 都将 netty 封装起来，本项目只是利用反射调用，inject 注入的方式去处理。
使用者只需要在 netty 的 channel pipe 中加入 RestfulHandler 即可使用。
