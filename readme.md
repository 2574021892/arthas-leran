---
title: arthas使用教程
date: 2023-03-16 10:32:43
tags:

---

# Dashboard

## 1.直接使用

​	dashboard命令可以查看当前系统的实时数据面板。

![img](https://arthas.aliyun.com/images/dashboard.png)

下面是参数代表的的意义

- ID: Java 级别的线程 ID，注意这个 ID 不能跟 jstack 中的 nativeID 一一对应。
- NAME: 线程名
- GROUP: 线程组名
- PRIORITY: 线程优先级, 1~10 之间的数字，越大表示优先级越高
- STATE: 线程的状态
- CPU%: 线程的 cpu 使用率。比如采样间隔 1000ms，某个线程的增量 cpu 时间为 100ms，则 cpu 使用率=100/1000=10%
- DELTA_TIME: 上次采样之后线程运行增量 CPU 时间，数据格式为`秒`
- TIME: 线程运行总 CPU 时间，数据格式为`分:秒`
- INTERRUPTED: 线程当前的中断位状态
- DAEMON: 是否是 daemon 线程

# Sysenv

## 1.查看所有的环境变量

直接使用sysenv的话可以查看到当前jvm运行时系统的所有环境变量

```
[arthas@23572]$ sysenv
 KEY                                           VALUE
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
 USERDOMAIN_ROAMINGPROFILE                     LAPTOP-QSUR4HJI
 NO_PROXY                                      *                                                                                                                                                                                       
 PROCESSOR_LEVEL                               25
 SESSIONNAME                                   Console                                                                                                                                                                                 
 ALLUSERSPROFILE                               C:\ProgramData
 PROCESSOR_ARCHITECTURE                        AMD64
 GATEWAY_VM_OPTIONS                            C:\Users\wh\Desktop\jetbra\jetbra\vmoptions\gateway.vmoptions                                                                                                                           
 ANDROID_HOME                                  D:\dev\Android-sdk
 PSModulePath                                  C:\Program Files\WindowsPowerShell\Modules;C:\WINDOWS\system32\WindowsPowerShell\v1.0\Modules                                                                                           
 SystemDrive                                   C:
 JRE_HOME                                      D:\dev\java\jre1.8.0_321
 MAVEN_HOME                                    D:\dev\maven                                                                                                                                                                            
 RIDER_VM_OPTIONS                              C:\Users\wh\Desktop\jetbra\jetbra\vmoptions
                                               ider.vmoptions                                                                                                                                                                          
 JAVA_HOME_11                                  D:\dev\java\jdk-11.0.14

```

## 2.查看指定变量

直接使用sysenv展示的信息可能过多 可通过指定变量名直接查看变量值，这边我指定了JAVA_HOME可直接查看到JAVA_HOME的值，查看指定环境变量时也支持TAB自动补全

```
[arthas@23572]$ sysenv JAVA_HOME
 KEY                                           VALUE
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 JAVA_HOME                                     D:\dev\java\jdk1.8.0_321
```



# Watch

## 1. 观察方法返回结果 returnObj

使用方式看着复杂，其实很简单。

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# body:
[
    {
        "id": 1,
        "name": "张三"
    },
    {
        "id": 2,
        "name": "李四"
    }
]
[arthas@22288]$ watch com.milk.arthaslearn.WatchController test returnObj
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 95 ms, listenerId: 1
method=com.milk.arthaslearn.WatchController.test location=AtExit
ts=2023-03-16 10:42:42; [cost=0.5567ms] result=@ArrayList[
    @User[User(id=1, name=张三)],
    @User[User(id=2, name=李四)],
]
```

上面的命令表示观察`com.milk.arthaslearn.WatchController`类中`test`方法的返回结果，并将其赋值给变量`returnObj`。然后在控制台输出`returnObj`的值。

## 2. 过滤不关心的调用 condition-express

显然，真实的案例肯定不会如上面的示例那么简单。我们可能需要根据一些条件来过滤出我们感兴趣的调用。比如，我们只想看到第二个参数为"Arthas"的调用：

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# body:
[
    {
        "id": 1,
        "name": "张三"
    },
    {
        "id": 2,
        "name": "李四"
    }
]
[arthas@22288]$ watch com.milk.arthaslearn.WatchController test {params,returnObj} 'params[0]=="Arthas"'
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 29 ms, listenerId: 3
method=com.milk.arthaslearn.WatchController.test location=AtExit
ts=2023-03-16 10:49:24; [cost=0.1341ms] result=@ArrayList[
    @Object[][isEmpty=false;size=2],
    @ArrayList[isEmpty=false;size=2],
]
[arthas@29420]$ watch com.milk.arthaslearn.WatchController test returnObj 'returnObj[0].id==1'
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 32 ms, listenerId: 19
method=com.milk.arthaslearn.WatchController.test location=AtExit
ts=2023-03-16 13:07:34; [cost=0.0311ms] result=@ArrayList[
    @User[User(id=1, name=张三)],
    @User[User(id=2, name=李四)],
]
```

上面的命令表示观察`com.milk.arthaslearn.WatchController`类中`test`方法的入参和返回结果，并将它们分别赋值给变量`params`和`returnObj`。然后在控制台输出一个数组，包含两个元素：第一个是入参数组，第二个是返回结果。最后，我们加了一个条件表达式：只有当第一个参数等于"Arthas"时才输出。

## 3. 同时观察入参和结果及控制层级

有时候，我们可能需要同时观察方法的入参和出参，以便对比分析。这时候，我们可以使用一个数组来包含多个变量，包括控制层级

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# body:
[
    {
        "id": 1,
        "name": "张三"
    },
    {
        "id": 2,
        "name": "李四"
    }
]
[arthas@22288]$ watch com.milk.arthaslearn.WatchController test {params,returnObj}
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 31 ms, listenerId: 6
method=com.milk.arthaslearn.WatchController.test location=AtExit
ts=2023-03-16 10:54:19; [cost=0.0322ms] result=@ArrayList[
    @Object[][isEmpty=false;size=2],
    @ArrayList[isEmpty=false;size=2],
]
# 
[arthas@22288]$ watch com.milk.arthaslearn.WatchController test {params,returnObj} -x 3
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 37 ms, listenerId: 7
method=com.milk.arthaslearn.WatchController.test location=AtExit
ts=2023-03-16 10:55:20; [cost=0.0389ms] result=@ArrayList[
    @Object[][
        @String[Arthas],
        @ArrayList[
            @User[User(id=1, name=张三)],
            @User[User(id=2, name=李四)],
        ],
    ],
    @ArrayList[
        @User[
            id=@Integer[1],
            name=@String[张三],
        ],
        @User[
            id=@Integer[2],
            name=@String[李四],
        ],
    ],
]
```

上面的命令表示观察`com.milk.arthaslearn.WatchController`类中`test`方法的入参和出参，并将它们分别赋值给变量`params`和`returnObj`。然后在控制台输出一个数组，包含两个元素：第一个是入参数组，第二个是返回结果。

## 4. 观察方法抛出的异常 throwExp

有时候，我们可能需要观察方法是否抛出了异常，以及异常的类型和信息。这时候，我们可以使用变量`throwExp`来获取异常对象：

```bash
# url: http://localhost:8080/watch/test?method=exception
[arthas@13760]$ watch com.milk.arthaslearn.WatchController test throwExp -e
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 78 ms, listenerId: 1
method=com.milk.arthaslearn.WatchController.test location=AtExceptionExit
ts=2023-03-16 11:03:41; [cost=0.8671ms] result=java.lang.RuntimeException: this way sir!
        at com.milk.arthaslearn.WatchController.test(WatchController.java:20)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)

