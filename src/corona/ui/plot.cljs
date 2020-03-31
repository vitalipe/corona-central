(ns corona.ui.plot
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.p5 :refer [p5-canvas]]

    [corona.ui.theme :refer [plot-i-color plot-s-color plot-r-color]]))


(def static-demo-data (->> (range 0 100 2)
                           (mapv #(let [s (- 100 %)
                                        i (/ (- 100 s) 2)
                                        r i]
                                    {:S s
                                     :I i
                                     :R r}))))

(defw graph []
  [p5-canvas
     :draw (fn [ctx]
             (let [w (.-width ctx)
                   h (.-height ctx)]
               (doto ctx
                (.background plot-r-color)
                (.noStroke)
                (.fill plot-i-color)

                (.beginShape)
                (.vertex 0 h)
                (.vertex 50 (- h 100))
                (.vertex 200 (- h 200))
                (.vertex 240 (- h 200))
                (.vertex 350 (- h 210))
                (.vertex 460 (- h 240))
                (.vertex 570 (- h 300))
                (.vertex 670 (- h 100))
                (.vertex 780 h)

                (.endShape))))])
