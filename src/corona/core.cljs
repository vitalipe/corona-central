(ns corona.core
  (:require
    [reagent.core :as r]

    [alpakit.widget :refer [defw]]
    [alpakit.core :as alpakit :refer [element app surface]]
    [alpakit.layout :as layout]
    [alpakit.p5 :refer [p5-canvas]]

    [corona.ui.theme :as theme]
    [corona.ui.menus :refer [main-menu]]

    [corona.ui.plot :refer [graph]]
    [corona.ui.stage :refer [stage]]))


(defw controls []
  [surface :css {:background-color theme/color-secondary
                 :padding "1rem"}
   "controls here..."])


(defw sandbox []
  [surface :css {:width "100%"}
           :layout (layout/grid :gap "2rem"
                                :areas ["1fr"    "30rem"
                                        [:graph :controls] "200px"
                                        [:stage :controls] "1fr"])
      ^{:key :stage}    [stage]
      ^{:key :graph}    [graph]
      ^{:key :controls} [controls]])

(defw corona-app []
  :state {screen :menu}

  [app :theme [theme/baseline-css theme/global-css]
    [surface  :layout (layout/h-box :justify :center)
              :css {:padding "6rem"
                    :height "100vh"}

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
