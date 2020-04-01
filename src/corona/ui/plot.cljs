(ns corona.ui.plot
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.p5 :refer [p5-canvas]]

    [corona.ui.theme :refer [plot-i-color plot-s-color plot-r-color]]
    [corona.math  :refer [ceil round]]
    [corona.sim :as sim]))

;;; WIP
(def demo-sim (reduce
                (fn [s _] (conj s (sim/step (last s))))
                [(sim/init {:w 100 :h 100} [100 1 0])]
                (range 100)))

(def demo-data (mapv :report demo-sim))
;;;


(defn draw-susceptible! [ctx w h x-points]
  (doto ctx
    (.fill plot-s-color)
    (.beginShape)
    (.vertex 0 h))

  (loop [[[pos-x {I :I S :S} i] & points] x-points]
    (.vertex ctx pos-x (- h (* (+ I S) h)))
    (when-not (empty? points) (recur points)))

  (doto ctx
    (.vertex w h)
    (.endShape)))


(defn draw-infected! [ctx w h x-points]
  (doto ctx
    (.fill plot-i-color)
    (.beginShape)
    (.vertex 0 h))

  (loop [[[pos-x {I :I} i] & points] x-points]
    (.vertex ctx pos-x (- h (* (+ I) h)))
    (when-not (empty? points) (recur points)))

  (doto ctx
    (.vertex w h)
    (.endShape)))


(defw graph []
  [p5-canvas
     :draw (fn [ctx]
             (let [w (.-width ctx)
                   h (.-height ctx)

                   ;; number of x values?
                   x-n (min
                         (quot w 20)
                         (count demo-data))

                   ;; how far are the x values in px?
                   xw-px (quot w  x-n)

                   step-size (round (/ (count demo-data) x-n))

                  ;; vector of [x-pos, {:S .. :I .. :R ..}, value]
                   x-points (map vector
                              (range 0 w xw-px)
                              (take-nth step-size demo-data)
                              (range 0 99999 step-size))]

               (doto ctx
                (.background plot-r-color)
                (.noStroke))

              (draw-susceptible! ctx w h x-points)
              (draw-infected! ctx w h x-points)))])
