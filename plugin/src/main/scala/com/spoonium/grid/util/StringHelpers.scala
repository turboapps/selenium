package com.spoonium.grid.util

import java.security.SecureRandom
import java.lang.{StringBuilder => GoodSB}


// borrowed from LiftWeb
object StringHelpers {

  private val _random = new SecureRandom

  /**
   * Create a random string of a given size.  5 bits of randomness per character
   * @param size size of the string to create. Must be a positive integer.
   * @return the generated string
   */
  def randomString(size: Int): String = {
    def addChar(pos: Int, lastRand: Int, sb: GoodSB): GoodSB = {
      if (pos >= size) sb
      else {
        val randNum = if ((pos % 6) == 0) {
          _random.synchronized(_random.nextInt)
        } else {
          lastRand
        }

        sb.append((randNum & 0x1f) match {
          case n if n < 26 => ('A' + n).toChar
          case n => ('0' + (n - 26)).toChar
        })
        addChar(pos + 1, randNum >> 5, sb)
      }
    }
    addChar(0, 0, new GoodSB(size)).toString
  }
}
