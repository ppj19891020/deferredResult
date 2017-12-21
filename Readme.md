#spring mvc异步接口配置
###1、spring-mvc配置如下
````
<mvc:annotation-driven>
    <mvc:async-support default-timeout="5000"/>
</mvc:annotation-driven>
````
###2、web.xml的所有servlet和filter
````
<async-supported>true</async-supported>
````
###3、异步测试如下
####3.1 Callable
#####这种方法可以实现异步请求，但是没有设置超时时间和相关超时回调。只是客户端发起请求，服务端保证执行完成即可，然后将直接成功的结果返回给客户端，客户端根绝返回值判断是否需要发起另一个请求。
````
@RequestMapping("/callable")
  public @ResponseBody Callable<String> callable() {

    Callable<String> asyncTask = new Callable<String>() {
      @Override
      public String call() throws Exception {
        Thread.sleep(4000);
        System.out.println("等待4s");
        return "Callableresult";
      }
    };
    System.out.println("已交给服务线程处理");
    return asyncTask;
  }
````
####3.2 WebAsyncTask
#####这种方法通过使用spring的WebAsyncTask实现了异步请求，并且可以设置超时时间，以及超时和完成之后的回调函数。需要注意的是，超时之后也会回调onCompletion中设置方法。
````
Callable<String> asyncTask = new Callable<String>() {
    @Override
    public String call() throws Exception {
        Thread.sleep(10000);
        System.out.println("等待10s");
        return "Callableresult";//超时之后不会执行返回操作，但是return之前的能够执行完成
    }
};
      System.out.println("已交给服务线程处理");
      WebAsyncTask<String> webAsyncTask = new WebAsyncTask<String>(5000, asyncTask);
      webAsyncTask.onCompletion(new CompleteWork(webAsyncTask));
      webAsyncTask.onTimeout(timeOutCallBack());
      System.out.println("main over");
      return webAsyncTask;
  }
````
####3.3 DeferredResult
#####这种方法跟上面的WebAsyncTask类似，超时之后也会调用onCompletion函数。所以我们需要在回调函数中增加超时的判断。上面的方法中DeferredResult可以通过isSetOrExpired()来判断，但是WebAsyncTask还不知道如何判断。还有一点就是DeferredResult是在设置deferredResult.setResult(…)的时候就响应客户端，而WebAsyncTask是直接return。
#####
````
  
  @ResponseBody
  @RequestMapping("/defeeredResult")
  public DeferredResult<String>  defeeredResult(){
    //设置 5秒就会超时
    final DeferredResult<String> stringDeferredResult = new DeferredResult<String>(100000);

    //将请求加入到队列中
    deferredResults.add(stringDeferredResult);

    //setResult完毕之后，调用该方法
    stringDeferredResult.onCompletion(new Runnable() {
      @Override
      public void run() {
        System.out.println("异步调用完成");
        //响应完毕之后，将请求从队列中去除掉
        deferredResults.remove(stringDeferredResult);
      }
    });

    stringDeferredResult.onTimeout(new Runnable() {
      @Override
      public void run() {
        System.out.println("业务处理超时");
        stringDeferredResult.setResult("error:timeOut");
      }
    });
    return stringDeferredResult;
  }
  
  @ResponseBody
    @RequestMapping("/setResult")
    public String setResult(){
      for(int i = 0;i < deferredResults.size();i++){
        DeferredResult<String> deferredResult = deferredResults.getFirst();
        deferredResult.setResult("result:" + i);
      }
      return "OK";
    }

````