package com.spoonium.grid.web

import org.springframework.mock.web.MockHttpServletResponse

class HackedHttpServletResponse extends MockHttpServletResponse {

  override def setIntHeader(name: String, value: Int) {
    setHeader(name, value.toString)
  }
}
