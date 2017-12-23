package com.fly.deferred.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 描述:
 * 队列事件监听器
 * @author pangpeijie
 * @create 2017-12-23 18:04
 */
@Component
public class QueueListener implements ApplicationListener<ContextRefreshedEvent>{

  @Autowired
  private Queue queue;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    System.out.println("queue监听器开启");
    new Thread(()->{
      while (true){
        try {
          Task<String> task = queue.get();
          task.getResult().setResult(task.getMessage());
          System.out.println("任务处理完成");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

}
