(ns corona.ui.menus
  (:require
    [reagent.core :as r]
    [garden.color :refer [darken]]

    [alpakit.widget :refer [defw]]
    [alpakit.core :as alpakit :refer [surface element]]
    [alpakit.layout :as layout]
    [corona.ui.theme :as theme]))




(defw menu-button [text disabled? on-click :or {disabled? false on-click #()}]
  (let [override (when disabled?
                   {:background-color theme/color-disabled
                    :cursor          nil
                    :&:hover         nil})]
    [element :css (->> override
                    (merge
                      {:text-align       "center"
                       :font-size        "1.5rem"
                       :padding          "1rem"
                       :border-radius    "0.5rem"
                       :background-color theme/color-secondary
                       :width            "90%"
                       :margin-top       "0.25rem"
                       :cursor           "pointer"
                       :text-transform   "capitalize"
                       :&:hover {:background-color (darken theme/color-secondary 20)}}))

            :-attr {:on-click (fn [_] (on-click))}

     text]))


(defw main-menu [items on-select]
   [surface :layout (layout/v-box :align :center)
      [surface :css {:background-color theme/color-primary
                     :border-radius "1rem 1rem 0 0"
                     :padding "1rem"
                     :margin-bottom "0.75rem"}


         [element :e :h4 "Welcome to"]
         [element :e :h1
                  :css {:text-align "center"
                        :margin "1rem 0 0.5rem 0"}
            "Corona Central"]]

     (for [{:keys [label value disabled?]} items]
       ^{:key value}
        [menu-button
         :text      label
         :disabled? disabled?
         :on-click  #(on-select value)])])
