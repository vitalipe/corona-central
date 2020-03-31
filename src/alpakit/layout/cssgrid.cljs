(ns alpakit.layout.cssgrid
  (:require
    [clojure.string :refer [join]]
    [garden.selectors :as selectors]
    [garden.units     :as units]

    [alpakit.util  :refer [map-kv]]
    [alpakit.props :refer [get-key]]
    [alpakit.layout.protocol :refer [LayoutStrategy]]))


;; util
(defn- map-when [test effect coll]
  (map #(if (test %) (effect %) %)  coll))

(defn- css-unit-or-ref? [thing]
  (or
    (satisfies? IDeref thing)
    (string? thing)
    (units/unit? thing)))


(defn areas->css [areas]
  "
     [20%     1fr    2fr    (px 100)
      [:A      :A     :A     :A]  10%
      [:B      :C     :C     nil] 1fr
      [:B      :C     :C     nil] 200px]

     =>

    {:grid-template-areas [[A A A A]
                           [B C C .]
                           [B C C .]]
     :grid-template-rows    10% 1fr 200px
     :grid-template-columns (20% 1fr  2fr #garden.types.CSSUnit{:unit :px, :magnitude 100})
  "
  {:grid-template-areas (->> areas
                          (filter vector?)
                          (mapv (partial map-when nil? (constantly :.))) ;; dot means an empty gird cell
                          (mapv (partial map name))
                          (mapv (partial join " "))
                          (mapv #(str "\"" % "\""))
                          (join " "))

   :grid-template-columns (->> areas
                            (take-while css-unit-or-ref?)
                            (into '())
                            (reverse))

   :grid-template-rows (->> areas
                         (drop-while css-unit-or-ref?)
                         (drop 1)
                         (take-nth 2)
                         (into '())
                         (reverse))})


(defn rows+cols->css [rows cols]
  (merge {}
    (when-not (empty? cols) {:grid-template-columns (into '() cols)})
    (when-not (empty? rows) {:grid-template-rows    (into '() rows)})))


(defn gap->css [gap]
  (let [[row-gap col-gap] (if (string? gap) [gap gap] gap)]
    {:grid-column-gap col-gap
     :grid-row-gap    row-gap}))


(defn place-content->css [[align justify]]
  ;; edge has no support for "place-items", so let's split
  {:align-content (name align)
   :justify-content (name justify)})


(defn place-items->css [[align justify]]
  ;; edge has no support for "place-items", so let's split
  {:align-items (name align)
   :justify-items (name justify)})


(defn auto-sizes->css [[row col]]
  {:grid-auto-columns col
   :grid-auto-rows row})


(defn auto-flow->css [flow]
  {:grid-auto-flow (name flow)})


(defn grid-children->css [index-area-map]
  (->> index-area-map
    ;; I'm not sure if it's a garden bug or just me can't read docs.
    ;; if we pass an int here we get "2n" "1n" etc.. but if we pass a string we just get a number
    (map-kv (comp selectors/nth-child str inc)
            #(hash-map :grid-area (name %)))))


(defn grid-repeat->css [min-max]
  (let [css-sizes (if (vector? min-max)
                    (str "minmax(" (join "," min-max) ")")
                    min-max)]
    {:grid-template-columns (str "repeat(auto-fill," css-sizes ")")}))


(defrecord GridLayout [areas
                       rows
                       cols
                       gap
                       place-items
                       place-content
                       auto-sizes
                       auto-flow]
  LayoutStrategy
    (generate-layout-styles [{:keys [areas
                                     rows
                                     cols
                                     gap
                                     place-items
                                     place-content
                                     auto-sizes
                                     auto-flow]}
                             children]
      (let [index-area-map (->> children
                             (map-indexed (fn [idx child]
                                           [idx (get-key child)]))
                             (remove (comp nil? second))
                             (into {}))]

        (into {}
          (remove (comp nil? val)
            (merge {:display "grid"}
                   (areas->css areas)
                   (rows+cols->css rows cols)
                   (gap->css gap)
                   (place-items->css place-items)
                   (place-content->css place-content)
                   (auto-sizes->css auto-sizes)
                   (auto-flow->css auto-flow)
                   (grid-children->css index-area-map)))))))


(defrecord AutofillGridLayout [gap
                               min-max]
  LayoutStrategy
    (generate-layout-styles [{:keys [gap min-max]} _]
      (merge {:display "grid"}
             (gap->css gap)
             (grid-repeat->css min-max))))
