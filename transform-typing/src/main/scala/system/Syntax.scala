package system

object Syntax {

  trait ISort

  case class Sort(name: String) extends ISort {
    assert(name != "Prop")
    override def toString: String = name
  }

  case object Prop extends ISort {
    override def toString: String = "Prop"
  }

  case class Symbol(name: String, in: List[ISort], out: ISort) {
    override def toString: String = name
    def sigString =
      if (in.isEmpty)
        s"$name: $out"
      else
        s"$name: ${in.mkString(" ")} -> $out"
  }

  type Subst = Map[Var, Term]
  type Error = String

  trait Term {
    val sort: ISort

    def apply(i: Int): Term

    def isGround: Boolean

    def freevars: Set[Var]

    def subst(s: Subst): Term

    def occurs(v: Var): Boolean

    def unify(t: Term): Either[Subst, Error] = unify(t, Map())
    def unify(t: Term, s: Subst): Either[Subst, Error] = unify(t, Map())

    def findAll(p: Term => Boolean): Seq[Term]
  }

  case class Var(name: String, sort: ISort) extends Term {
    override def apply(i: Int) = throw new IndexOutOfBoundsException(s"Var term $this has no children")

    override def isGround: Boolean = false

    override def toString: String = "$" + name

    override def freevars: Set[Var] = Set(this)

    override def subst(s: Subst): Term = s.getOrElse(this, this)

    override def occurs(v: Var): Boolean = this == v

    override def unify(t: Term, s: Subst): Either[Subst, Error] = s.get(this) match{
      case Some(t2) => t2.unify(t, s)
      case None =>
        val tt = t.subst(s)
        if (this == tt)
          Left(Map())
        else if (tt.occurs(this))
          Right(s"Occurs check failed, $this occurs in $tt")
        else {
          val news = Map(this -> tt)
          Left(s.mapValues(_.subst(news)) ++ news)
        }
    }

    override def findAll(p: (Term) => Boolean): Seq[Term] = if (p(this)) Seq(this) else Seq()
  }

  case class App(sym: Symbol, kids: List[Term]) extends Term {
    assert(sym.in.size == kids.size)
    assert(sym.in.zip(kids) forall (p => p._1 == p._2.sort))

    override val sort: ISort = sym.out

    override def apply(i: Int) = kids(i)

    override def isGround: Boolean = kids.forall(_.isGround)

    override def toString: String = {
      val ks = kids.mkString(" ")
      val space = if (ks.isEmpty) "" else " "
      s"($sym$space$ks)"
    }

    override def freevars: Set[Var] = kids.toSet[Term].flatMap(_.freevars)

    override def subst(s: Subst): Term = App(sym, kids.map(_.subst(s)))

    override def occurs(v: Var): Boolean = kids.exists(_.occurs(v))

    override def unify(t: Term, s: Subst): Either[Subst, Error] =
      t match {
        case v: Var => t.unify(this, s)
        case App(`sym`, tkids) =>
          kids.zip(tkids).foldLeft[Either[Subst, Error]](Left(s)){
            case (err@Right(_), _) => err
            case (Left(s), (t1, t2)) => t1.unify(t2, s)
          }
        case _ => Right(s"Could not match $this against $t")
      }

    override def findAll(p: (Term) => Boolean): Seq[Term] = {
      val me = if (p(this)) Seq(this) else Seq()
      me ++ kids.flatMap(_.findAll(p))
    }
  }
  object App {
    def apply(sym: Symbol, kids: Term*): App = App(sym, kids.toList)
  }

  case class Judg(sym: Symbol, terms: List[Term]) {
    assert(sym.out == Prop)
    assert(sym.in.size == terms.size)
    assert(sym.in.zip(terms) forall (p => p._1 == p._2.sort))

    def apply(i: Int) = terms(i)

    def updatedCopy(i: Int, t: Term) = Judg(sym, terms.updated(i, t))

    def subst(s: Subst): Judg = Judg(sym, terms.map(_.subst(s)))

    override def toString: String = s"$sym(${terms.mkString(" ")})"

    def toString(mark: Int): String = {
      val start = s"$sym("
      val end = ")"
      val mid1 = terms.slice(0, mark).mkString(" ")
      val markterm = terms(mark).toString
      var mid2 = if (mark < terms.size) s" [${markterm.substring(1, markterm.size-1)}] " else ""
      val mid3 = terms.slice(mark + 1, terms.size).mkString(" ")
      if (mid1.isEmpty)
        mid2 = mid2.substring(1)
      if (mid3.isEmpty)
        mid2 = mid2.substring(0, mid2.size - 1)
      start + mid1 + mid2 + mid3 + end
    }
  }
  object Judg {
    def apply(sym: Symbol, terms: Term*): Judg = Judg(sym, terms.toList)
  }

  case class Rule(name: String, conclusion: Judg, premises: List[Judg]) {
    override def toString: String = {
      val indent = "  "
      val ps = premises.mkString("\n" + indent)
      val psn = if (ps.isEmpty) "" else "\n"
      s"$name:\n$indent$ps$psn$indent=>\n$indent$conclusion"
    }
  }
  object Rule {
    def apply(name: String, conclusion: Judg, premises: Judg*): Rule = Rule(name, conclusion, premises.toList)
  }

  case class Rewrite(pat: Term, gen: Term) {
    assert(gen.freevars.subsetOf(pat.freevars))
    override def toString: String = s"$pat ~> $gen"
  }

  case class Transform(contract: Rule, pos: Int, rewrites: List[Rewrite]) {
    assert(pos < contract.conclusion.terms.size)
    assert(contract.conclusion.terms(pos).isInstanceOf[App])

    val contractedTerm = contract.conclusion.terms(pos).asInstanceOf[App]
    val contractedSym = contractedTerm.sym
    rewrites.foreach(r =>
      assert(r.pat.isInstanceOf[App] && r.pat.asInstanceOf[App].sym == contractedSym, s"Rewrite $r does not match contracted symbol $contractedSym"))

    override def toString: String = {
      val premises = contract.premises
      val name = contract.name
      val indent = "  "
      val ps = premises.mkString("\n" + indent)
      val scontract = s"$name:\n$indent$ps\n$indent=>\n$indent${contract.conclusion.toString(pos)}"
      s"""${contractedSym.sigString}
         |
         |contract
         |$scontract
         |
         |rewritings
         |${rewrites.mkString("\n")}
       """.stripMargin
    }
  }

  object Transform {
    def apply(contract: Rule, pos: Int, rewrites: Rewrite*): Transform = new Transform(contract, pos, rewrites.toList)
  }

}