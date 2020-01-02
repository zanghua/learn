# MyBatis

## 原理

1. Mybatis运行为两大部分
   (1)读取配置文件缓存到Configuration对象，用以创建SqlSessionFactory
   (2)SqlSession执行过程

运行过程

1. 构建SqlSessionFactory/Configuration
   (1)主要通过SqlSessionFacotryBuild去构建
   (2)XMLConifgBuilder解析xml,并将读取解析的数据保存到(构建)Configuration类,
   (3)注意几乎所有配置都存到这里了.Mapping映射器
   (4)使用Configuration去创建SqlSessionFactory(默认实现DefaultSqlSessionFactory)
   (5)Mapper映射器组成:MappedStatement/SqlSource/BoundSql
   |- MappedStatement:保存一个节点(select|insert...)
   |- SqlSource:是提供BoundSql地方,是MappedStatement的属性
   |- BoundSql:它是建立 Sql和参数的地方.
2. SqlSession运行过程
   (1)旧版本中使用SqlSession，在新版建议使用Mapper
   |- Mapper映射是通过动态代理实现的，MapperProxyFactory,最后MapperMethod.execute(SqlSession)
   (2)SqlSession下的四大对象,通过类名与方法名就能匹配到SQL
   |- Executor:代表执行器，它来调度下面三个Handler
   |- StatementHandler的作用是使用数据库statement执行，它是核心，起承上启下作用。
   |- ParameterHandler用于对SQL参数的处理
   |- resultHandler对数据集ResultSet的封装处理

## Mybatis插件

Mybatis插件又称拦截器。

Mybatis采用责任链模式，通过动态代理组织多个插件（拦截器），通过这些插件可以改变Mybatis的默认行为（诸如SQL重写之类的），由于插件会深入到Mybatis的核心，因此在编写自己的插件前最好了解下它的原理，以便写出安全高效的插件。

MyBatis 允许你在已映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：

- Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

总体概括为：

- 拦截执行器的方法
- 拦截参数的处理
- 拦截结果集的处理
- 拦截Sql语法构建的处理

## Mybatis四大接口

竟然Mybatis是对四大接口进行拦截的，那我们要先要知道Mybatis的四大接口对象 Executor, StatementHandler, ResultSetHandler, ParameterHandler。

![](D:/workspace/learn/0-0/img/03.png)

上图Mybatis框架的整个执行过程。Mybatis插件能够对这四大对象进行拦截，可以包含到了Mybatis一次查询的所有操作。可见Mybatis的的插件很强大。

1. Executor是 Mybatis的内部执行器，它负责调用StatementHandler操作数据库，并把结果集通过 ResultSetHandler进行自动映射，另外，他还处理了二级缓存的操作。从这里可以看出，我们也是可以通过插件来实现自定义的二级缓存的。
2. StatementHandler是Mybatis直接和数据库执行sql脚本的对象。另外它也实现了Mybatis的一级缓存。这里，我们可以使用插件来实现对一级缓存的操作(禁用等等)。
3. ParameterHandler是Mybatis实现Sql入参设置的对象。插件可以改变我们Sql的参数默认设置。
4. ResultSetHandler是Mybatis把ResultSet集合映射成POJO的接口对象。我们可以定义插件对Mybatis的结果集自动映射进行修改。

## 插件Interceptor

Mybatis的插件实现要实现Interceptor接口，我们看下这个接口定义的方法。

```java
public interface Interceptor {   
   //插件运行的代码，它将代替原有的方法
   Object intercept(Invocation invocation) throws Throwable;     
   // 拦截四大接口  
   Object plugin(Object target);    
   // 配置自定义相关属性
   void setProperties(Properties properties);
}
```

理解这个接口的定义，先要知道java动态代理机制。plugin接口返回参数target对象（Executor/ParameterHandler/ResultSetHander/StatementHandler）的代理对象。在调用对应对象的接口的时候，可以进行拦截并处理。

## Mybatis四大接口对象创建方法

```csharp
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
   //确保ExecutorType不为空(defaultExecutorType有可能为空)
   executorType = executorType == null ? defaultExecutorType : executorType;
   executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
   Executor executor;   if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
   } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
   } else {
      executor = new SimpleExecutor(this, transaction);
   }   if (cacheEnabled) {
      executor = new CachingExecutor(executor);
   }
   executor = (Executor) interceptorChain.pluginAll(executor);
   return executor;
}

public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
   StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
   statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
   return statementHandler;
}

public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
   ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
   parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
   return parameterHandler;
}

public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql) {
   ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
   resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
   return resultSetHandler;
}
```

查看源码可以发现， Mybatis框架在创建好这四大接口对象的实例后，都会调用**InterceptorChain.pluginAll()**方法。**InterceptorChain**对象是插件执行链对象，看源码就知道里面维护了Mybatis配置的所有插件(Interceptor)对象。



