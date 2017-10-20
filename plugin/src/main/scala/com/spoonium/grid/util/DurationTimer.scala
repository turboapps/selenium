package com.spoonium.grid.util

import com.google.common.cache.{CacheLoader, CacheBuilder}
import java.util.concurrent.TimeUnit
import java.lang.{Long => JLong}

object DurationTimer {

  lazy val cache = CacheBuilder.newBuilder()
    .expireAfterAccess(1, TimeUnit.HOURS)
    .build[String, JLong](
      new CacheLoader[String, JLong]() {
        override def load(key: String): JLong = {
          JLong.valueOf(System.currentTimeMillis())
        }
      })

  def started(key: String): Boolean = cache.getIfPresent(key) != null
  
  def start(key: String) {
    cache.get(key)
  } 
  
  def time(key: String): Option[Long] = {
    if(started(key)) {
      val now = System.currentTimeMillis()
      val start = cache.get(key)
      Some(now - start)
    } else {
      None
    }
  }

  def stop(key: String): Option[Long] = {
    val result = time(key)
    cache.invalidate(key)
    result
  }
}
