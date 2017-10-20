package com.spoonium.grid.util

import java.net._
import com.google.common.io.{Closeables, ByteStreams}
import com.google.common.base.{Charsets}

object HttpUtil extends Loggable {

  CookieHandler.setDefault(new CookieManager())

  def get(url: String, cookies: Map[String, String] = Map()) = try {
    val connection = new URL(url).openConnection()
    connection.setRequestProperty("Accept-Charset", "UTF-8")

    connection.setRequestProperty("Cookie", cookies.map({case (k,v) => new HttpCookie(k, v).toString }).mkString(";"))

    val response = connection.getInputStream // makes the call
    try {
      new String(ByteStreams.toByteArray(response), Charsets.UTF_8)
    } finally {
      Closeables.close(response, true)
    }
  }

  def getJson(url: String, cookies: Map[String, String]) = try {
    val connection = new URL(url).openConnection()
    connection.setRequestProperty("Accept-Charset", "UTF-8")
    connection.setRequestProperty("Content-Type", "application/json")

    connection.setRequestProperty("Cookie", cookies.map({case (k,v) => new HttpCookie(k, v).toString }).mkString(";"))

    val response = connection.getInputStream // makes the call
    try {
      new String(ByteStreams.toByteArray(response), Charsets.UTF_8)
    } finally {
      Closeables.close(response, true)
    }
  }
  
  // post data as body to specified url
  def post(url: String, data: String, contentType: String = "application/x-www-form-urlencoded;charset=UTF-8"): Boolean = try {
    val connection = new URL(url).openConnection()
    connection.setDoOutput(true) // Triggers POST.
    connection.setRequestProperty("Accept-Charset", "UTF-8")
    connection.setRequestProperty("Content-Type", contentType);

    connection.getOutputStream.write(data.getBytes("UTF-8"))

    val response = connection.getInputStream // makes the call
    val status = connection.asInstanceOf[HttpURLConnection].getResponseCode
    val ok = status / 100 == 2
    if(!ok) logger.severe(s"Got NOK http status $status for POST to $url")
    
    ok
  } catch {
    case e: Exception => {
      loggerSevere(s"POST failed to $url", e)
      false
    }
  }
} 