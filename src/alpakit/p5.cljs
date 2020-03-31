(ns alpakit.p5
  (:require
    [reagent.core :as r]
    [alpakit.widget :refer [defw]]
    [alpakit.core :as alpakit :refer [surface]]
    ["p5" :as    p5]))


(defn only-once [f]
  (let [called (atom false)]
    (fn [& args]
      (when-not @called
        (reset! called true)
        (apply f args)))))


(defn ratio+bbox->size [initial ratio bbox]
  (let [width  (if (= :auto (initial 0)) (.-width bbox)  (initial 0))
        height (if (= :auto (initial 1)) (.-height bbox) (initial 1))]
    (if (> (/ width height) ratio)
      [(* height ratio) height]
      [width (/ width ratio)])))


(defn init-p5! [draw {:keys [fps width height]} p5-instance]
  (let [setup (fn []
                (doto p5-instance
                  (.createCanvas width height)
                  (.setFrameRate fps)))]

    (set! (.-setup p5-instance)  setup)
    (set! (.-draw p5-instance) (partial draw p5-instance))))


(defw p5-canvas [ratio
                 size
                 target-fps
                 draw
                 :or {size [:auto :auto]
                      ratio (/ 4 3)
                      target-fps 30}]

  (r/with-let [!cleanup (atom nil)
               init! (only-once ;; refs are wierd.. they are called with nil on every update..
                       (fn [node]
                         (let [[width height] (ratio+bbox->size size ratio (.getBoundingClientRect node))
                               p5-config {:width  width
                                          :height height
                                          :fps target-fps}
                               instance   (new p5 (partial init-p5! draw p5-config) node)
                               sync-size! (fn [e]
                                            (let [bbox  (.-contentRect (aget e 0))
                                                  [w h] (ratio+bbox->size size ratio bbox)]
                                              (.resizeCanvas instance w h)))

                               resize-observer  (doto (new js/ResizeObserver sync-size!)
                                                  (.observe node))]
                            (reset!
                              !cleanup #(do
                                          (.remove instance)
                                          (.unobserve resize-observer node))))))]

    [surface :css {:overflow "hidden"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"}
             :-attr {:ref init!}]

    (finally (@!cleanup))))
