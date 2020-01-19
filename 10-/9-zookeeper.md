# Zookeeper

​		ZooKeeper 是一个开放源码的分布式协调服务，它是集群的管理者，监视着集群中各个节点的状态根据节点提交的反馈进行下一步合理操作。最终，将简单易用的接口和性能高效、功能稳定的系统提供给用户。
​		分布式应用程序可以基于 Zookeeper 实现诸如数据发布/订阅、负载均衡、命名服务、分布式协调/通知、集群管理、Master 选举、分布式锁和分布式队列等功能。

Zookeeper 保证了如下分布式一致性特性：顺原单可实

1. 顺序一致性 
2. 原子性
3. 单一视图
4. 可靠性
5. 实时性（最终一致性）
   - 客户端的读请求可以被集群中的任意一台机器处理，如果读请求在节点上注册了监听器，这个监听器也是由所连接的 zookeeper 机器来处理。
   - 对于写请求，这些请求会同时发给其他 zookeeper 机器并且达成一致后，请求才会返回成功。因此，随着 zookeeper 的集群机器增多，读请求的吞吐会提高但是写请求的吞吐会下降。
   - 有序性是 zookeeper 中非常重要的一个特性，所有的更新都是全局有序的，每个更新都有一个唯一的时间戳，这个时间戳称为 zxid（Zookeeper Transaction Id）。
   - 而读请求只会相对于更新有序，也就是读请求的返回结果中会带有这个zookeeper 最新的 zxid。

### ZooKeeper 提供了什么？

1、文件系统
2、通知机制

### Zookeeper 文件系统

- Zookeeper 提供一个多层级的节点命名空间（节点称为 znode）。与文件系统不同的是，这些节点都可以设置关联的数据，而文件系统中只有文件节点可以存放数据而目录节点不行。
- Zookeeper 为了保证高吞吐和低延迟，在内存中维护了这个树状的目录结构，这种特性使得 Zookeeper 不能用于存放大量的数据，每个节点的存放数据上限为1M。

### ZAB 协议？

- ZAB 协议是为分布式协调服务 Zookeeper 专门设计的一种支持**崩溃恢复**的原子广播协议。
- ZAB 协议包括两种基本的模式：**崩溃恢复**和**消息广播**。
  - 当整个 zookeeper 集群刚刚启动或者 Leader 服务器宕机、重启或者网络故障导致不存在过半的服务器与 Leader 服务器保持正常通信时，所有进程（服务器）进入崩溃恢复模式，首先选举产生新的 Leader 服务器，然后集群中 Follower 服务器开始与新的 Leader 服务器进行数据同步，当集群中超过半数机器与该 Leader服务器完成数据同步之后，退出恢复模式进入消息广播模式，Leader 服务器开始接收客户端的事务请求生成事物提案来进行事务请求处理。

### 四种类型的数据节点 Znode

1、PERSISTENT /pərˈsɪstənt/-持久节点：除非手动删除，否则节点一直存在于 Zookeeper 上

2、EPHEMERAL /ɪˈfemərəl/-临时节点：临时节点的生命周期与客户端会话绑定，一旦客户端会话失效（客户端与zookeeper 连接断开不一定会话失效），那么这个客户端创建的所有临时节点都会被移除。

3、PERSISTENT_SEQUENTIAL-持久顺序节点：基本特性同持久节点，只是增加了顺序属性，节点名后边会追加一个由父节点维护的自增整型数字。

4、EPHEMERAL_SEQUENTIAL-临时顺序节点：基本特性同临时节点，增加了顺序属性，节点名后边会追加一个由父节点维护的自增整型数字。

### Zookeeper Watcher 机制 -- 数据变更通知

Zookeeper 允许客户端向服务端的某个 Znode 注册一个 Watcher 监听，当服务端的一些指定事件触发了这个 Watcher，服务端会向指定客户端发送一个事件通知来实现分布式的通知功能，然后客户端根据 Watcher 通知状态和事件类型做出业务上的改变。
工作机制：
1、客户端注册 watcher
2、服务端处理 watcher
3、客户端回调 watcher

