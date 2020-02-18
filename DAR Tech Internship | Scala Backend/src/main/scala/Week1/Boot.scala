package Week1

object Boot extends App {

  // exercise 2.1.4.1
  val temp1: Int = 1 + 2 // its int with value 3
  println(1+2)
  val temp2: Int = "3".toInt // its int with value 3
  println(temp2)
//  val temp3: Int = "foo".toInt // its says that its int but actually it is an error
//  println(temp3)

  // exercise 2.2.5.1
  "foo" take 1
  1.+(2).+(3)

  // exercise 2.2.5.2
  1 + 2 + 3 // it gets it value by arithmetical operation
  6 // it gets its value automatically(from the beginning)
  // they have the same type: Int, and the same value
  // i think that there is no difference if you want to use it in program you may use both

  // exercise 2.3.8.1
  // 42 is Int, true is Boolean, 123L is Long, 42.0 is Double

  // exercise 2.3.8.2
  // 'a' is a char, "a" is a string

  // exercise 2.3.8.3
  "Hello world" // is a string
  println("Hello world") // is a Unit

  // exercise 2.3.8.4
  // 'Hello world' its incorrect

  // exercise 2.4.5.1
  object Oswald {
    val colour: String = "Black"
    val food: String = "Milk"
  }

  object Henderson {
    val colour: String = "Ginger"
    val food: String = "Chips"
  }

  object Quentin {
    val colour: String = "Tabby and white"
    val food: String = "Curry"
  }

  // exercise 2.4.5.2
  object calc {
    def square(x: Double) = x * x
    def cube(x: Double) = x * square(x)
  }

  // exercise 2.4.5.3
  object calc2 {
    def square(x: Double) = x * x
    def square(x: Int) = x * x
    def cube(x: Double) = x * square(x)
    def cube(x: Int) = x * square(x)
  }

  // exercise 2.4.5.4
  object argh {
    def a = {
      println("a")
      1
    }
    val b = {
      println("b")
      a + 2
    }
    def c = {
      println("c")
      a
      b + "c"
    }
  }
  val res: String = argh.c + argh.b + argh.a
  println(res)

  // exercise 2.4.5.5
  object person {
    val firstname = "Arman"
    val secondname = "Kizatov"
  }
  object alien {
    def greet(p: person.type ): String = s"Hello, ${person.firstname}!"
  }
  // No we can't greet other objects, because we use person as a parameter

  // exercise 2.6.4.1
  val str: String = if(1 > 2) "alien" else "predator" // value = predator(2 > 1) type = String

  // exercise 2.6.4.2
  val any = if(1 > 2) "alien" else 2001 // value = 2001 type = Any

  // exercise 2.6.4.3
  val unitany =  if(false) "hello" // both type and value are Any and Unit

  // exercise 3.1.6.1
  class Cat(val name: String, val colour: String, val food: String)
  val oswald = new Cat("Oswald", "Black", "Milk")
  val henderson = new Cat("Henderson", "Ginger", "Chips")
  val quentin = new Cat("Quentin", "Tabby and white", "Curry")

  // exercise 3.1.6.2
  object ChipShop {
    def willServe(cat: Cat): Unit = {
      if(cat.food == "Chips") {
        true
      } else {
        false
      }
    }
  }

  // exercise 3.1.6.3
  class Director(val firstName: String, val lastName: String, val yearOfBirth: Int) {
    def name: String = {
      val fullname = s"${this.firstName} ${this.lastName}"
      return fullname
    }

    def copy(firstName: String = this.firstName,
             lastName: String = this.lastName,
             yearOfBirth: Int = this.yearOfBirth): Director = new Director(firstName, lastName, yearOfBirth)
  }


  class Film(val name: String, val yearOfRelease: Int, val imdbRating: Double, val director: Director) {
    def directorsAge =
      yearOfRelease - director.yearOfBirth

    def isDirectedBy(director: Director) = this.director == director

    def copy(name: String = this.name,
             yearOfRelease: Int = this.yearOfRelease,
             imdbRating: Double = this.imdbRating,
             director: Director = this.director): Film = new Film(name, yearOfRelease, imdbRating, director)

  }
  val eastwood = new Director("Clint", "Eastwood", 1930)
  val mcTiernan = new Director("John", "McTiernan", 1951)
  val nolan = new Director("Christopher", "Nolan", 1970)
  val someBody = new Director("Just", "Some Body", 1990)
  val memento = new Film("Memento", 2000, 8.5, nolan)
  val darkKnight = new Film("Dark Knight", 2008, 9.0, nolan)
  val inception = new Film("Inception", 2010, 8.8, nolan)
  val highPlainsDrifter = new Film("High Plains Drifter", 1973, 7.7, eastwood)
  val outlawJoseyWales = new Film("The Outlaw Josey Wales", 1976, 7.9, eastwood)
  val unforgiven = new Film("Unforgiven", 1992, 8.3, eastwood)
  val granTorino = new Film("Gran Torino", 2008, 8.2, eastwood)
  val invictus = new Film("Invictus", 2009, 7.4, eastwood)
  val predator = new Film("Predator", 1987, 7.9, mcTiernan)
  val dieHard = new Film("Die Hard", 1988, 8.3, mcTiernan)
  val huntForRedOctober = new Film("The Hunt for Red October", 1990, 7.6, mcTiernan)
  val thomasCrownAffair = new Film("The Thomas Crown Affair", 1999, 6.8, mcTiernan)

