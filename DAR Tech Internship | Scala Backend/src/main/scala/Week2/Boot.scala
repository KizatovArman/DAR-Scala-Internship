package Week2

object Boot extends App {
  println("Hello World!\n")
  var oper = 0
  var pos = 0

  def compute(x: Int, y: Int): Int = {
    oper match {
      case 1 => x + y
      case 2 => x - y
      case 3 => x * y
    }
  }

  def parseTerm(str: String): Int = {
    if(str.charAt(pos) == '(' || str.charAt(pos) == ')') {
      pos = pos + 1
      println(s"parseT (), at ${pos - 1}, ${str.charAt(pos - 1)}")
      return parseExp(str)
    }
//    else if(str.charAt(pos) == '+'){
//      oper = 1
////      pos = pos + 1
//      println(s"parseT +, ${pos}, ${str.charAt(pos)}")
//      return -1000000
//    }
//    else if(str.charAt(pos) == '-'){
//      oper = 2
////      pos = pos + 1
//      println(s"parseT -, ${pos}, ${str.charAt(pos)}")
//      return -1000000
//    }else if(str.charAt(pos) == '*'){
//      oper = 3
////      pos = pos + 1
//      println(s"parseT *, ${pos}, ${str.charAt(pos)}")
//      return -1000000
//    }
    else if(str.charAt(pos) == '0' || str.charAt(pos) == '1' || str.charAt(pos) == '2' || str.charAt(pos) == '3' || str.charAt(pos) == '4' || str.charAt(pos) == '5' || str.charAt(pos) == '6' || str.charAt(pos) == '7' || str.charAt(pos) == '8' || str.charAt(pos) == '9') {
//      pos = pos + 1
      println(s"parseT num, at ${pos}, ${str.charAt(pos)}")
      return str.charAt(pos).toInt
    }
    else if(str.charAt(pos) == '+' || str.charAt(pos) == '-'){
      pos = pos + 1
      println(s"parseT +/-, at ${pos - 1}, ${str.charAt(pos-1)}")
      return parseExp(str)
    }
    else {
      pos = pos + 1
      println(s"parseT *, at ${pos-1}, ${str.charAt(pos-1)}")
      return parseExp(str)
    }
  }

  def parseExp(str: String): Int = {
    val first = parseFact(str)
    if((pos + 1) != str.length  && str.charAt(pos) == '+') {
      println(s"parseE found +, ${pos}, ${str.charAt(pos)}")
      oper = 1
    }
    else if ((pos + 1) != str.length && str.charAt(pos) == '-') {
      println(s"parseE found -, ${pos}, ${str.charAt(pos)}")
      oper = 2
    }
    pos = pos + 1
    val second = parseExp(str)

    println(s"parseE, ${pos}, ${str.charAt(pos)}")
    return compute(first, second)
  }

  def parseFact(str: String): Int = {
    val first = parseTerm(str)
    if((pos + 1) != str.length  && str.charAt(pos) == '*') {
      println(s"parseF found *, ${pos}, ${str.charAt(pos)}")
      oper = 3
    }
    pos = pos + 1
    val second = parseFact(str)

    println(s"parseF, ${pos}, ${str.charAt(pos)}")
    return compute(first, second)
  }

  val testString: String = "5*(3+4)"
  println(parseExp(testString))

}

