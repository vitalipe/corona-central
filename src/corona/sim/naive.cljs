(ns corona.sim.naive
  (:require
    [alpakit.util :refer [pivot-by map-vals]]
    [corona.math  :refer [clamp **]]))


(defrecord Person [id
                   state      ; #{:S :I :R}
                   position   ; vector-2d
                   velocity]) ; vector-2d


(defn spawn-person [{:keys [w h]}
                    gods-of-rng-hate-this-one?
                    this-one-is-saved?
                    id]
  ;; we all live in a fucking simulation, made by shitty web programmers :(
  (map->Person {:id id
                :position [(rand-int w) (rand-int h)]
                :velocity [(- (rand-int 6) 3) (- (rand-int 6) 3)]
                :state    (cond
                            (gods-of-rng-hate-this-one? id) :I
                            (this-one-is-saved? id)         :R
                            :youre-not-special              :S)}))


(def infected?    (comp (partial = :I) :state))
(def susceptible? (comp (partial = :S) :state))


(defn spread-vec-2d [r [x y]]
  (for [dx (range (inc r))
        dy (range (inc r))]
    [(+ x dx) (+ y dy)]))

(defn population->SIR% [population]
  (let [stats (->> (vals population)
                (map :state)
                (frequencies)
                (merge {:S 0 :I 0 :R 0}))]
    (-> stats
      (update :S / (count population))
      (update :I / (count population))
      (update :R / (count population)))))


(defn naively-infect-population [infect% infect-dist population]
  (let [n->% (fn [n] (- 1 (** (- 1 infect%) n)))
        targets    (->> (vals population)
                     (filter susceptible?)
                     (group-by :position))

        vectors (->> (vals population)
                  (filter infected?)
                  (map :position)
                  (mapcat (partial spread-vec-2d infect-dist))
                  (frequencies))

        infected-ids (->> targets
                       (map    (fn [[v targets]] [(get vectors v 0), targets]))
                       (map    (fn [[n targets]] [(n->% n),          targets]))
                       (mapcat (partial apply random-sample))
                       (map    :id))]
      (reduce
        (fn [population id] (assoc-in population [id :state] :I))
        population
        infected-ids)))


(defn randomly-move-population [w h population]
  (->> population
    (map-vals (fn [{[x y] :position [dx dy] :velocity :as npc}]
                (assoc npc
                  :position [(clamp 0 (+ x dx) w) (clamp 0 (+ y dy) h)]
                  :velocity [(- (rand-int 6) 3) (- (rand-int 6) 3)])))))


;; API
(defn init [{:keys [w h] :as config} [n-susceptible n-infected n-retired]]
  (let [id-seed             (range 1 (+ 1 n-susceptible n-infected n-retired))
        ids-of-the-infected (->> id-seed
                              (shuffle)
                              (take n-infected)
                              (into #{}))
        ids-of-the-retired (->> id-seed
                              (shuffle)
                              (take n-retired)
                              (into #{}))
        spawn      (partial spawn-person config ids-of-the-infected ids-of-the-retired)
        population (->> id-seed
                     (map spawn)
                     (pivot-by :id))]

    {:step       0
     :population population
     :config     config
     :report     (population->SIR% population)}))


(defn step [{{:keys [w h]} :config, :keys [step population] :as state}]
  (let [updated (->> population
                   (naively-infect-population 0.9 10)
                   (randomly-move-population w h))]
    (assoc state
       :step       (inc step)
       :population updated
       :report     (population->SIR% updated))))
