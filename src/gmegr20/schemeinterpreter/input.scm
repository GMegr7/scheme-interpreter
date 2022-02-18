(define (isPrime x)
    (define (checkToX x curr)
        (if (= x curr) #t
            (if (= (* (/ x curr) curr) x) #f
                (checkToX x (+ curr 1)))))
    (checkToX x 2))

(define (fibonacci n)
    (if (= n 1) 1
    (if (= n 2) 1
        (+ (fibonacci (- n 1)) (fibonacci (- n 2))))))

(= 1 2)