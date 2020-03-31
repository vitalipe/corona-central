(ns alpakit.util)


(defn deep-merge [& maps]
  "recursively merge maps, useful for config etc..
   note: that nil will override values (same as merge)"
  (apply merge-with
         #(cond
             (every? map? %&) (apply deep-merge %&)
             :otherwise       (last %&))
     maps))


(defn map-kv [k-fn v-fn a-map]
  "map over a seq of [k v] pairs, returns a map"
  (->> a-map
    (map #(vector (k-fn (first %)) (v-fn (second %))))
    (into {})))


(defn map-keys [k-fn a-map]
  "map over keys"
  (map-kv k-fn identity a-map))


(defn map-vals [v-fn a-map]
  "map over vals"
  (map-kv identity v-fn a-map))


(defn pivot-by [f items]
  "like group by, but will only keep the first val"
  (->> items
    (group-by f)
    (map-vals first)))