```

上面的命令表示观察`com.milk.arthaslearn.WatchController`类中`test`方法是否抛出了异常，并将异常对象赋值给变量`throwExp`。然后在控制台输出异常对象的信息。

注意，这里使用了`-e`参数，表示只有当方法抛出异常时才触发watch命令。如果不加这个参数，那么无论方法是否抛出异常，都会触发watch命令。

这里使用了`-n 5`参数，表示只观察5次方法调用。如果不加这个参数，那么会一直观察方法调用，直到按下Q或Ctrl+C退出。

## 5. 观察执行时间超过指定时间的过滤

有时候我们观测方法执行效率时要根据返回时间来反向定位问题，可以使用表达式进行过滤：

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# url: http://localhost:8080/watch/test?method=cost
[arthas@29420]$ watch com.milk.arthaslearn.WatchController test '{params, returnObj}' '#cost>200' -x 2
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 31 ms, listenerId: 10
method=com.milk.arthaslearn.WatchController.test location=AtExit
ts=2023-03-16 11:21:01; [cost=3008.0319ms] result=@ArrayList[
    @Object[][
        @String[cost],
        @ArrayList[isEmpty=false;size=2],
    ],
    @ArrayList[
        @User[User(id=1, name=张三)],
        @User[User(id=2, name=李四)],
    ],
]
```

