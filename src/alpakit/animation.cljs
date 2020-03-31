(ns alpakit.animation
  (:require
   [alpakit.animation.impl.spring :as springs]))




(def spring springs/spring)
(defn spring+control [ & args]
  (let [s (apply springs/spring args)]
    [s (.-control s)]))
