(ns corona.math)

(defn clamp [from val to]
  (max from (min to val)))

(defn round [n]
  (.round js/Math n))

(defn ceil [n]
  (.ceil js/Math n))

(defn ** [n k]
  (.pow js/Math n k))