上面的命令表示观察`com.milk.arthaslearn.WatchController`类中`test` 方法时进行了条件过滤,只有当方法耗时超过200ms时才显示watch的信息，如果执行时间小于200ms时则不会显示watch信息

## 6. 观测静态变量变化

```bash
# url: http://localhost:8080/watch/test?method=target&name2=zhangsan
# url: http://localhost:8080/watch/test?method=target2&name2=zhangsan
[arthas@4060]$ watch com.milk.arthaslearn.StaticClass *  '{params,returnObj,throwExp,@com.milk.arthaslearn.StaticClass@name2}' -n 5  -x 3 
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 3) cost in 74 ms, listenerId: 3
method=com.milk.arthaslearn.StaticClass.changeName2 location=AtExit
ts=2023-03-16 13:27:06; [cost=1.0021ms] result=@ArrayList[
    @Object[][
        @String[zhangsan],
    ],
    null,
    null,
    @String[zhangsan],
]
method=com.milk.arthaslearn.StaticClass.changeName3 location=AtExit
ts=2023-03-16 13:27:11; [cost=0.0506ms] result=@ArrayList[
    @Object[][
        @String[zhangsan],
    ],
    null,
    null,
    @String[zhangsan],
]

```

上面的命令中观测了`StatticsClass`的`name2`属性的变动,结果时只有在name2变动的时候会记录对应的方法信息 这边我请求了两次 一次是changeName2 一次是changeName3

## 7. 观察方法执行的命令选项 options

除了上面介绍的变量和表达式之外，arthas watch命令还提供了一些选项来控制观察方法执行的行为和输出。下面简单介绍一些常用的选项：

- `-b`：表示观察方法执行的开始位置。
- `-e`：表示只观察方法执行出现异常的情况。
- `-s`：表示静默模式，不输出任何结果。
- `-x`：表示展开对象的层级，默认为1。
- `-n`：表示只观察指定次数的方法调用，默认为无限次。
- `-E`：表示开启正则表达式匹配类名和方法名，默认为通配符匹配。
- `-S`：表示观察指定行号或代码片段对应位置，默认为无效。
- `-c <value>`：表示限制匹配到指定数量（value）类之后停止搜索，默认为无限大。

更多选项和详细说明，请参考arthas官方文档：

https://arthas.aliyun.com/doc/watch.html

# Trace

## 1. trace追钟调用链路和方法耗时

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# url: http://localhost:8080/watch/test?method=cost
# body:
[
    {
        "id": 1,
        "name": "张三"
    },
    {
        "id": 2,
        "name": "李四"
    }
]
[arthas@4060]$ trace com.milk.arthaslearn.WatchController test
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 40 ms, listenerId: 8
`---ts=2023-03-16 13:37:17;thread_name=http-nio-8080-exec-3;id=24;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@5eb2172
    `---[0.0629ms] com.milk.arthaslearn.WatchController:test()


[arthas@4060]$ trace com.milk.arthaslearn.WatchController test
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 32 ms, listenerId: 9
`---ts=2023-03-16 13:38:31;thread_name=http-nio-8080-exec-7;id=28;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@5eb2172
    `---[3008.6547ms] com.milk.arthaslearn.WatchController:test()
        `---[100.00% 3008.5769ms ] com.milk.arthaslearn.WatchController:sleep3() #24
```

