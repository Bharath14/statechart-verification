(declare-fun switch_status_1()Int)
(assert (not (not( = (- switch_status_1 1) 1)) ))
(check-sat)
(exit)
