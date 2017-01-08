package readtext

import readtext.Action._

class Action[S, R](run: S => (R,S)) {
//  def apply(s: S) = run(s)
//
//  def flatMap[B](f: R => Action[S,B]): Action[S, B] = Action{s =>
//  val (r,s1) = run(s)
//    f(r).run(s1)
//  }
//
//  def map[R2](f: R => R2): Action[S, R2] = flatMap(r => unit(f(r)))
//
//  def concat[R2,R3](next: Action[S,R2])(f: (R,R2) => R3): Action[S, R3] = for {
//    r1 <- this
//    r2 <- next
//  } yield f(r1,r2)
}

object Action {
//  def apply[S,R](run: S => (R, S)): Action[S,R] = new Action(run)
//  def unit[S,R](r: R): Action[S,R] = Action(s => (r,s))
//
//  def sequence[S,R](as: List[Action[S,R]]): Action[S, List[R]] =
//    sequence(as, List[R]())((l,e) => e::l) map(_.reverse)
//
//  def sequence[S,R,B](as: Traversable[Action[S,R]], zero: B)(f: (B,R) => B): Action[S, B] = Action{s=>
//    as.foldLeft((zero,s)){case ((buf,s), a)=>
//        val (v,s1) = a(s)
//      (f(buf, v),s1)
//    }
//  }
}


