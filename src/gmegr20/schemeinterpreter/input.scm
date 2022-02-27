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

(isPrime 7)

((lambda (x y) (+ x y)) 1 2)

(define (square x) (* x x))

(map square '(1 2 3))

(map (lambda (x) (* x x)) '(1 2 3))

(apply + '(1 2 3))
(eval '(and #t #t))

(null? '())
(length '(1 2 ()))