![image-20230316133914832](https://files.catbox.moe/4abaag.png)

可以发现在多级调用时，trace也会逐级显示调用链路并逐级统计调用时间，在命令行界面中，会将耗时较久的方法进行标红展示

## 2.trace追踪jdk方法

```bash
# url: http://localhost:8080/watch/test?method=traceJdk
# body:
[
    {
        "id": 1,
        "name": "张三"
    },
    {
        "id": 2,
        "name": "李四"
    }
]
[arthas@23572]$ trace com.milk.arthaslearn.WatchController test  -n 5 
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 37 ms, listenerId: 2
`---ts=2023-03-16 13:53:13;thread_name=http-nio-8080-exec-5;id=25;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@2eb917d0
    `---[0.1612ms] com.milk.arthaslearn.WatchController:test()
    
[arthas@23572]$ trace com.milk.arthaslearn.WatchController test  -n 5 --skipJDKMethod false 
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 77 ms, listenerId: 1
`---ts=2023-03-16 13:44:12;thread_name=http-nio-8080-exec-2;id=22;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@2eb917d0
    `---[0.6086ms] com.milk.arthaslearn.WatchController:test()
        +---[5.13% 0.0312ms ] java.lang.String:equals() #20
        +---[0.72% 0.0044ms ] java.lang.String:equals() #23
        +---[0.56% 0.0034ms ] java.lang.String:equals() #26
        +---[0.84% 0.0051ms ] java.lang.String:equals() #29
        +---[0.53% 0.0032ms ] java.lang.String:equals() #32
        +---[15.26% 0.0929ms ] java.lang.String:replace() #33
        `---[10.93% 0.0665ms ] java.io.PrintStream:println() #33


```

可以发现在多级调用时，trace也会逐级显示调用链路并逐级统计调用时间，在命令行界面中，会将耗时较久的方法进行标红展示

## 3. trace根据调用时间过滤

有时候我们观测方法执行效率时要根据返回时间来反向定位问题，可以使用表达式进行过滤：

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# url: http://localhost:8080/watch/test?method=cost
[arthas@29420]$ trace com.milk.arthaslearn.WatchController test '{params, returnObj}' '#cost>200' -x 2
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 31 ms, listenerId: 10
method=com.milk.arthaslearn.WatchController.test location=AtExit
ts=2023-03-16 11:21:01; [cost=3008.0319ms] result=@ArrayList[
    @Object[][
        @String[cost],
        @ArrayList[isEmpty=false;size=2],
    ],
    @ArrayList[
        @User[User(id=1, name=张三)],
        @User[User(id=2, name=李四)],
    ],
]
```

上面的命令表示观察`com.milk.arthaslearn.WatchController`类中`test` 方法时进行了条件过滤,只有当方法耗时超过200ms时才显示watch的信息，如果执行时间小于200ms时则不会显示watch信息

## 4. trace 不支持查看入参出参

```bash
# url: http://localhost:8080/watch/test?method=Arthas
[arthas@23572]$ trace -E com.milk.arthaslearn.WatchController test params 
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 44 ms, listenerId: 16

[arthas@23572]$ trace -E com.milk.arthaslearn.WatchController test
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 44 ms, listenerId: 17
`---ts=2023-03-16 14:14:34;thread_name=http-nio-8080-exec-2;id=22;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@2eb917d0
    `---[0.0373ms] com.milk.arthaslearn.WatchController:test()

[arthas@23572]$ trace -E com.milk.arthaslearn.WatchController test params -v
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 45 ms, listenerId: 18
Condition express: params , result: false
```

上面的命令中我们使用了params想查看入参 发现调用了接口并没有反应，然后我们将params去掉之后就能正确显示链路信息，这是因为trace本身不支持显示入参出参，通过增加-v查看详细信息后  我们可以看到我们的params被当成了一个condition表达式 结果是false，所以才没有结果

## 5.trace同时追踪多个方法

```bash
# url: http://localhost:8080/watch/test?method=cost
[arthas@23572]$ trace -E com.milk.arthaslearn.WatchController test|sleep3
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 2) cost in 42 ms, listenerId: 20
`---ts=2023-03-16 14:21:53;thread_name=http-nio-8080-exec-7;id=27;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@2eb917d0
    `---[3005.9088ms] com.milk.arthaslearn.WatchController:test()
        `---[100.00% 3005.8688ms ] com.milk.arthaslearn.WatchController:sleep3() #24
            `---[100.00% 3005.8481ms ] com.milk.arthaslearn.WatchController:sleep3()
                `---[100.00% 3005.7902ms ] cn.hutool.core.thread.ThreadUtil:sleep() #39

```

trace也支持同时追踪多个方法 这边我们同时追踪了test和sleep3，test中又调用了sleep3，所以结果中出现了两遍sleep3的结果

# Stack

stack和trace的不同之处在于stack主要时用来定位当前方法被调用的调用路径，可以帮助定位方法在哪边被调用了，trace则用于查看方法内部的调用路径，并输出各个节点上的耗时，主要用来解决排查执行效率问题

## 1. stack查看链路调用

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# url: http://localhost:8080/watch/test?method=cost
# body:
[
    {
        "id": 1,
        "name": "张三"
    },
    {
        "id": 2,
        "name": "李四"
    }
]
[arthas@23572]$ stack com.milk.arthaslearn.WatchController test  -n 5 
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 36 ms, listenerId: 22
ts=2023-03-16 14:52:43;thread_name=http-nio-8080-exec-3;id=23;is_daemon=true;priority=5;TCCL=org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader@2eb917d0
    @com.milk.arthaslearn.WatchController.test()
        at sun.reflect.GeneratedMethodAccessor8.invoke(null:-1)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:150)
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117)
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:895)
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808)
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1071)
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:964)
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)
        at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:898)
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:670)
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:779)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:227)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)

```

# Tt

`watch` 虽然很方便和灵活，但需要提前想清楚观察表达式的拼写，这对排查问题而言要求太高，因为很多时候我们并不清楚问题出自于何方，只能靠蛛丝马迹进行猜测。

这个时候如果能记录下当时方法调用的所有入参和返回值、抛出的异常会对整个问题的思考与判断非常有帮助。

于是乎，TimeTunnel 命令就诞生了。

## 1. tt指定类和方法记录一段现场

```bash
# url: http://localhost:8080/watch/test?method=Arthas
# url: http://localhost:8080/watch/test?method=Arthas
# url: http://localhost:8080/watch/test?method=Arthas
# url: http://localhost:8080/watch/test?method=cost
# url: http://localhost:8080/watch/test?method=Arthas
[arthas@23572]$ tt -t com.milk.arthaslearn.WatchController test -n 5 
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 35 ms, listenerId: 23
 INDEX         TIMESTAMP                           COST(ms)          IS-RET        IS-EXP         OBJECT                     CLASS                                                METHOD                                               
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
 1005          2023-03-16 15:18:24                 0.0381            true          false          0x639b0c1b                 WatchController                                      test                                                 
 1006          2023-03-16 15:18:25                 0.0138            true          false          0x639b0c1b                 WatchController                                      test                                                 
 1007          2023-03-16 15:18:27                 0.0235            true          false          0x639b0c1b                 WatchController                                      test                                                 
 1008          2023-03-16 15:18:31                 3006.7394         true          false          0x639b0c1b                 WatchController                                      test                                                 
 1009          2023-03-16 15:18:34                 0.1584            true          false          0x639b0c1b                 WatchController                                      test                                                 
Command execution times exceed limit: 5, so command will exit. You can set it with -n option.

```

上述命令中我们用tt命令监控了`test`方法 并增加-n 只记录5次

-n主要用于限制记录次数，在排查调用量特别大的请求时，可使用-n限制次数 否则jvm会瞬间被撑爆

## 2.tt查看调用记录

在上面我们使用tt记录了一段时间片段之后，我们可以从其中筛选出想要的时间片段，这时候我们还可以通过tt来对已经记录下来的调用记录进行检索，例如我们上面调用了4次正常时间的调用和一次3s的时间调用，这时候我们可以排查一下这个耗时3s的记录究竟时什么原因造成的

即使我们不记得调用记录的信息了 也可以使用`tt -l`来进行查询

```bash
[arthas@23572]$ tt -l
 INDEX         TIMESTAMP                           COST(ms)          IS-RET        IS-EXP         OBJECT                     CLASS                                                METHOD
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
 1000          2023-03-16 14:19:52                 0.0455            true          false          0x639b0c1b                 WatchController                                      test
 1001          2023-03-16 14:20:01                 0.0143            true          false          0x639b0c1b                 WatchController                                      test                                                 
 1002          2023-03-16 14:20:02                 0.0196            true          false          0x639b0c1b                 WatchController                                      test
 1003          2023-03-16 14:20:02                 0.0146            true          false          0x639b0c1b                 WatchController                                      test                                                 
 1004          2023-03-16 14:20:03                 0.0166            true          false          0x639b0c1b                 WatchController                                      test
 1005          2023-03-16 15:18:24                 0.0381            true          false          0x639b0c1b                 WatchController                                      test
 1006          2023-03-16 15:18:25                 0.0138            true          false          0x639b0c1b                 WatchController                                      test                                                 
 1007          2023-03-16 15:18:27                 0.0235            true          false          0x639b0c1b                 WatchController                                      test
 1008          2023-03-16 15:18:31                 3006.7394         true          false          0x639b0c1b                 WatchController                                      test
 1009          2023-03-16 15:18:34                 0.1584            true          false          0x639b0c1b                 WatchController                                      test                                                 
Affect(row-cnt:10) cost in 2 ms.
```

```
[arthas@23572]$ tt -i 1008
 INDEX          1008
 GMT-CREATE     2023-03-16 15:18:31
 COST(ms)       3006.7394                                                                                                                                                                                                              
 OBJECT         0x639b0c1b
 CLASS          com.milk.arthaslearn.WatchController
 METHOD         test
 IS-RETURN      true
 IS-EXCEPTION   false                                                                                                                                                                                                                  
 PARAMETERS[0]  @String[cost]
 PARAMETERS[1]  null
 PARAMETERS[2]  @ArrayList[                                                                                                                                                                                                            
                    @User[User(id=1, name=张三)],                                                                                                                                                                                       

                    @User[User(id=2, name=李四)],                                                                                                                                                                                       

                ]                                                                                                                                                                                                                      
 RETURN-OBJ     @ArrayList[
                    @User[User(id=1, name=张三)],                                                                                                                                                                                       

                    @User[User(id=2, name=李四)],                                                                                                                                                                                       

                ]
Affect(row-cnt:1) cost in 1 ms.
```

可以看到，我们通过`tt -i 记录号`的方式查询除了执行了3s的那一次调用 并记录了具体的参数和返回的信息

## 3. tt调用重试

当我们经过排查解决了某些方面 例如数据库之类的问题，可能需要重新调用一次，这时我们可能需要去网页上点击一次或是通过postman之类的重新发起一次请求，但这些操作 通常都比较费时间麻烦，而tt命令因为保存了调用时的所有信息，所以我们可以自己主动对一个 `INDEX` 编号的时间片自主发起一次调用，从而解放你的沟通成本。此时你需要 `-p` 参数。通过 `--replay-times` 指定 调用次数，通过 `--replay-interval` 指定多次调用间隔(单位 ms, 默认 1000ms)

```bash
[arthas@23572]$ tt -i 1008 -p
 RE-INDEX       1008                                                                                                                                                                                                                   
 GMT-REPLAY     2023-03-16 15:30:29                                                                                                                                                                                                    
 OBJECT         0x639b0c1b
 CLASS          com.milk.arthaslearn.WatchController
 METHOD         test                                                                                                                                                                                                                   
 PARAMETERS[0]  @String[cost]
 PARAMETERS[1]  null
 PARAMETERS[2]  @ArrayList[                                                                                                                                                                                                            
                    @User[User(id=1, name=张三)],                                                                                                                                                                                       

                    @User[User(id=2, name=李四)],                                                                                                                                                                                       

                ]                                                                                                                                                                                                                      
 IS-RETURN      true
 IS-EXCEPTION   false
 COST(ms)       3001.3318                                                                                                                                                                                                              
 RETURN-OBJ     @ArrayList[
                    @User[User(id=1, name=张三)],                                                                                                                                                                                       

                    @User[User(id=2, name=李四)],                                                                                                                                                                                       

                ]
Time fragment[1008] successfully replayed 1 times.
```

上述命令中我们用tt命令监控了`test`方法 并增加-n 只记录5次

-n主要用于限制记录次数，在排查调用量特别大的请求时，可使用-n限制次数 否则jvm会瞬间被撑爆

# Thread



## 1. 排查死锁

```bash
# http://localhost:8080/watch/test?method=deadLock
[arthas@18276]$ thread -b
No most blocking thread found!
[arthas@18276]$ thread -b
"test-thread2" Id=65 BLOCKED on java.lang.Object@1c537abc owned by "test-thread1" Id=64
    at com.milk.arthaslearn.WatchController$2.run(WatchController.java:76)
    -  blocked on java.lang.Object@1c537abc
    -  locked java.lang.Object@17fb27d <---- but blocks 1 other threads!
    at java.lang.Thread.run(Thread.java:750)

[

```

在我们执行deadLock方法前thread -b排查不到死锁 在执行deadLock方法之后thread成功将死锁信息展示了出来并且告知是哪一行发生了死锁

# Ognl示例

## 1.查看第一个参数

> ```
> watch com.taobao.container.Test test "params[0]"
> ```

## 2.查看第一个参数size

> ```
> watch com.taobao.container.Test test "params[0].size()"
> ```

## 3.投影参数

> ```
> 获取result第一个元素的name
> watch com.milk.arthaslearn.WatchController test '{params,returnObj[0].{ #this.name },throwExp}'  -n 5  -x 3  -v
> {#this.name}指的就是returnObj[0] 对其做的表达式
> 获取result每一个元素的name
> watch com.milk.arthaslearn.WatchController test '{params,returnObj.{ #this.name },throwExp}'  -n 5  -x 3  -v
> ```

## 4.ognl执行静态方法

> ```
> [arthas@18276]$ ognl '@java.lang.Thread@currentThread()'
> @Thread[
>     name=@String[arthas-command-execute],
>     priority=@Integer[5],
>     threadQ=null,
>     .......
> ```

## 5.ognl执行静态方法后再执行非静态方法

> ```
> [arthas@18276]$ ognl '@java.lang.Thread@currentThread().getContextClassLoader()'
> @AppClassLoader[
>     ucp=@URLClassPath[sun.misc.URLClassPath@2aab607a],
>     $assertionsDisabled=@Boolean[true],
>     ucp=@URLClassPath[sun.misc.URLClassPath@2aab607a],
>     acc=@AccessControlContext[java.security.AccessControlContext@0],
>     closeables=@WeakHashMap[isEmpty=false;size=9],
>     initialized=@Boolean[true],
> ```

## 6.通过SpringUtil动态获取bean执行方法

> ```
>  ognl '@cn.hutool.extra.spring.SpringUtil@getBean("watchController").log()'
> ```

## 7.获取静态变量

> [arthas@28964]$ ognl '@com.milk.arthaslearn.StaticClass@name2'
> @String[Bieber]
> [arthas@28964]$ ognl '@com.milk.arthaslearn.StaticClass@name2'
> @String[zhangsan]

# 其他

还有一些其他的使用频率比较低，使用也不是很复杂的命令，这边就简单介绍一下

## 1.hisory

> 历史指令会通过一个名叫 history 的文件持久化，所以 history 指令可以查看当前 arthas 服务器的所有历史命令，而不仅只是当前次会话使用过的命令。

## 2.reset

> arthas的原理其实上是对类做了增强实现以上的功能，所以我们可以通过reset将被 Arthas 增强过的类全部还原，Arthas 服务端`stop`时也会自动重置所有增强过的类

## 3.sc

> sc命令可以查看查看 JVM 已加载的类信息
>
> [arthas@28964]$ sc com.milk.*
> com.milk.arthaslearn.ArthasLearnApplication
> com.milk.arthaslearn.ArthasLearnApplication$$EnhancerBySpringCGLIB$$af253de5
> com.milk.arthaslearn.WatchController
> Affect(row-cnt:3) cost in 7 ms.
> [arthas@28964]$ sc cn.hutool.*
> cn.hutool.core.exceptions.UtilException
> cn.hutool.core.lang.TypeReference
> cn.hutool.extra.spring.SpringUtil
> Affect(row-cnt:3) cost in 8 ms.

# Docker中使用arthas

由于arthas是诊断当前系统中的java进行，所以如果是用docker启动的java进程，那么直接在服务器中使用arthas是检测不到java进程的

## 1.诊断docker中的java进程

我们可以通过exec 进入docker容器并下载arthas的jar包启动并执行

```bash
docker exec -it  ${containerId} /bin/bash -c "wget https://arthas.aliyun.com/arthas-boot.jar && java -jar arthas-boot.jar"
```

## 2.把arthas放到基础镜像中

如果不想每次进入容器都下载一遍arthas，也可以通过dockerfile将arthas安装进docker镜像中

```bash
# copy arthas
COPY --from=hengyunabc/arthas:latest /opt/arthas /opt/arthas
```