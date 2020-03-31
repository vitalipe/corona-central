(ns alpakit.props)


(defn normalize-kv-args [arg-list]
  "takes a seq of keyword args followed by n positional args
   and returns a map and a list of positional args.
     for example:
        [:x 42 :y false 1 2 3 4 5] => [{:x 42 :y false} (1 2 3 4 5)]
        [1 2 3 4 5]                => [{} (1 2 3 4 5)]
        [:x 42 :y false]           => [{:x 42 :y false} ()]
"
  (let [rest-index (loop [[arg & other] arg-list index 0]
                      (cond
                        (keyword? arg) (recur (rest other) (+ 2 index))
                        :otherwise     index))
        kv-args   (apply hash-map (take rest-index arg-list))
        rest-list (drop rest-index arg-list)]

      [kv-args rest-list]))


(defn normalize-args [[first-arg & rest-args :as body]]
  "takes a hiccup body OR a seq of keyword args followed by n positional args
   and returns a map and a list of positional args.

     for example:
        [{:x 42 :y false} 1 2 3 4 5] => [{:x 42 :y false} (1 2 3 4 5)]
        [:x 42 :y false 1 2 3 4 5]   => [{:x 42 :y false} (1 2 3 4 5)]

        [1 2 3 4 5]                  => [{} (1 2 3 4 5)]

        [{:x 42 :y false}]           => [{:x 42 :y false} ()]
        [:x 42 :y false]             => [{:x 42 :y false} ()]
"
  (cond
    (map? first-arg)            [first-arg rest-args]
    (not (keyword? first-arg))  [{} body]
    :otherwise                  (normalize-kv-args body)))


(defn get-prop
  ([e key] (get-prop e key nil))
  ([[_ & body] key default]
   ;; FIXME: this is not fast!
   (let [[props _] (normalize-args body)]
     (get props key default))))


(defn get-key [e]
  (or ;; prefer prop over meta..
    (get-prop  e   :key)
    (get (meta e)  :key)))


(defn args->props
  "collect kv or hiccup into a map with default values, useful for props+children"
  ([argv] (args->props argv {}))
  ([argv defaults]
   (let [[props children] (normalize-args argv)]
     (merge defaults props {:children children}))))
