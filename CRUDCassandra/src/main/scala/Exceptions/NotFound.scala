package Exceptions

case class NotFound(private val message: String = "") extends Exception(message)