  println(eastwood.yearOfBirth )// should be 1930
  println(dieHard.director.name) // should be "John McTiernan"
  println(invictus.isDirectedBy(nolan)) // should be false
  highPlainsDrifter.copy(name = "L'homme des hautes plaines") // returns Film("L'homme des hautes plaines", 1973, 7.7, /* etc */)
  thomasCrownAffair.copy(yearOfRelease = 1968, director = new Director("Norman", "Jewison", 1926))
  // returns Film("The Thomas Crown Affair", 1926, /* etc */)
  inception.copy().copy().copy()
  // returns a new copy of `inception`

  // exercise 3.1.6.4, 3.1.6.5, 3.1.6.6
  class Counter(val count: Int) {
    def inc = new Counter(this.count + 1)
    def dec = new Counter(this.count - 1)
    def inc(x: Int = 1) = new Counter(count + x)
    def dec(x: Int = 1) = new Counter(count - x)

    def adjust(adder: Adder): Unit = {
      new Counter(adder.add(count))
    }
  }

//  val cal = new Counter(10).inc(4).dec.inc.inc.count

  class Adder(amount: Int) {
    def add(in: Int) = in + amount
  }

  // exercise 3.3.2.1
  class Person(val firstName: String, val secondName: String)

  object Person {
    def apply(name: String): Person = {
      val parts = name.split(" ")
      new Person(parts(0), parts(1))
    }
  }

  println(Person.apply("Arman Kizatov").firstName)
  println(Person.apply("Arman Kizatov").secondName)

  // exercise 3.3.2.2
  object Director {
    def apply(firstName: String, lastName: String, yearOfBirth: Int): Director = {
      new Director(firstName, lastName, yearOfBirth)
    }

    def older(dir1: Director, dir2: Director): Director = {
      if(dir1.yearOfBirth > dir2.yearOfBirth){
        dir1
      } else {
        dir2
      }
    }
  }

  object Film {
    def apply(name: String, yearOfRelease: Int, imdbRating: Double, director: Director): Film = {
      new Film(name, yearOfRelease, imdbRating, director)
    }

    def highestRating(film1: Film, film2: Film): Film = {
      val rating1 = film1.imdbRating
      val rating2 = film2.imdbRating
      if(rating1 >= rating2) {
        film1
      } else {
        film2
      }
    }

    def oldestDirectorAtTheTime(film1: Film, film2: Film): Film = {
      val dir1 = film1.director
      val dir2 = film2.director
      val age1 = dir1.yearOfBirth
      val age2 = dir2.yearOfBirth
      if(age1 >= age2) {
        film1
      } else {
        film2
      }
    }
  }

  // exercise 3.3.2.3
  // val prestige: Film = bestFilmByChristopherNolan() - type
  // new Film("Last Action Hero", 1993, mcTiernan) - type(reference to a constructor which is a type
  // Film("Last Action Hero", 1993, mcTiernan) - value(reference to a apply
  // Film.newer(highPlainsDrifter, thomasCrownAffair) - value
  // Film.type - value

  // exercise 3.4.5.1
//  case class Cat(name: String, color: String, food: String)

  // exercise 3.4.5.2
//  case class Director(firstName: String, lastName: String, yearOfBirth: Int) {
//    def name: String = {
//      s"${this.firstName} ${this.lastName}"
//    }
//  }

//  case class Film(name: String, yearOfRelease: Int, imdbRating: Double, director: Director) {
//    def directorsAge =
//      yearOfRelease - director.yearOfBirth
//
//    def isDirectedBy(director: Director) = this.director == director
//  }

  // exercise 3.4.5.3
//  case class Counter(count: Int) {
//    def inc = copy(count = count + 1)
//    def dec = copy(count = count - 1)
//    def adjust(adder: Adder) = {
//      copy(count = adder(count))
//    }
//  }

  // exercise 3.4.5.4
//  case class Person(firstName: String, secondName: String)
}
