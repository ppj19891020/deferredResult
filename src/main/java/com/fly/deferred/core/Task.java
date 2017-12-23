package com.fly.deferred.core;

import org.springframework.web.context.request.async.DeferredResult;

/**
 * 描述:
 *  保存消息内容和返回对象
 * @author pangpeijie
 * @create 2017-12-23 17:27
 */
public class Task<T> {

  public Task(DeferredResult<String> result, T message) {
    this.result = result;
    this.message = message;
  }

  /**
   * 延时返回对象
   */
  private DeferredResult<String> result;

  /**
   * 任务消息
   */
  private T message;

  /**
   * 是否超时
   */
  private Boolean isTimeout = false;

  public DeferredResult<String> getResult() {
    return result;
  }

  public void setResult(
      DeferredResult<String> result) {
    this.result = result;
  }

  public T getMessage() {
    return message;
  }

  public void setMessage(T message) {
    this.message = message;
  }

  public Boolean getTimeout() {
    return isTimeout;
  }

  public void setTimeout(Boolean timeout) {
    isTimeout = timeout;
  }
}
