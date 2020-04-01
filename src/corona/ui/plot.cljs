(ns corona.ui.plot
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.p5 :refer [p5-canvas]]

    [corona.math  :refer [ceil round]]
    [corona.sim :as sim]
    [corona.ui.theme :refer [plot-i-color
                             plot-s-color
                             plot-r-color
                             plot-highlight-color]]))

;;; WIP
(defonce demo-sim (reduce
                    (fn [s _] (conj s (sim/step (last s))))
                    [(sim/init {:w 100 :h 100} [100 3 10])]
                    (range 42)))

(defonce demo-data (mapv :report demo-sim))
;;;


(defn draw-area! [ctx [x y w h] color width points]
  (doto ctx
    (.fill color)
    (.beginShape)
    (.vertex x (+ y h)))

  (loop [[p & points] points, x x]
    (.vertex ctx x (- (+ y h) (* (+ p) h)))
    (when-not (empty? points) (recur points (+ x width))))

  (doto ctx
    (.vertex (+ x w) (+ y h))
    (.endShape)))


(defw graph []
  [p5-canvas
     :draw (fn [ctx]
             (let [w (.-width ctx)
                   h (.-height ctx)

                   mouse-x (.-pmouseX ctx)

                   ;; graph bbox
                   g-w (- w 50)
                   g-h (- h 50)
                   g-x 30
                   g-y 2

                   xw-px (/ g-w (max 0 (dec (count demo-data))))
                   i-points (map :I                           demo-data)
                   s-points (map (fn [{:keys [I S]}] (+ I S)) demo-data)]

               (doto ctx
                (.background 0 0 0)
                (.noStroke)
                (.fill plot-r-color)
                (.rect g-x g-y g-w g-h))

              (draw-area! ctx [g-x g-y g-w g-h] plot-s-color xw-px s-points)
              (draw-area! ctx [g-x g-y g-w g-h] plot-i-color xw-px i-points)

              ;; yellow bar
              (when (< g-x mouse-x (+ g-x g-w))
                (doto ctx
                  (.stroke plot-highlight-color)
                  (.line mouse-x (+ 1 g-y) mouse-x (+ -1 g-y g-h))))

              ;; HUD
              (doto ctx
                (.stroke 255 255 255)
                (.strokeWeight 0.75)
                (.line g-x, g-y, g-x, (+ g-y g-h 10))
                (.line (- g-x 10), (+ g-y g-h), (+ g-x g-w), (+ g-y g-h)))

              (doall
                (->>
                  (range 0 1 0.1)
                  (map #(.line ctx
                          (+ 4 g-x,)
                          (+ g-y (* g-h %)),
                          (+ -4 g-x,)
                          (+ g-y (* g-h %))))))))])
