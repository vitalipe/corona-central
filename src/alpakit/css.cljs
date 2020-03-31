
(ns alpakit.css
  " a cljs implementation of styled components with garden.



  ISSUES:
    - only 1 style-sheet element per app is supported :(
       I coupled the diff logic for better performance.. I need to move it outside the component.
    - no style or css-var removal
       I'm not sure if we actually need this..
  "
  (:require
    [clojure.set    :as set]
    [clojure.string :as string]

    [cljs.cache :as cache]
    [garden.core :refer [css]]
    [garden.compiler :refer [render-css]]

    [reagent.core :as r]
    [alpakit.util :refer [pivot-by]]))


;; helpers
(defn hashable->css-str [thing]
  (.toString (hash thing) 36)) ;; 36 = [a-z] + [0-10]


(defn transform->css [transform]
  "render css transform based on type:
      string? - no changes
      list?   - no changes
      vector? - treat as a 4x4 homogeneous matrix (matrix3d)
      map?    - each key is converted to a css transform function,
                and applied in order of [translate, scale, rotate, skew]
  "
  (let [aliases {:rotate-x    "rotateX"
                 :rotate-y    "rotateY"
                 :rotate-z    "rotateZ"
                 :rotate-3d   "rotate3d"

                 :scale-3d    "scale3d"
                 :scale-x     "scaleX"
                 :scale-y     "scaleY"
                 :scale-z     "scaleZ"

                 :skew-x       "skewX"
                 :skew-y       "skewY"

                 :translate-x  "translateX"
                 :translate-y  "translateY"
                 :translate-z  "translateX"
                 :translate-3d "translate3d"}

        transform-order {"translate"   1
                         "translateX"  1
                         "translateY"  1
                         "translate3d" 1

                         "scale"   2
                         "scaleX"  2
                         "scaleY"  2
                         "scaleZ"  2
                         "scale3d" 2

                         "rotate"    3
                         "rotateX"   3
                         "rotateY"   3
                         "rotateZ"   3
                         "rotate3d"  3

                         "skew"      4
                         "skewX"     4
                         "skewY"     4}]

       (cond
         (vector? transform)   (list "matrix3d(" (into [] (flatten transform) ")"))
         (map?    transform)   (->> transform
                                   (map (fn [[k v]] [(or (aliases k) (name k)) v]))
                                   (sort-by (comp transform-order first))
                                   (mapcat (fn [[k v]] [k "(" v ")"]))
                                   (reverse)
                                   (into '()))
         :otherwise            transform)))



(defn transform->css-str [t]
  (->> (transform->css t)
    (map #(if (satisfies? IDeref %) (deref %) %))
    (string/join "")))


(defn apply-css-props-middleware [styles middleware]
  (loop [styles styles [[prop f] & middleware] middleware]
    (cond
        (nil? prop)              styles
        (contains? styles prop)  (recur (update styles prop f) middleware)
        :otherwise               (recur styles middleware))))


(defn extract-css-vars-and-normalize [styles]
  "-> extract atoms and replaces them with a css var in the form --alpakit-var-{{(hashable->css-str da-atom)}}
   -> render lists with spaces not commas (useful for gird-row-templates, transform-origin, transform functions etc..)

   returns a vector with the replaced var map and the normalized styles
  "
  (let [css-vars   (atom {})
        normalized (->> styles
                     (clojure.walk/postwalk
                       (fn [form]
                         (cond
                          (list? form)                 (string/join " " (map render-css form))
                          (satisfies? IDeref form)     (let [id  (str "--alpakit-css-var-" (hashable->css-str form))]
                                                         (swap! css-vars assoc id (deref form))
                                                         (str "var(" id ")"))

                          :otherwise-dont-fuck-with-it form))))]

       [@css-vars normalized]))


(defn rules->pairs [rules]
  "takes a rule map and returns a list of [selector rule] pairs.
   assumes that each map val is a rule and it's key is a selector"
  (when-not (empty? rules)
    (let [vanila-map? #(and (map? %) (not (record? %)))
          selector-keys (->> rules
                          (filter (comp vanila-map? val))
                          (map first))]
       (into
         [[:& (apply dissoc rules selector-keys)]]
         (map #(vector % (rules %)) selector-keys)))))


(defn- ->class-name [coll]
  "use hash to make unique short class names"
   (str "alpakit-" (hashable->css-str coll)))


(defn- ->style [selector rules]
  (let [class-name (->class-name [selector rules])]
    {:css          (css {:pretty-print? false} [(str "." class-name) [selector rules]])
     :style        [selector rules]
     :class-name   class-name}))



;; loaded styles and css vars, this should reflect the dom state
(def css-registry (atom {}))
(def css-var-registry (atom {}))

;; upcoming changes to be diffed
(def pending-css-updates     (atom {}))
(def pending-css-var-updates (atom {}))

;; cache computed css because it's expensive
(def style-cache (atom (cache/lru-cache-factory {} :threshold 10000)))


(defn create-css-through-cache! [rule-pairs]
  (doall
    (->> rule-pairs
      (map #(let [in-cache (cache/has? @style-cache %)
                  style (if in-cache
                          (cache/lookup @style-cache %)
                          (apply ->style %))]
              (if in-cache
                  (swap! style-cache cache/hit %)
                  (swap! style-cache cache/miss % style))

              style)))))


(defn register-css! [items]
  (->> items
    (pivot-by :class-name)
    (swap! pending-css-updates merge))
  items)


;;; API
(defn reset-css! []
  (reset! css-registry {})
  (reset! css-var-registry {})
  (reset! pending-css-updates {})
  (reset! pending-css-var-updates {}))

(defn css! [styles]
  "register css and return class names"
  (let [[css-vars nomalized-styles] (-> styles
                                      (apply-css-props-middleware {:transform transform->css})
                                      (extract-css-vars-and-normalize))]
    (swap! pending-css-var-updates merge css-vars)
    (->> nomalized-styles
      (rules->pairs)
      (create-css-through-cache!)
      (register-css!)
      (map :class-name)
      (string/join " "))))


(defn style-sheet []
  "style container element"
  (let [my-id (random-uuid)

        syncing-css-next-tick (atom false)
        sync-css! (fn [shit]
                    (let [next    @pending-css-updates
                          current @css-registry
                          diff    (reduce (fn [diff [k v]]
                                            (if-not (contains? current k)
                                             (assoc diff k v)
                                             diff))
                                     {} next)]

                      (if (empty? next)
                        ;; at this point we don't really care about removing styles, only handle `reset!`
                        (when (empty? current)
                          (while (pos? (.. shit -cssRules -length)) (.deleteRule shit 0)))
                        ;; when not empty? next
                        (do
                          (reset! pending-css-updates {})
                          (swap! css-registry merge diff)

                          (doall
                            (->> (vals diff)
                              (map :css)
                              (map #(.insertRule shit %))))))))


        syncing-vars-next-tick (atom false)
        sync-css-vars! (fn [shit]
                         (let [next @pending-css-var-updates
                               current @css-var-registry
                               diff (reduce (fn [diff [k v]]
                                              (if (not= v (current k))
                                               (assoc diff k v)
                                               diff))
                                       {} next)]
                           (when-not (empty? next)
                             (swap! css-var-registry merge diff)
                             (reset! pending-css-var-updates {})

                             (doall
                               (map #(.setProperty (.-style shit) (key %) (val %)) diff)))))]


    (r/create-class { :render (fn [] [:style.alpakit-css])
                      :componentWillUnmount #(do
                                               (remove-watch pending-css-var-updates my-id)
                                               (remove-watch pending-css-var-updates my-id))

                      :component-did-mount (fn [this]
                                             (let [css-shit (.-sheet (r/dom-node this))
                                                   css-var-shit (.. js/document -documentElement)]

                                               ;; add all known styles and vars
                                               (doall
                                                 (->> (vals @css-registry)
                                                   (map :css)
                                                   (map #(.insertRule css-shit %))))
                                               (doall
                                                 (->> @css-var-registry
                                                   (map #(.setProperty (.-style css-var-shit) (key %) (val %)))))

                                               ;; now sync! pending updates
                                               (sync-css! css-shit)
                                               (sync-css-vars! css-var-shit)

                                               ;; register css var sync
                                               (reset! syncing-vars-next-tick false)
                                               (add-watch
                                                 pending-css-var-updates
                                                 my-id
                                                 (fn [_ _ _ _]
                                                   (when-not @syncing-vars-next-tick
                                                     (reset! syncing-vars-next-tick true)
                                                     (r/next-tick #(do
                                                                     (reset! syncing-vars-next-tick false)
                                                                     (sync-css-vars! css-var-shit))))))

                                               ;; register css sync
                                               (reset! syncing-css-next-tick false)
                                               (add-watch
                                                 pending-css-updates
                                                 my-id
                                                 (fn [_ _ _ _]
                                                   (when-not @syncing-css-next-tick
                                                     (reset! syncing-css-next-tick true)
                                                     (r/next-tick #(do
                                                                     (reset! syncing-css-next-tick false)
                                                                     (sync-css! css-shit))))))))})))
