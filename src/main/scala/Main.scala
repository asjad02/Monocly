import example.Foo.{FooI, FooIS, FooList, FooS}
import example.{Foo, Mono, Poly}
import optics.poly._

object Main {

  def main(args: Array[String]): Unit = {

    val monoSome = Mono(1, FooIS(8, "hey"), Some(0), List(), Map())
    val monoNone = Mono(1, FooIS(8, "hey"), None, List(), Map())

    println(Mono.foo.andThen(Foo.fooIS).getOption(monoSome))
    println(Mono.foo.andThen(Foo.fooIS).andThen(FooIS.i).getOption(monoSome))
    println(Mono.optI.andThen(Prism.some).getOption(monoSome))

    // return error message
    println(Mono.optI.andThen(Prism.some).getOrError(monoNone))


    val poly1 = Poly(1, FooIS(8, "hey"), Some(0), Option(3))
    // change a Poly[Option[Int]] to  Poly[Option[String]]
    println(Poly.pparam.andThen(PPrism.some).replaceOrError("hello")(poly1))

    val poly2 = Poly(1, FooIS(8, "hey"), Some(0), Some(3))
    // change a Poly[Some[Int]] to  Poly[Some[String]]
    println(Poly.pparam.andThen(PPrism.some).replaceOrError("hello")(poly2))

    // combine poly and monomorphic optics
    val poly3 = Poly(1, FooIS(8, "hey"), Some(0), Option(FooI(4): Foo))
    println(Poly.param.andThen(Prism.some).andThen(Foo.fooI).replaceOrError(FooI(7))(poly3))

    // Left(Expected FooIS but got FooI(4))
//    println((Poly.param.andThen(PPrism.some.andThen(Foo.fooIS).getOrError(poly3))
    // Left(it is none)
//    println((Poly.param.andThen(PPrism.some.andThen(Foo.fooIS).getOrError(poly3.copy(param = None)))


    // (true,false)
    println(NonEmptyPTraversal.pair.modify((_: Int) % 2 == 0)((10, 7)))
    // List(1, 3, 6, 10)
    println(NonEmptyPTraversal.pair.andThen(NonEmptyPTraversal.pair).toListOrError(((1, 3), (6, 10))))
    // List(1, 3, 6, 10)
    println(NonEmptyTraversal.pair.andThen(NonEmptyTraversal.pair).toListOrError(((1, 3), (6, 10))))

    // Left(Expected FooI but got FooS(hello))
    println(EPTraversal.list.andThen(Foo.fooI).toListOrError(List(FooI(10), FooS("hello"), FooI(0), FooIS(3, "world"))))
    // Left(List is empty)x
    println(EPTraversal.list.andThen(Foo.fooI).toListOrError(Nil))
    // Right(List(FooI(10), FooI(0)))
//    println((PTraversal.list >>>? Foo.fooI).toListOrError(List(FooI(10), FooS("hello"), FooI(0), FooIS(3, "world"))))

    // Right(List(1, 2, 3, 4))
    println(Foo.fooL.andThen(EPTraversal.list).toListOrError(FooList(List(1,2,3,4))))
    // Left(List is empty)
    println(Foo.fooL.andThen(EPTraversal.list).toListOrError(FooList(Nil)))
    // Left(Expected FooList but got FooI(10))
    println(Foo.fooL.andThen(EPTraversal.list).toListOrError(FooI(10)))
    // Right(List()) TODO It should be a Left if there is no item matching
//    println((Foo.fooL ?>>> PTraversal.list).toListOrError(FooI(10)))

  }

}
