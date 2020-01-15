package optics

import optics.internal.{Applicative, TraversalRes}

import scala.annotation.alpha

trait EPOptional[+E, -S, +T, +A, -B] extends EPTraversal[E, S, T, A, B] { self =>
  def getOrModify(from: S): Either[(E, T), A]

  def traversal[F[+ _] : Applicative](f: A => F[B])(from: S): TraversalRes[F, E, T] =
    getOrModify(from) match {
      case Left((e, t)) => TraversalRes(Some(e), Applicative[F].pure(t))
      case Right(to)    => TraversalRes(None   , Applicative[F].map(f(to))(replace(_)(from)))
    }

  def getOrError(from: S): Either[E, A] =
    getOrModify(from).left.map(_._1)

  def getOption(from: S): Option[A] =
    getOrModify(from).toOption

  @alpha("andThen")
  def >>>[E1 >: E, C, D](other: EPOptional[E1, A, B, C, D]): EPOptional[E1, S, T, C, D] = new EPOptional[E1, S, T, C, D] {
    def getOrModify(from: S): Either[(E1, T), C] =
      for {
        a <- self.getOrModify(from)
        t <- other.getOrModify(a).left.map{ case (e1, b) => (e1, self.replace(b)(from)) }
      } yield t
    override def replace(to: D): S => T = self.modify(other.replace(to))
  }
}

object EPOptional {
  def apply[E, S, T, A, B](_getOrModify: S => Either[(E, T), A])(_replace: B => S => T): EPOptional[E, S, T, A, B] = new EPOptional[E, S, T, A, B] {
    def getOrModify(from: S): Either[(E, T), A] = _getOrModify(from)
    override def replace(to: B): S => T = _replace(to)
  }
}

object POptional {
  def apply[S, T, A, B](_getOrModify: S => Either[T, A])(_replace: B => S => T): POptional[S, T, A, B] =
    EPOptional[Unit, S, T, A, B](_getOrModify(_).left.map(() -> _))(_replace)
}

object Optional {
  def apply[A, B](_getOption: A => Option[B])(_replace: B => A => A): Optional[A, B] =
    POptional[A, A, B, B](from => _getOption(from).toRight(from))(_replace)
}
