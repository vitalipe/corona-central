(ns alpakit.animation.impl.spring
  (:require
    #?(:cljs [reagent.core :as r :refer [atom cursor next-tick]])
   [alpakit.animation.impl.transitionable :refer [->Transitionable]]))


#?(:clj (def cursor #()))
#?(:clj (def next-tick #()))

;; most of the spring stuff was taken from:
;; https://github.com/timothypratley/reanimated

(defn- abs [n]
  #?(:clj  (.abs Math n)
     :cljs (.abs js/Math n)))

(defn- now []
  #?(:clj (System/currentTimeMillis)
     :cljs (.now js/Date)))



(defn- evaluate
  "This is where the spring physics formula is applied."
  [x2 dt x v a mass stiffness damping]
  (let [x (+ x (* v dt))
        v (+ v (* a dt))
        f (- (* stiffness (- x2 x)) (* damping v))
        a (/ f mass)]
    [v a]))


(defn- rk4
  "Takes an itegration step from numbers x to x2 over time dt,
  with a present velocity v."
  [x2 dt x v mass stiffness damping]
  (let [dt2 (* dt 0.5)
        [av aa] (evaluate x2 0.0 x v 0.0 mass stiffness damping)
        [bv ba] (evaluate x2 dt2 x av aa mass stiffness damping)
        [cv ca] (evaluate x2 dt2 x bv ba mass stiffness damping)
        [dv da] (evaluate x2 dt x cv ca mass stiffness damping)
        dx (/ (+ av (* 2.0 (+ bv cv)) dv) 6.0)
        dv (/ (+ aa (* 2.0 (+ ba ca)) da) 6.0)]
    [(+ x (* dx dt)) (+ v (* dv dt))]))




(defn spring [& {:keys [to
                        mass
                        stiffness
                        damping
                        initial-velocity
                        initial-value]
                 :or {to        0
                      mass      10.0
                      stiffness 1.0
                      damping   1.0
                      initial-velocity  0.0
                      initial-value     0}}]

  (let [control (atom {:to        to
                       :mass      mass
                       :stiffness stiffness
                       :damping   damping})

         x2                  (cursor control [:to])
         last                (clojure.core/atom initial-value)
         x2prev              (clojure.core/atom to)
         internal-anim-state (atom {:t (now)
                                    :x initial-value
                                    :v initial-velocity})
        next-step! (fn []
                    (when (not= @x2 @x2prev)
                      (reset! last @x2prev)
                      (reset! x2prev @x2))

                    (let [{:keys [mass stiffness damping]} @control
                          {:keys [x v t]} @internal-anim-state
                          t2 (now)
                          dt (min 1 (/ (- t2 t) 10.0))
                          threshold (/ (abs (- @last @x2)) 100.0)]

                      ;; some libs allow the user to set threshold
                      ;; I don't think it's useful, but we can change this later...
                      (if (and (<= (- threshold) (- x @x2) threshold)
                               (<= (- threshold) v threshold))
                        (do ;; we're done
                          (swap! internal-anim-state assoc :x @x2 :v 0)
                          (deref x2))

                        ;; otherwise do next step
                        (let [[x v] (rk4 @x2 dt x v mass stiffness damping)]
                          (next-tick #(reset! internal-anim-state {:t t2 :x x :v v}))
                          x))))]

    (->Transitionable
       (cursor #(next-step!) [])
       x2
       control
       {}
       (hash [internal-anim-state control]))))
