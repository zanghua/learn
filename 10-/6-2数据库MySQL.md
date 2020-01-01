## MySql

### [SQL执行流程](https://github.com/Snailclimb/JavaGuide/blob/master/docs/database/一条sql语句在mysql中如何执行的.md)

- MySQL 主要分为 Server 层和引擎层，Server 层主要包括连接器、查询缓存、分析器、优化器、执行器，同时还有一个日志模块（binlog），这个日志模块所有执行引擎都可以共用,redolog 只有 InnoDB 有。
- 引擎层是插件式的，目前主要包括，MyISAM,InnoDB,Memory 等。
- 查询语句的执行流程如下：权限校验（如果命中缓存）---》查询缓存---》分析器---》优化器---》权限校验---》执行器---》引擎
- 更新语句执行流程如下：分析器----》权限校验----》执行器---》引擎---redo log(prepare 状态---》binlog---》redo log(commit状态)





[Mysql优化](https://www.nowcoder.com/discuss/150059)

[面试官问你MySQL的优化，看这篇文章就够了](https://mp.weixin.qq.com/s/hxy_qe1O7r0rYLChUOURcw)

