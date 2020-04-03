(ns corona.ui.stage
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.p5 :refer [p5-canvas]]
    [corona.ui.theme :as theme]))



(defw stage []
  [p5-canvas
     :css {:border-color theme/color-primary
           :border-style "solid"
           :border-width "0.25rem"}
     :ratio :auto
     :draw (fn [ctx]
             (let [w       (.-width ctx)
                   h       (.-height ctx)
                   n-frame (.-frameCount ctx)]
               (doto ctx
                (.background 255 255 255)
                (.noStroke)

                ; S
                (.fill theme/stage-s-color)
                (.circle (+ (* 0.1 w) (/ w 2)) (/ h 2) 10)

                ;; I
                (.fill theme/stage-i-color)
                (.circle (/ w 2) (/ h 2) 10)
                (.circle (+ (* 0.2 w) (/ w 2)) (/ h 2) 10)

                ;; infection radius
                (.strokeWeight 2)
                (.stroke theme/stage-i-color)
                (.noFill)
                (.circle (+ (* 0.2 w) (/ w 2)) (/ h 2) (mod (+ 0 n-frame) 40))
                (.circle (+ 0         (/ w 2)) (/ h 2) (mod (+ 10 n-frame) 40)))))])
