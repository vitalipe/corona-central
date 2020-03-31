(ns corona.sim
  (:require [alpakit.util :refer [pivot-by map-vals]]))


(defrecord Person [id
                   state      ; #{:S :I :R}
                   position   ; vector
                   velocity]) ; vector


(defn spawn-uninfected-person [{:keys [w h]} id]
  (map->Person {:id id
                :state :S
                :position [(rand-int w) (rand-int h)]
                :velocity [(- (rand-int 6) 3) (- (rand-int 6) 3)]}))

(def infected?    (comp (partial = :I) :state))
(def susceptible? (comp (partial = :S) :state))


(defn clamp [from val to]
  (max from (min to val)))


(defn spread-vec-2d [r [x y]]
  (for [dx (range (inc r))
        dy (range (inc r))]
    [(+ x dx) (+ y dy)]))


(defn naively-infect-population [infect% infect-dist population]
  (let [targets    (->> (vals population)
                     (filter susceptible?)
                     (group-by :position))

        infection-vectors (->> (vals population)
                            (filter infected?)
                            (map :position)
                            (mapcat (partial spread-vec-2d infect-dist))
                            (frequencies))]
       ;; FIXME: do the last step here...
       population))


(defn randomly-move-population [w h population]
  (->> population
    (map-vals (fn [{[x y] :position [dx dy] :velocity :as npc}]
                (assoc npc
                  :position [(clamp 0 (+ x dx) w) (clamp 0 (+ y dy) h)]
                  :velocity [(- (rand-int 6) 3) (- (rand-int 6) 3)])))))

;; API
(defn init [{:keys [w h] :as config} n]
  (let [population (->> (inc n)
                     (range 1)
                     (map (partial spawn-uninfected-person config))
                     (pivot-by :id))]
    {:step       0
     :population population
     :config     config
     :report {:S n
              :I 0
              :R 0}}))


(defn step [{{:keys [w h]} :config, :keys [step population] :as state}]
  (let [updated (->> population
                   (naively-infect-population 0.2 1)
                   (randomly-move-population w h))]
    (assoc state
       :step       (inc step)
       :population updated
       :report     (merge
                     {:S 0 :I 0 :R 0}
                     (->> (vals updated)
                       (map :state)
                       (frequencies))))))
