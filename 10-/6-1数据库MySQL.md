# Mysql

## 一、事务  

### 概念

事务指的是满足 ACID 特性的一组操作，可以通过 Commit 提交一个事务，也可以使用 Rollback 进行回滚。  

### ACID （事务特性）

1. 原子性（Atomicity）
   事务被视为不可分割的最小单元，事务的所有操作要么全部提交成功，要么全部失败回滚。
   回滚可以用回滚日志来实现，回滚日志记录着事务所执行的修改操作，在回滚时反向执行这些修改操作即可。
2. 一致性（Consistency）
  在一致性状态下，所有事务对一个数据的读取结果都是相同的。数据库在事务执行前后都保持一致性状态。
3. 隔离性（Isolation）
   一个事务所做的修改在最终提交以前，对其它事务是不可见的。
4. 持久性（Durability）
   一旦事务提交，则其所做的修改将会永远保存到数据库中。即使系统发生崩溃，事务执行的结果也不能丢失。
   使用重做日志来保证持久性。  
5. 总结：事务的 ACID 特性概念简单，但不是很好理解，主要是因为这几个特性不是一种平级关系：
   * 只有满足一致性，事务的执行结果才是正确的。
   * 在无并发下，事务串行执行，隔离性一定能够满足。只要能满足原子性，一定能满足一致性。
   * 在并发下，多个事务并行执行，事务不仅要满足原子性，还需要满足隔离性，才能满足一致性。
   * 事务满足持久化是为了能应对数据库崩溃的情况  
6. MySQL 默认采用自动提交模式(AUTOCOMMIT )。  也就是说，如果不显式使用 START TRANSACTION 语句来开始一个事务，那么每个查询都会被当做一个事务自动提交。  

## 二、并发一致性问题  

在并发环境下，事务的隔离性很难保证，因此会出现很多并发一致性问题。  

1. **丢失修改**：两个事务，T2的修改覆盖了T1的修改。
2. **读脏数据**：两个事务，T1读取到T2未提交的数据，T2撤销了修改。
3. **不可重复读**：T1两次个数据不一致，因T2在两次之间修改了数据。
4. **幻读**：对于插入、删除来说，T1两次读取这个范围的数据不一致。

产生并发不一致性问题主要原因是破坏了事务的隔离性，解决方法是通过**并发控制**来保证隔离性。并发控制可以通过**加锁**来实现，但是封锁操作需要用户自己控制，相当复杂。数据库管理系统提供了事务的**隔离级别**，让用户以一种更轻松的方式处理并发一致性问题。  



## 三、封锁

### 锁粒度  

MySQL 中提供了两种封锁粒度：**行级锁以及表级锁**。  

1. 锁粒度尽量小：只锁定需要修改的那部分数据，而不是所有的资源。锁定的数据量越少，发生锁争用的可能就越小，系统的并发程度就越高。  
2. 锁粒度越小，系统开销就越大：加锁需要消耗资源，锁的各种操作（包括获取锁、释放锁、以及检查锁状态）都会增加系统开销。  
3. 在选择封锁粒度时，需要在**锁开销和并发程度**之间做一个权衡。

## 锁类型  

### 1. 读写锁  

1. 排它锁（Exclusive），简写为 X 锁，又称**写锁**。一个事务对数据对象 A 加了 X 锁，就可以对 A 进行读取和更新。加锁期间其它事务不能对 A 加任何锁。  
2. 共享锁（Shared），简写为 S 锁，又称**读锁**。  一个事务对数据对象 A 加了 S 锁，可以对 A 进行读取操作，但是不能进行更新操作。加锁期间其它事务能对 A加 S 锁，但是不能加 X 锁。  

### 2.意向锁  

使用意向锁（Intention Locks）  解决判断A表加锁需要检测每一行耗时问题。

1. 意向锁在原来的 X/S 锁之上引入了 IX/IS，**IX/IS 都是表锁**
2. 规定：
   * 获得S 锁之前，必须先获得表的 IS 锁或者更强的锁；
   * 获得 X 锁之前，必须先获得表的 IX 锁。  

### 3.封锁协议  

#### （1）三级封锁协议  

* **一级封锁协议**：解决丢失修改问题。事务 T 要修改数据 A 时必须加 X 锁，直到 T 结束才释放锁。  
* **二级封锁协议**：解决读脏数据问题。在一级基础上，读数据要加S锁，读完即刻释放（而不是事务结束）。因加入了X，所以就不会读入数据。
* **三级封锁协议**：解决不可重复读问题。在二级基础上，读取数据要加S锁，事务结束才释放。这样其他事务就不能加X锁，避免了在读期间数据发生变化。

