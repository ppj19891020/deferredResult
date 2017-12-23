package com.fly.deferred.core;

import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

/**
 * 描述:
 *  队列处理
 * @author pangpeijie
 * @create 2017-12-23 17:32
 */
@Component
public class Queue {

  /**
   * 接受任务消息
   */
  private LinkedBlockingQueue<Task<String>> receiveQueue = new LinkedBlockingQueue<>();

  /**
   * 完成任务
   */
  private LinkedBlockingQueue<Task<String>> completeQueue = new LinkedBlockingQueue<>();

  public Queue(){
    this.execute();
  }

  /**
   * put 任务
   * @param task
   * @throws InterruptedException
   */
  public void put(Task<String> task) throws InterruptedException {
    receiveQueue.put(task);
  }

  /**
   * 获取完成任务
   * @return
   * @throws InterruptedException
   */
  public Task<String> get() throws InterruptedException {
    return completeQueue.take();
  }

  /**
   * 处理任务
   */
  private void execute(){
    System.out.println("开始处理任务");
    new Thread(()->{
      while(true){
        try {
          Task<String> task = receiveQueue.take();
          System.out.println("队列收到消息,处理中...");
          //模拟任务处理延时
          Thread.sleep(2000L);
          task.setMessage("ok");
          if(task.getTimeout()){
            System.out.println("任务超时，跳过该任务");
            continue;
          }
          completeQueue.put(task);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

}
