package readtext

trait Gen[S,R] extends (S=>(R,S)) {
  def map[R2](m:R=>R2): Gen[S,R2] = Gen{ s =>
    val (a,s1) = apply(s)
    (m(a), s1)
  }

  def flatMap[R2](f: R=>Gen[S,R2]): Gen[S,R2] = Gen{s=>
    val (r1,s1) = apply(s)
    f(r1)(s1)
  }

  def onlyResult: S=>R = s => apply(s)._1
}

object Gen {
  def apply[S,R](f: S=>(R,S)): Gen[S,R] = new Gen[S,R] {
    override def apply(s: S) = f(s)
  }
}

object Combiners {

  def sequence[S,R](gs: List[Gen[S,R]]): Gen[S, List[R]] =
    sequence(gs, List[R]())((l,e) => e::l) map (_.reverse)

//  def sequence[S,A,B](gs: Traversable[Gen[S,A]], zero: B)(f: (B,A) => B): Gen[S,B] = Gen { s =>
//    gs.foldLeft((zero, s)) { case ((buf, s), g) =>
//      val (a, s1) = g(s)
//      (f(buf, a), s1)
//    }
//  }

  def sequence[S,A,B](gs: Traversable[Gen[S,A]], zero: B)(f: (B,A) => B): Gen[S,B] = Gen { s =>
    ((zero, s) /: gs) { case ((buf, s), g) =>
      val (a, s1) = g(s)
      (f(buf, a), s1)
    }
  }
}