### 数据库 MySQL


#### MyBatis

##### 原理

1. Mybatis运行为两大部分
(1)读取配置文件缓存到Configuration对象，用以创建SqlSessionFactory
(2)SqlSession执行过程

###### 运行过程

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



Mapper是个接口，在运行时，查看发现Mybatis为我们创建代理类，




###### 补充知识-CGLIB
1. CGLIB是一个强大的高性能的代码生成包。它广泛的被许多AOP的框架使用，例如Spring AOP为他们提供
方法的interception（拦截）。
2. CGLIB包的底层是通过使用一个小而快的字节码处理框架ASM，来转换字节码并生成新的类。
3. 除了CGLIB包，脚本语言例如Groovy和BeanShell，也是使用ASM来生成java的字节码。
4. 当然不鼓励直接使用ASM，因为它要求你必须对JVM内部结构包括class文件的格式和指令集都很熟悉。
###### 补充知识-JDK动态代理
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




