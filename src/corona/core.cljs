(ns corona.core
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.core :as alpakit :refer [app surface]]
    [alpakit.layout :as layout]
    [alpakit.p5 :refer [p5-canvas]]

    [corona.ui.theme :as theme]
    [corona.ui.menus :refer [main-menu]]

    [corona.ui.plot :refer [graph]]
    [corona.ui.stage :refer [stage]]))


(defw sandbox []
  [surface :css {:width "100%"}
           :layout (layout/grid :areas ["1fr"       "1fr"
                                        [:graph    :stage]    "400px"
                                        [:controls :controls] "300px"])
      ^{:key :stage}    [stage]
      ^{:key :graph}    [graph]
      ^{:key :controls} [surface "sim controls"]])


(defw corona-app []
  :state {screen :menu}

  [app :theme [theme/baseline-css theme/global-css]
    [surface  :layout (layout/h-box :justify :center)
              :css {:padding "6rem"}

     (case screen
       :menu [main-menu :on-select #(reset! screen %)
                        :items [{:label "new Game"     :value :new-game :disabled? true}
                                {:label "sandbox mode" :value :sandbox}
                                {:label "about"        :value :about}
                                {:label "credits"      :value :credits}]]
       :sandbox [sandbox])]])


;; for shadow live reload
(defn ^:dev/after-load render! []
  (alpakit/render! "#app" [corona-app]))

(defn ^:export main []
  (render!))