- Watcher 特性总结：
  1、一次性
  无论是服务端还是客户端，一旦一个 Watcher 被触发，Zookeeper 都会将其从相应的存储中移除。这样的设计有效的减轻了服务端的压力，不然对于更新非常频繁的节点，服务端会不断的向客户端发送事件通知，无论对于网络还是服务端的压力都非常大。
  2、客户端串行执行
  客户端 Watcher 回调的过程是一个串行同步的过程。
  3、轻量
  3.1、Watcher 通知非常简单，只会告诉客户端发生了事件，而不会说明事件的具
  体内容。
  3.2、客户端向服务端注册 Watcher 的时候，并不会把客户端真实的 Watcher 对象实体传递到服务端，仅仅是在客户端请求中使用 boolean 类型属性进行了标记。4、watcher event 异步发送 watcher 的通知事件从 server 发送到 client 是异步的，这就存在一个问题，不同的客户端和服务器之间通过 socket 进行通信，由于网络延迟或其他因素导致客户端在不通的时刻监听到事件，由于 Zookeeper 本身提供了 ordering guarantee，即客户端监听事件后，才会感知它所监视 znode发生了变化。所以我们使用 Zookeeper 不能期望能够监控到节点每次的变化。
- 4、Zookeeper 只能保证最终的一致性，而无法保证强一致性。
- 5、注册 watcher getData、exists、getChildren
- 6、触发 watcher create、delete、setData
- 7、当一个客户端连接到一个新的服务器上时，watch 将会被以任意会话事件触发。当与一个服务器失去连接的时候，是无法接收到 watch 的。而当 client 重新连接时，如果需要的话，所有先前注册过的 watch，都会被重新注册。通常这是完全透明的。只有在一个特殊情况下，watch 可能会丢失：对于一个未创建的 znode
  的 exist watch，如果在客户端断开连接期间被创建了，并且随后在客户端连接上之前又删除了，这种情况下，这个 watch 事件可能会被丢失。

7. 客户端注册 Watcher 实现
1、调用 getData()/getChildren()/exist()三个 API，传入 Watcher 对象
2、标记请求 request，封装 Watcher 到 WatchRegistration
3、封装成 Packet 对象，发服务端发送 request
4、收到服务端响应后，将 Watcher 注册到 ZKWatcherManager 中进行管理
5、请求返回，完成注册。

- 服务端处理 Watcher 实现
  1、服务端接收 Watcher 并存储
  接收到客户端请求，处理请求判断是否需要注册 Watcher，需要的话将数据节点的节点路径和 ServerCnxn（ServerCnxn 代表一个客户端和服务端的连接，实现了 Watcher 的 process 接口，此时可以看成一个 Watcher 对象）存储在WatcherManager 的 WatchTable 和 watch2Paths 中去。
  2、Watcher 触发
  以服务端接收到 setData() 事务请求触发 NodeDataChanged 事件为例：
  2.1 封装 WatchedEvent
  将通知状态（SyncConnected）、事件类型（NodeDataChanged）以及节点路径封装成一个 WatchedEvent 对象
  2.2 查询 Watcher
  从 WatchTable 中根据节点路径查找 Watcher
  2.3 没找到；说明没有客户端在该数据节点上注册过 Watcher
  2.4 找到；提取并从 WatchTable 和 Watch2Paths 中删除对应 Watcher（从这里
  可以看出 Watcher 在服务端是一次性的，触发一次就失效了）
  3、调用 process 方法来触发 Watcher
  这里 process 主要就是通过 ServerCnxn 对应的 TCP 连接发送 Watcher 事件通知。

9. 客户端回调 Watcher
客户端 SendThread 线程接收事件通知，交由 EventThread 线程回调 Watcher。
客户端的 Watcher 机制同样是一次性的，一旦被触发后，该 Watcher 就失效了。
11. Chroot 特性
3.2.0 版本后，添加了 Chroot 特性，该特性允许每个客户端为自己设置一个命名
空间。如果一个客户端设置了 Chroot，那么该客户端对服务器的任何操作，都将
会被限制在其自己的命名空间下。
通过设置 Chroot，能够将一个客户端应用于 Zookeeper 服务端的一颗子树相对
应，在那些多个应用公用一个 Zookeeper 进群的场景下，对实现不同应用间的相
互隔离非常有帮助。
12. 会话管理
分桶策略：将类似的会话放在同一区块中进行管理，以便于 Zookeeper 对会话进行不同区块的隔离处理以及同一区块的统一处理。
分配原则：每个会话的“下次超时时间点”（ExpirationTime）
计算公式：
ExpirationTime_ = currentTime + sessionTimeout
ExpirationTime = (ExpirationTime_ / ExpirationInrerval + 1) *ExpirationInterval , ExpirationInterval 是指 Zookeeper 会话超时检查时间间隔，默认 tickTime
13. 服务器角色
Leader
1、事务请求的唯一调度和处理者，保证集群事务处理的顺序性
2、集群内部各服务的调度者
Follower /ˈfɑːloʊər/
1、处理客户端的非事务请求，转发事务请求给 Leader 服务器
2、参与事务请求 Proposal 的投票
3、参与 Leader 选举投票
Observer
1、3.0 版本以后引入的一个服务器角色，在不影响集群事务处理能力的基础上提
升集群的非事务处理能力
2、处理客户端的非事务请求，转发事务请求给 Leader 服务器
3、不参与任何形式的投票
13. Zookeeper 下 Server 工作状态
    服务器具有四种状态，分别是 LOOKING、FOLLOWING、LEADING、OBSERVING。
    1、LOOKING：寻找 Leader 状态。当服务器处于该状态时，它会认为当前集群中
    没有 Leader，因此需要进入 Leader 选举状态。
    2、FOLLOWING：跟随者状态。表明当前服务器角色是 Follower。
    3、LEADING：领导者状态。表明当前服务器角色是 Leader。
    4、OBSERVING：观察者状态。表明当前服务器角色是 Observer。
