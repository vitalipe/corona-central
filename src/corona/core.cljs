(ns corona.core
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.core :as alpakit :refer [app surface]]
    [alpakit.layout :as layout]
    [alpakit.p5 :refer [p5-canvas]]
    [corona.ui.theme :as theme]
    [corona.ui.plot :refer [graph]]
    [corona.ui.stage :refer [stage]]))



(defw sandbox []
  [app :theme [theme/baseline-css theme/global-css]
   [surface :css {:padding-top "2rem"}
            :layout (layout/grid :areas ["1fr"       "1fr"
                                         [:graph    :stage]    "400px"
                                         [:controls :controls] "300px"])
      ^{:key :stage}    [stage]
      ^{:key :graph}    [graph]
      ^{:key :controls} [surface "sim controls"]]])


;; for shadow live reload
(defn ^:dev/after-load render! []
  (alpakit/render! "#app" [sandbox]))

(defn ^:export main []
  (render!))
