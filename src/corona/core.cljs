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
    [corona.ui.stage :refer [stage]]
    ["/corona/sim/py/sim" :as py-sim]))


(comment
   @barak

     this fails ->
      (py-sim/construct 100 100 50 10)
      "Failed to load corona/core.cljs TypeError: Cannot set property 'undefined' of undefined
          at populate_world$$module$corona$sim$py$sim (sim.js:354)
          at Object.construct$$module$corona$sim$py$sim [as construct] (sim.js:363)
          at eval (core.cljs:26)
          at eval (<anonymous>)
          at Object.goog.globalEval (main.js:2128)
          at Object.shadow$cljs$devtools$client$browser$script_eval [as script_eval] (browser.cljs:29)
          at Object.shadow$cljs$devtools$client$browser$do_js_load [as do_js_load] (browser.cljs:41)
          at eval (browser.cljs:58)
          at eval (env.cljs:257)
          at Object.shadow$cljs$devtools$client$env$do_js_reload_STAR_ [as do_js_reload_STAR_]"

    when you set breakpoint in line 354 -> sim.js, it looks like status is a number ¯\_(ツ)_/¯)



(defw controls []
  [surface :css {:background-color theme/color-secondary
                 :padding "1rem"}
   "controls here..."])


(defw sandbox []
  [surface :css {:width "100%"}
           :layout (layout/grid :gap "2rem"
                                :areas ["minmax(20rem, 1fr)"    "minmax(10rem, 20rem)"
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
