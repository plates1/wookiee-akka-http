package com.webtrends.harness.component.akkahttp.logging

import akka.http.scaladsl.model.{DateTime, HttpRequest, StatusCode}
import com.webtrends.harness.command.{BaseCommand, CommandBean}
import com.webtrends.harness.component.akkahttp.AkkaHttpBase.TimeOfRequest
import com.webtrends.harness.logging.Logger

trait AccessLog  {
  this: BaseCommand =>

  val accessLog = Logger("AccessLog")
  def logAccess(request: HttpRequest, bean: CommandBean, statusCode: Option[StatusCode] = None) {

    // modify the logback.xml file to write the "AccessLog" entries to a file without all of the prefix information
    //TODO add a config with an option to turn off logging
    try {
      val host = request.headers.find(h=>h.is("host")).map(h=> h.value).getOrElse("-")
      // the userId may be either a String or an Int
      val user: String = bean.get("userId").map(id => id.toString).getOrElse("-")
      val status:String = statusCode.map(sc => sc.intValue.toString).getOrElse("-")
      val responseTimestamp: Long = System.currentTimeMillis()
      val requestTimestamp: Long = bean.getValue[Long](TimeOfRequest).getOrElse(responseTimestamp)
      val elapsedTime: Long = responseTimestamp - requestTimestamp
      val now: String = DateTime(requestTimestamp).toIsoDateTimeString()
      /*
          LogFormat "%v %l %u %t \"%r\" %>s %b %{ms}T"

          %v – The IP address of the server.
          %l – The identity of the client determined by identd on the client’s machine. Will return a hyphen (-) if this information is not available.
          %u – The userid of the client if the request was authenticated.
          %t – The time that the request was received, in UTC
          \"%r\" – The request line that includes the HTTP method used, the requested resource path, and the HTTP protocol that the client used.
          %>s – The status code that the server sends back to the client.
          %b – The size of the object requested. Will return a hyphen (-) if this information is not available.
          %{ms}T - The time taken to serve the request, in milliseconds

          see https://httpd.apache.org/docs/2.4/logs.html
      */
      accessLog.info( s"""$host - $user [$now] "${request.method.value} ${request.uri} ${request.protocol.value}" $status - $elapsedTime""")
    } catch {
      case e: Exception =>
        log.error("Could not construct access log", e)
    }
  }

}
