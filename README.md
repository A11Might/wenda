# 目录：

<!-- TOC -->

- [document](/document.md)

- [一、注册与登录的实现](#注册与登录的实现)

- [二、发布问题和敏感词过滤](#发布问题和敏感词过滤)

- [三、发表评论和站内信](#发表评论和站内信)

- [四、赞踩实现](#赞踩实现)

- [五、异步消息机制与邮件发送](#异步消息机制与邮件发送)

- [六、关注和被关注服务实现](#关注和被关注服务实现)

- [七、timeline](#timeline)

<!-- /TOC -->

## 注册与登录的实现

#### 注册

将用户输入用户名和密码提交至服务器，服务器入库。在入库前需要判断，用户名的合法性(长度，敏感词，重复，特殊字符等)，密码长度及组成

- 为了对用户信息负责，在用户注册时，随机生成该用户的 salt ，将用户密码 salt 加密(加密 [password + salt] )后存入数据库，数据库中不会存储用户明文密码

    - 明文存储用户密码是对用户信息的不负责

    - 密码直接加密存储，数据库脱库后可能会被人撞库(社工库，常用密码事先加密入库，解密时直接在库中查找)

- 为了防止恶意脚本注册，可以设置用户邮件/短信激活

#### 登录/登出

- 登录

新建数据表 login_ticket 用来存储 ticket 字段。该字段在注册或用户登录成功时随机生成并与用户关联后存入数据库，同时添加进浏览器的 Cookie 中

ticket 既不是密码，也不是密码加密后的数据，也不是userId，是一个随机生成的 UUID 字符串，有过期时间以及有效状态

- 登出

将数据库中该用户对应的ticket过期

#### 页面访问

在访问页面时，浏览器将 cookie 中 ticket 发送给服务器验证(带 token 的 http 请求)，服务器通过 ticket 字段获取 ticket 的具体信息(过期时间，是否有效)以及关联用户id(可以获取用户的具体信息)，根据用户和页面访问权限进行页面渲染或者页面跳转

具体实现分为

- 面向切面编程(AOP)

- 拦截器(interceptor)

    使用拦截器( preHandle )来拦截所有浏览器请求，判断请求中是否存在有效的 ticket，如果有就获取关联用户信息并写入 Threadlocal(本地线程：当前变量每个线程都有一份拷贝，通过统一的接口访问)。所有线程的 threadlocal 都被存在一个叫做 hostholder 的实例中，根据该实例就可以在全局任意位置(controller, service...中)获取当前登录用户的信息

#### 未登录跳转next

使用拦截器( preHandle )判断用户是否登录，若该页面强制登录但用户未登录，则直接跳转至登录页面，并将当前页面url作为参数传递过去，在用户登录后再跳回当前页面

## 发布问题和敏感词过滤

#### 发布问题

发布问题时检查标题和内容(UGC，用户生成内容)，防止 xss 注入，并且过滤敏感词，后存入数据库

防止 xss 注入直接使用 HTMLutils 工具类封装的方法即可

#### 敏感词过滤

读取一份保存敏感词的文本文件来初始化敏感词字典树，遍历UGC的每个字符，判断以该字符开始的字符串是否是敏感词，是则进行打码处理，实现敏感词过滤。然后将过敏感词过滤作为一个service，让需要过滤敏感词的服务进行调用

## 发表评论和站内信

#### 通用的新模块开发流程

1. Database column：业务模型设计数据库字段

2. Model：定义模型，和数据库相匹配

3. DAO：数据读取

4. Service：服务包装

5. Controller：业务入口

6. Test

#### 评论中心和消息中心

统一评论服务，在数据库中建立表 comment 表来存储每个实体的评论(问题的评论、评论的评论...)。每一个问题下面都有评论，显示评论数量，具体内容，发布评论的用户等信息。发布评论时，需要更新该问题的评论数，要求同时更新 comment 表和 question 表(更新表中的content_count，冗余的数据项用于问题页面显示评论数，只需查找question表)，使用事务保证两个表的更新同时成功或失败，本项目使用redis做成异步更新评论数

在数据库中建立表 message 表来存储每条站内信。每两个用户之间的站内信，会有一个唯一的conversation_id，通过conversation_id可以选出两用户之间所有的站内信，用于实现私信详情页面；通过数据库 group by 操作可以获取当前用户与其他用户之间站内信的最新一条，用于实现私信列表页面

站内信分为两种：

- 轮询，每隔一段时间去服务器上查询有无新的站内信，可以直接从数据库中读取(与查询不同，读取像timeline中的pull)

- 长连接，websocket一直连接服务器，一旦有新的站内信，服务器主动通知，站内信可以第一时间知道

## 赞踩实现

点赞点踩功能(给评论点赞踩、给问题点赞踩...)不关注发生时间和顺序，所以使用redis的两个集合记录所有点赞点踩的人，用于判断用户点赞点踩与否和获取该实体(评论、问题...)点赞点踩的总人数

根据业务确定唯一的 key 与之对应，格式：前缀是业务用分隔符与参数结合起来

如点赞："LIKE" + ":" + entityType + ":" + entityId

## 异步消息机制与邮件发送

在之前的功能中有一些不需要实时执行的操作或者任务，可以异步处理，提高网站性能

#### 单向队列实现异步

生产者：操作(点赞踩，评论...)触发事件，将事件包装成 Event，序列化后加入 redis 的单向队列

消费者：从单向队列中取出 Event，使用对应 handler 处理

![img](https://github.com/A11Might/A11Might.github.io/blob/master/img/nowcoder2016/1_1_1.jpg)

#### 邮件发送

使用spring提供的模板实现邮件发送

## 关注和被关注服务实现

- 使用Redis的zset存储每个实体(用户, 问题...)的的粉丝列表以及关注某个实体的关注列表，zset中每个元素有对应的score，可以储存关注发生的时间来排序元素，方便获取最近关注的粉丝或问题

    每一个实体的粉丝列表以及关注某个实体的关注列表对应唯一key，用于用户向列表插入或删除元素，实现关注和取关功能

    - 对于粉丝列表，除了显示粉丝的基本信息之外，还要显示当前用户是否关注了这个粉丝，以便前端显示

    - 对于关注列表来说，如果被关注对象是用户的话，除了显示用户的基本信息之外，还要显示当前用户是被这个用户关注，以便前端显示

    关注/取关功能有两个操作，向粉丝列表中添加/删除元素，向某个实体的关注列表中添加/删除元素，需要同时成功，所以使用Redis的事务multi来包装成事务再进行执行操作

- 实体被关注会收到站内信通知，关注成功时触发关注事件，异步使用handler处理

## 新鲜事

#### timeline

- 事件触发产生新鲜事(如评论、关注...)

- 粉丝新鲜事列表获取(push、pull、push and pull)

    1. 推：事件触发后广播给所有的粉丝
        - 对于粉丝数过多的事件后台压力较大，浪费存储空间
        - 流程清晰，开发难度低，关注新用户需要同步新feed流

    2. 拉：登录打开页面的时候根据关注的实体动态生成timeline内容
        - 读取压力大
        - 存储占用小，缓存最近读取的feed，根据时间分区拉去

    3. 推拉：活跃/在线用户推，其他用户拉
        - 降低存储空间，又满足大部分用户的读取需求

- 各新鲜事自定义渲染(每条新鲜事不同，如评论问题、关注问题...)

    1. timeline新鲜事统一存储，类似flyweight模式(享元模式：底层数据只有一份，所有用户公共去引用它)，存储事件的核心变量(不是渲染出来的html信息，而是原始数据)

    2. 模板和变量整合渲染

![img](https://github.com/A11Might/A11Might.github.io/blob/master/img/nowcoder2016/1_1_2.jpg)

- 新鲜事排序显示

- 广告推荐整合

#### 实现

使用feedhandler异步处理用户关注问题和评论问题两个新鲜事

当上述事件发生时，根据具体事件构造新鲜事，包括：发起者，日期，新鲜事类型，新鲜事的具体内容，然后将该数据存入MySQL数据库的feed表中，并将新鲜事id存储当前用户所有粉丝的timeline中(本例使用Redis来存储某个用户接受的新鲜事id列表，称为 timeline，根据每个用户的唯一key来存储)

pull：通过当前用户关注者的列表，从数据库中 `查找` 所有关注者产生的新鲜事

push：通过当前用户timeline中的所有新鲜事id， `读取` 数据库中的新鲜事
