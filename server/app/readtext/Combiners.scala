package readtext

trait SFunc[S,R] extends (S=>(R,S)) {
  def map[R2](m:R=>R2): SFunc[S,R2] = SFunc{ s =>
    val (a,s1) = apply(s)
    (m(a), s1)
  }

  def flatMap[R2](f: R=>SFunc[S,R2]): SFunc[S,R2] = SFunc{ s=>
    val (r1,s1) = apply(s)
    f(r1)(s1)
  }

  def onlyResult: S=>R = s => apply(s)._1
}

object SFunc {
  def apply[S,R](f: S=>(R,S)): SFunc[S,R] = new SFunc[S,R] {
    override def apply(s: S) = f(s)
  }
}

object Combiners {

  def sequence[S,R](gs: List[SFunc[S,R]]): SFunc[S, List[R]] =
    sequence(gs, List[R]())((l,e) => e::l) map (_.reverse)

//  def sequence[S,A,B](gs: Traversable[Gen[S,A]], zero: B)(f: (B,A) => B): Gen[S,B] = Gen { s =>
//    gs.foldLeft((zero, s)) { case ((buf, s), g) =>
//      val (a, s1) = g(s)
//      (f(buf, a), s1)
//    }
//  }

  def sequence[S,A,B](gs: Traversable[SFunc[S,A]], zero: B)(f: (B,A) => B): SFunc[S,B] = SFunc { s =>
    ((zero, s) /: gs) { case ((buf, s), g) =>
      val (a, s1) = g(s)
      (f(buf, a), s1)
    }
  }
}