(ns corona.ui.stage
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.p5 :refer [p5-canvas]]
    [corona.ui.theme :refer [stage-i-color stage-s-color stage-r-color]]))



(defw stage []
  [p5-canvas
     :draw (fn [ctx]
             (let [w       (.-width ctx)
                   h       (.-height ctx)
                   n-frame (.-frameCount ctx)]
               (doto ctx
                (.background 0 0 0)
                (.noStroke)

                ; S
                (.fill stage-s-color)
                (.circle (+ (* 0.1 w) (/ w 2)) (/ h 2) 10)

                ;; I
                (.fill stage-i-color)
                (.circle (/ w 2) (/ h 2) 10)
                (.circle (+ (* 0.2 w) (/ w 2)) (/ h 2) 10)

                ;; infection radius
                (.strokeWeight 2)
                (.stroke stage-i-color)
                (.noFill)
                (.circle (+ (* 0.2 w) (/ w 2)) (/ h 2) (mod (+ 0 n-frame) 40))
                (.circle (+ 0         (/ w 2)) (/ h 2) (mod (+ 10 n-frame) 40)))))])
