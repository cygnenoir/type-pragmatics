
(declare-sort Term 0)
(declare-fun lookup (Term Term Term) Bool)
(declare-fun tcheck (Term Term Term) Bool)
(declare-fun future (Term) Term)
(declare-fun bind (Term Term Term) Term)
(declare-fun arrow (Term Term) Term)
(declare-fun app (Term Term) Term)
(declare-fun join (Term) Term)
(declare-fun fork (Term) Term)
(declare-fun fabs (Term Term Term) Term)
(declare-fun par (Term) Term)
(declare-fun const-x () Term)
(declare-fun const-t () Term)
(declare-fun const-eapp1 () Term)
(declare-fun const-eapp2 () Term)
(declare-fun const-tapp () Term)
(declare-fun const-xabs () Term)
(declare-fun const-sabs () Term)
(declare-fun const-eabs () Term)
(declare-fun const-tabs () Term)
(define-fun lookup-ctx-base () Bool (forall ((X Term) (T Term) (CTX Term)) (lookup X (bind X T CTX) T)))
(define-fun lookup-ctx-step () Bool (forall ((X Term) (Y Term) (T Term) (T2 Term) (CTX Term)) (=> (and (distinct X Y) (lookup X CTX T)) (lookup X (bind Y T2 CTX) T))))
(define-fun T-var () Bool (forall ((C Term) (X Term) (T Term)) (=> (lookup X C T) (tcheck C X T))))
(define-fun T-abs () Bool (forall ((C Term) (X Term) (E Term) (S Term) (T Term)) (=> (tcheck (bind X S C) E T) (tcheck C (fabs X S E) (arrow S T)))))
(define-fun T-app () Bool (forall ((S Term) (C Term) (E Term) (F Term) (T Term)) (=> (and (tcheck C E (arrow S T)) (tcheck C F S)) (tcheck C (app E F) T))))
(define-fun T-fork () Bool (forall ((C Term) (E Term) (T Term)) (=> (tcheck C E T) (tcheck C (fork E) (future T)))))
(define-fun T-join () Bool (forall ((C Term) (E Term) (T Term)) (=> (tcheck C E (future T)) (tcheck C (join E) T))))
(define-fun T-IH-par-abs () Bool (forall ((C Term) (T Term)) (=> (tcheck C const-eabs T) (tcheck C (par const-eabs) T))))
(define-fun T-abs-inversion () Bool (forall ((T Term) (X Term) (S Term) (C Term) (E Term) (T2 Term)) (=> (tcheck C (fabs X S E) T) (exists ((T2 Term)) (and (= T (arrow S T2)) (tcheck (bind X S C) E T2))))))
(define-fun T-IH-par-app-1 () Bool (forall ((C Term) (T Term)) (=> (tcheck C const-eapp1 T) (tcheck C (par const-eapp1) T))))
(define-fun T-IH-par-app-2 () Bool (forall ((C Term) (T Term)) (=> (tcheck C const-eapp2 T) (tcheck C (par const-eapp2) T))))
(define-fun T-app-inversion () Bool (forall ((E Term) (T Term) (C Term) (F Term) (S Term)) (=> (tcheck C (app E F) T) (exists ((S Term)) (and (tcheck C E (arrow S T)) (tcheck C F S))))))
(assert lookup-ctx-base)
(assert lookup-ctx-step)
(assert T-var)
(assert T-abs)
(assert T-app)
(assert T-fork)
(assert T-join)
(assert T-IH-par-abs)
(assert T-abs-inversion)
(assert T-IH-par-app-1)
(assert T-IH-par-app-2)
(assert T-app-inversion)
(define-fun T-par-x () Bool (forall ((C Term)) (=> (tcheck C const-x const-t) (tcheck C const-x const-t))))
(define-fun T-par-app () Bool (forall ((C Term)) (=> (tcheck C (app const-eapp1 const-eapp2) const-tapp) (tcheck C (app (join (fork (par const-eapp1))) (join (fork (par const-eapp2)))) const-tapp))))
(define-fun T-par-abs () Bool (forall ((C Term)) (=> (tcheck C (fabs const-xabs const-sabs const-eabs) const-tabs) (tcheck C (fabs const-xabs const-sabs (par const-eabs)) const-tabs))))
(push 1)
(assert (not T-par-x))
(check-sat)
(pop 1)
(push 1)
(assert (not T-par-app))
(check-sat)
(pop 1)
(push 1)
(assert (not T-par-abs))
(check-sat)
(pop 1)