14. 数据同步
    整个集群完成 Leader 选举之后，Learner（Follower 和 Observer 的统称）回向Leader 服务器进行注册。当 Learner 服务器想 Leader 服务器完成注册后，进入数据同步环节。
    数据同步流程：（均以消息传递的方式进行）
    Learner 向 Learder 注册
    数据同步
    同步确认
    Zookeeper 的数据同步通常分为四类：
    1、直接差异化同步（DIFF 同步）
    2、先回滚再差异化同步（TRUNC+DIFF 同步）
    3、仅回滚同步（TRUNC 同步）
    4、全量同步（SNAP 同步）
    在进行数据同步前，Leader 服务器会完成数据同步初始化。
    ————————————————
    版权声明：本文为CSDN博主「秃桔子」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
    原文链接：https://blog.csdn.net/qq_41482054/article/details/102203755

### zookeeper是怎么选举出来leader的？或者说选举机制是怎样的？

- 当leader崩溃或者leader失去大多数的follower时，zk就进入**恢复模式**，恢复模式即需要重新选举出一个新的leader，让所有的节点都恢复到一个正确的状态。

- Zk的选举算法有两种：一种是基于BasicLeaderElection实现的，另外一种是基于FastLeaderElection算法实现的。系统**默认的选举算法为FastLeaderElection**

- 基于FastLeaderElection算法的选举机制的两种情况下选举
- 第一种：全新选举
  - 假设目前有 5 台服务器，每台服务器均没有数据，它们的编号分别是1,2,3,4,5,按编号依次启动，它们的选择举过程如下：
  - 服务器 1 启动，给自己投票，然后发投票信息，由于其它机器还没有启动所以它收不到反馈信息，服务器 1 的状态一直属于 Looking。
  - 服务器 2 启动，给自己投票，同时与之前启动的服务器 1 交换结果，由于服务器 2 的编号大所以服务器 2 胜出，但此时投票数没有大于半数，所以两个服务器的状态依然是 LOOKING。
  - 服务器 3 启动，给自己投票，同时与之前启动的服务器 1,2 交换信息，由于服务器 3 的编号最大所以服务器 3 胜出，此时投票数正好大于半数，所以服务器 3 成为领导者，服务器 1,2 成为小弟。
  - 服务器 4 启动，给自己投票，同时与之前启动的服务器 1,2,3 交换信息，尽管服务器 4 的编号大，但之前服务器 3 已经胜出，所以服务器 4 只能成为小弟。
  - 服务器 5 启动，后面的逻辑同服务器 4 成为小弟
- 第二种：非全新选举
  - 对于运行正常的 zookeeper 集群，中途有机器 down 掉，需要重新选举时，选举过程就需要加入数据 ID、服务器 ID 和逻辑时钟。
  - 这样选举的标准就变成：
  - 1、逻辑时钟小的选举结果被忽略，重新投票；
  - 2、统一逻辑时钟后，数据 id 大的胜出；
  - 3、数据 id 相同的情况下，服务器 id 大的胜出；
  - 根据这个规则选出 leader。数据ID 大  -》 服务ID 大的服务
  
- 基于BasicLeaderElection算法的选举机制
  - 选举线程是一个独立的线程，其主要功能是对投票结果进行统计，并选出推荐的Server；
  - 选举线程首先向所有节点发起一次询问(包括自己)；在收到回复后，验证是否是自己发起的询问(验证zxid是否一致)，然后获取对方的id(myid)，并存储到当前询问对象列表中，最后获取对方提议的leader相关信息(id,zxid)，并将这些信息存储到当次选举的投票记录表中





## 同类对比





## 参考

ZooKeeper 面试题 :https://blog.csdn.net/qq_41482054/article/details/102203755





https://blog.csdn.net/qq_34988624/article/details/86433658

https://blog.csdn.net/Sunshine_2211468152/article/details/87938175