```tsx
// target  --> Executor/ParameterHandler/ResultSetHander/StatementHandler
public Object pluginAll(Object target) {
   for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
   }
   return target;
}
```

其实就是按顺序执行我们插件的plugin方法，一层一层返回我们原对象(Executor/ParameterHandler/ResultSetHander/StatementHandler)的代理对象。

## 插件实现

下面的MyBatis官网的一个拦截器实例：

```tsx
@Intercepts({@Signature(type = Executor.class, method = "update",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class TestInterceptor implements Interceptor {
   public Object intercept(Invocation invocation) throws Throwable {
     Object target = invocation.getTarget(); //被代理对象
     Method method = invocation.getMethod(); //代理方法
     Object[] args = invocation.getArgs(); //方法参数
     // do something ...... 方法拦截前执行代码块
     Object result = invocation.proceed();
     // do something .......方法拦截后执行代码块
     return result;
   }
   public Object plugin(Object target) {
     return Plugin.wrap(target, this);
   }
  public void setProperties(Properties properties) {
  }
}
```

Plugin.warp方法会返回四大接口对象的代理对象，会拦截所有的执行方法。这个拦截器拦截Executor接口的update方法（其实也就是SqlSession的新增，删除，修改操作），所有执行executor的update方法都会被该拦截器拦截到。

Mybatis中利用了注解的方式配置指定拦截哪些方法，只有通过Intercepts注解指定的方法才会执行我们自定义插件的intercept方法。

下面给出一个拦截的实例。

```tsx
//拦截StatementHandler中参数类型为Connection的prepare方法
@Intercepts({@Signature(type=StatementHandler.class,method="prepare",args={Connection.class})})
public class PageInterceptor implements Interceptor {

    private String test; // 获取xml中配置的属性

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
        //通过MetaObject优雅访问对象的属性，这里是访问statementHandler的属性
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        //先拦截到RoutingStatementHandler，里面有个StatementHandler类型的delegate变量，其实现类是BaseStatementHandler
        // 然后获取到BaseStatementHandler的成员变量mappedStatement
        MappedStatement mappedStatement = (MappedStatement)metaObject.getValue("delegate.mappedStatement");
        // 配置文件中SQL语句的ID
        String id = mappedStatement.getId();
        if(id.matches(".+ByPage$")) { //需要拦截的ID(正则匹配)
            BoundSql boundSql = statementHandler.getBoundSql();
            // 原始的SQL语句
            String sql = boundSql.getSql();
            // 此处省略一系列改造代码
            String pageSql = sql + " limit " + page.getDbIndex() + "," + page.getDbNumber();
            metaObject.setValue("delegate.boundSql.sql", pageSql);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        // 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身,减少目标被代理的次数
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {
        this.test = properties.getProperty("test");
    }
}
```

MetaObject是Mybatis提供的一个用于方便、优雅访问对象属性的对象，通过它可以简化代码、不需要try/catch各种reflect异常，同时它支持对JavaBean、Collection、Map三种类型对象的操作。

MetaObject 的构造器是私有的，获取MetaObject对象需要使用静态方法MetaObject.forObject，并且需要指定ObjectFactory，ObjectWrapperFactory，ReflectorFactory(3.3.0之前不需要)。


链接：https://www.jianshu.com/p/f704c9ae600e


Mapper是个接口，在运行时，查看发现Mybatis为我们创建代理类，




## 补充知识-CGLIB

1. CGLIB是一个强大的高性能的代码生成包。它广泛的被许多AOP的框架使用，例如Spring AOP为他们提供
   方法的interception（拦截）。
2. CGLIB包的底层是通过使用一个小而快的字节码处理框架ASM，来转换字节码并生成新的类。
3. 除了CGLIB包，脚本语言例如Groovy和BeanShell，也是使用ASM来生成java的字节码。
4. 当然不鼓励直接使用ASM，因为它要求你必须对JVM内部结构包括class文件的格式和指令集都很熟悉。

## 补充知识-JDK动态代理

1. JDK动态代理是由java.lang.reflect.*包提供的支持。
   (1)编写服务类和接口，这是真正的服务提供者，在JDK代理中接口是必须的
   (2)编写代理类提供绑定和方法，必须实现InvocationHandler接口

```
public class MyInvocationHandler implements InvocationHandler {

    /** 目标对象 */
    private Object target;
    public MyInvocationHandler(Object target){
        this.target = target;
    }
    @Override //实现此方法
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("------插入前置通知代码-------------");
        // 执行相应的目标方法
        Object rs = method.invoke(target,args);
        System.out.println("------插入后置处理代码-------------");
        return rs;
    }
}

```

(3)获取代理类并执行，

```
HelloService helloService = (HelloService) Proxy.newProxyInstance(HelloProxy.class.getClassLoader(),
                new Class[]{HelloService.class},//目标类接口
                new HelloProxy(new HelloServiceImpl()));//代理类
helloService.sayHello(); //代理类执行
```



