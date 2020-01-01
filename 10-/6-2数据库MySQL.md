## MySql

### 一、索引  

mysql的索引数据结构采取的是B+树，几种适合快速查找的数据结构，比如：hash(哈希)、二叉树，红黑树，B+树。

BTree和B+Tree的区别：BTree每个节点都可以存储数据，而B+Tree只有叶子节点才存储数据，第二B+Tree在mysql的实现时还做了定制，可以看到相邻的叶子节点间加了链式的关联的。这样的优势是在进行范围查找时会很快。

1. [参考1.mysql索引面试](https://blog.csdn.net/javawcj123/article/details/79824020)

红黑树的规则：
1）每个结点要么是红的，要么是黑的。
2）根结点是黑的。
3）每个叶结点（叶结点即指树尾端NIL指针或NULL结点）是黑的。
4）如果一个结点是红的，那么它的俩个儿子都是黑的。
5）对于任一结点而言，其到叶结点树尾端NIL指针的每一条路径都包含相同数目的黑结点。

设计红黑树的目的，**就是解决平衡树的维护起来比较麻烦的问题，红黑树，读取略逊于AVL，维护强于AVL，每次插入和删除的平均旋转次数应该是远小于平衡树。**

小结一下：

能用平衡树的地方，就可以用红黑树。用红黑树之后，读取略逊于AVL，维护强于AVL。

* [[对B+树，B树，红黑树的理解](https://www.cnblogs.com/myseries/p/10662710.html)]

### [SQL执行流程](https://github.com/Snailclimb/JavaGuide/blob/master/docs/database/一条sql语句在mysql中如何执行的.md)

- MySQL 主要分为 Server 层和引擎层，Server 层主要包括连接器、查询缓存、分析器、优化器、执行器，同时还有一个日志模块（binlog），这个日志模块所有执行引擎都可以共用,redolog 只有 InnoDB 有。
- 引擎层是插件式的，目前主要包括，MyISAM,InnoDB,Memory 等。
- 查询语句的执行流程如下：权限校验（如果命中缓存）---》查询缓存---》分析器---》优化器---》权限校验---》执行器---》引擎
- 更新语句执行流程如下：分析器----》权限校验----》执行器---》引擎---redo log(prepare 状态---》binlog---》redo log(commit状态)

[Mysql优化](https://www.nowcoder.com/discuss/150059)

[面试官问你MySQL的优化，看这篇文章就够了](https://mp.weixin.qq.com/s/hxy_qe1O7r0rYLChUOURcw)

