(ns alpakit.core
  (:require
    [clojure.set :as set]
    [clojure.string :as string]

    [reagent.core :as r]
    [garden.core :as garden]

    [alpakit.widget :refer-macros [defwidget]]
    [alpakit.css    :as css :refer [transform->css-str style-sheet]]
    [alpakit.layout :as layout]
    [alpakit.layout.protocol :refer [generate-layout-styles]]

    [alpakit.props :as props]))



(defwidget surface
  "a building block for more complex dom based widgets with layout support"

  :props {-attr       {:default {}}

          e           {:default :div}
          css         {:default {}}
          style       {:default {}}

          layout      {:default layout/box-layout}
          transform   {:default nil}}

  (let [current-layout (if (fn? layout) (layout) layout)
        transform (or transform (:transform css) {})
        style-props    {:style (-> style
                                 (merge {:transform (transform->css-str transform)}))

                        :class (->> children
                                 (generate-layout-styles current-layout)
                                 (merge css)
                                 (css/css!))}]

    (into [e (merge  style-props -attr)] children)))


(defwidget element
  "a building block for dom based widgets"

  :props {-attr       {:default {}}

          e           {:default :div}
          css         {:default {}}
          style       {:default {}}

          transform   {:default nil}}


    (let [transform (or transform (:transform css) {})
          props (merge {:style (-> style
                                 (merge {:transform (transform->css-str transform)}))
                        :class (css/css! css)}

                      -attr)]

      (into [e props] children)))


(defwidget app
  "top level app container"

  :props {theme {:default {}}}
  :state {uuid  (random-uuid)}

  (into
    [:div {:class (str "alpakit-app-" uuid)}
     [:style.app-theme
      (garden/css theme)]
     [style-sheet]]
   children))


(defn render!
  ([app] (render! "body" app))
  ([parent-selector app]
   (let [root (.querySelector js/document parent-selector)]
     (if-not (= root (.-body js/document))
       (r/render app root)
       (let [root (.createElement js/document "div")]
         (.appendChild (.-body js/document) root)
         (r/render app root))))))
