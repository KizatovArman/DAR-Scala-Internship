package Week2

sealed trait Result[T]
case class Success[T](result: T) extends Result[T]
case class Failure[T](reason: String) extends Result[T]

sealed trait GenericList[T]{
  def apply(index: Int): Result[T] = this match {
    case GenericPair(head, tail) =>
      if(index == 0) Success(head)
      else tail.apply(index - 1)
    case GenericEnd() => Failure[T]("Index out of bounds")
  }

  def length: Int = {
    this match {
      case GenericPair(head, tail) => 1 + tail.length
      case GenericEnd() => 0
    }
  }

  def contains(el: T): Boolean = {
    this match {
      case GenericPair(head, tail) =>
        if(head == el) true
        else tail.contains(el)
      case GenericEnd() => false
    }
  }
}
case class GenericPair[T](head: T, tail: GenericList[T]) extends GenericList[T]
case class GenericEnd[T]() extends GenericList[T]