#### （2）两段锁协议  

* MySQL 的 InnoDB 存储引擎采用两段锁协议，会根据隔离级别在需要的时候自动加锁，并且所有的锁都是在同一时刻被释放，这被称为隐式锁定  
* 显示锁

```
SELECT ... LOCK In SHAR
SELECT ... FOR UPDATE;  
```

## 四、隔离级别  

未提交读（READ UNCOMMITTED）  : 事务中的修改，未提交其他事务也可见。

提交读（READ COMMITTED）  : 解决脏读，修改未提交其他事务不可见。

可重复读（REPEATABLE READ）: **默认级别**，解决重复读，保证多个事务读取同样的数据结果一样。

可串行化（SERIALIZABLE）  :强制事务串行执行。

## 五、多版本并发控制  

* 多版本并发控制（Multi-Version Concurrency Control, MVCC）是 MySQL 的 InnoDB 存储引擎实现隔离级别的一种具体方式，用于实现**提交读和可重复读**这两种隔离级别。  
* 而**未提交读隔离级别**总是读取最新的数据行，无需使用MVCC。
* 可串行化隔离级别需要对所有读取的行都加锁，单纯使用 MVCC 无法实现。  

### 版本号

每开始一个新的事务，就会分配一个版本号（全部唯一，且是自增）

* 事务版本号：事务开始时的系统版本号。
* 系统版本号：是一个递增的数字，每开始一个新的事务，系统版本号就会自动递增。

### 隐藏的列

MVCC 在每行记录后面都保存着**两个隐藏的列**，用来存储两个版本号：

* 创建版本号：指示创建一个数据行的快照时的系统版本号；
* 删除版本号：如果该快照的删除版本号大于当前事务版本号表示该快照有效，否则表示该快照已经被删除了。  

### Undo 日志

MVCC 使用到的快照存储在 Undo 日志中，该日志通过**回滚指针**把一个数据行（Record）的所有快照连接起来。  

### 实现过程  

以下实现过程针对可重复读隔离级别。  

- select:读取时，读取快照的**创建版本号**必须小于当前事务的版本号。另外删除版本号必须大于本事务版本（或没有删除版本号）
- insert：本事务版本号作为快照的创建版本号
- update：本事务版本号作为**更新前**快照的删除版本号，作为**更新后数据行快照**的**创建版本号**。先删除后更新
- delete：本事务版本号作为快照的删除版本号

### Next-Key Locks 

Next-Key Locks 是 MySQL 的 InnoDB 存储引擎的一种锁实现。MVCC 不能解决幻影读问题，Next-Key Locks 就是为了解决这个问题而存在的。在可重复读（REPEATABLE READ）隔离级别下，使用 MVCC + Next-Key Locks 可以解决幻读问题。 

* Record Locks ：单个行记录上的锁。锁定一个记录上的索引，而不是记录本身。如果表没有设置索引，InnoDB 会自动在主键上创建隐藏的聚簇索引，因此 Record Locks 依然可以使用。   
* Gap Locks：间隙锁，**锁定一个范围，但不包括记录本身**。GAP锁的目的，是为了防止同一事务的两次当前读，出现幻读的情况。
* Next-Key Lock：1+2，**锁定一个范围，并且锁定记录本身**。对于行的查询，都是采用该方法，主要目的是解决幻读的问题。

## 快照读与当前读  

1. 快照读
   使用 MVCC 读取的是快照中的数据，这样可以减少加锁所带来的开销。  

```
select * from table ...;  
```

2. 当前读
   读取的是最新的数据，需要加锁。以下第一个语句需要加 S 锁，其它都需要加 X 锁。  

```
select * from table where ? lock in share mode;
select * from table where ? for update;
insert;
update;
delete;  
```

## 六、关系数据库设计理论  

### 函数依赖  

* 记 A->B 表示 A 函数**决定** B，也可以说 B 函数**依赖**于 A。  
* 部分依赖关系是指某个属性只由构成主键的部分列决定，而和另一些列无关。例如对关系：学生选课（学号，姓名，课程号，成绩），此关系的主键是（学号，课程号），而“姓名”列只由“学号”决定，与“课程号”无关，这就是部分依赖关系。
* 对于 A->B，B->C，则 **A->C** 是一个**传递函数依赖**。  

### 范式  

第一范式：属性不可再分。

第二范式：每个非主属性完全函数依赖于键码。可以通过分解满足。

第三范式：不能传递依赖

[数据库设计之三范式（通俗解释）](https://www.iteye.com/blog/lixh1986-2357536)

### 