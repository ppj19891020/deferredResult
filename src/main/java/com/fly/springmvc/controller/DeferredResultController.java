package com.fly.springmvc.controller;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

/**
 * 描述:
 *  spring mvc 请求异步化
 * @author pangpeijie
 * @create 2017-12-21 20:22
 */
@Controller
public class DeferredResultController {

  private ConcurrentLinkedDeque<DeferredResult<String>> deferredResults = new ConcurrentLinkedDeque<DeferredResult<String>>();

  private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();


  /**
   * Callable
   * @return
   */
  @RequestMapping("/callable")
  public @ResponseBody Callable<String> callable() {
    Callable<String> asyncTask = () -> {
      Thread.sleep(4000);
      System.out.println("等待4s");
      return "Callableresult";
    };
    System.out.println("已交给服务线程处理");
    return asyncTask;
  }

  /**
   * WebAsyncTask
   * @return
   */
  @RequestMapping("/WebAsyncTask")
  public @ResponseBody WebAsyncTask<String> webAsyncHandle() {
    Callable<String> asyncTask = () -> {
      Thread.sleep(10000);
      System.out.println("等待10s");
      return "Callableresult";//超时之后不会执行返回操作，但是return之前的能够执行完成
    };
    System.out.println("已交给服务线程处理");
    WebAsyncTask<String> webAsyncTask = new WebAsyncTask<String>(50000, asyncTask);
    webAsyncTask.onCompletion(()->{
      System.out.println("WebAsyncTask:我执行完啦!");
    });
    webAsyncTask.onTimeout(()->{
      System.out.println("WebAsyncTask:超时");
      return "我超时了";
    });
    return webAsyncTask;
  }

  /**
   * DeferredResult
   * @return
   */
  @ResponseBody
  @RequestMapping("/defeeredResult")
  public DeferredResult<String>  defeeredResult() throws Exception{
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

    Thread.sleep(5000L);
    cachedThreadPool.execute(()->{
      LocalDateTime now = LocalDateTime.now();
      stringDeferredResult.setErrorResult("success"+ now);
      System.out.println("设置值:"+now);
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

}
