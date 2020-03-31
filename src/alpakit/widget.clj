(ns alpakit.widget
  (:require
    [reagent.core :as r]
    [alpakit.props :refer [normalize-args args->props]]
    [alpakit.util :refer [map-kv
                          map-vals
                          map-keys]]))


(defn deref-atom-access [body symbols]
  "walk over body and deref all symbols from `symbols`, unless it's a swap!/reset! call"
  (let [atom-operation? #(and (list? %) (#{'swap! 'reset! `swap! `reset!} (first %)))
        target-symbol?  (into #{} symbols)]
    (clojure.walk/postwalk
        (fn [form]
          (cond
            (target-symbol? form)  (list 'cljs.core/deref form)
            ;; this will be called on the way up (postwalk),
            ;; it's a great place to remove derefs on swap! reset! etc..
            (atom-operation? form)  (let [[f [_ a] & args] form] (concat (list f a) args))
            :otherwise              form))
        body)))


(defn with-lifecycle-meta [lifecycle widget]
  (if (empty? lifecycle)
    widget
    ;; otherwise merge lifecycle methods into metadata
    (let [with-clj-props (fn [?update-fn]
                           (when ?update-fn
                              (let [props-path '(.. this -props -argv)]
                                `(fn [~(symbol "this")]
                                   (~?update-fn
                                     (args->props
                                      (rest ~props-path)))))))
          methods (->> {:component-did-update   (with-clj-props (get lifecycle :update))
                        :component-did-mount    (get lifecycle :mount)
                        :component-will-unmount (get lifecycle :unmount)}
                    (remove (comp nil? val))
                    (into {}))]
      (with-meta widget (merge methods (meta widget))))))


(defn vec->hmap-props [args]
  (let [i-of-or (loop [i 0 [a & args] args]
                  (cond
                    (= a :or)     (inc i)
                    (empty? args) -1
                    :otherwise    (recur (inc i) args)))

        n-args   (if (pos? i-of-or) (dec i-of-or) (count args))
        defaults (map-vals #(hash-map :default %) (nth args i-of-or {}))

        props (->> args
                (take n-args)
                (map #(vector % {}))
                (into {}))]
    (merge
      props
      (select-keys
        defaults
        (keys props)))))


(defn normalize-spec [[?doc & spec]]
  (let [[doc ?props spec] (if (string? ?doc)
                            [?doc (first spec) (rest spec)]
                            ["" ?doc spec])]

    (let [[spec body] (if (vector? ?props)
                        ;; props are in this form -> [p1 p2 :or {p1 42}]
                        (let [[kv-spec body] (normalize-args spec)
                              vec-spec {:props (vec->hmap-props ?props)}]
                          [(merge kv-spec vec-spec)  body])
                        ;; props are in this form -> :props {p1 {:default 42} p2 {}}
                        (normalize-args (cons ?props spec)))]
      [doc spec body])))


(defmacro widget [& spec]
  (let [[docstring {:keys [props state lifecycle]} body] (normalize-spec spec)]
    (let [state-specs  (map-keys keyword state)
          prop-defaults (->> props
                          (filter #(contains? (val %) :default))
                          (map-kv keyword :default))

          prop-specs  (merge {:children (fn [& _] true)} (map-vals :spec props))
          prop-names  (conj (keys props) (symbol "children"))

          arg-list-sym (gensym "arg-list")

          no-state (empty? (keys state))
          no-props (empty? (keys props))
          only-body (and no-state no-props)]

        (with-lifecycle-meta lifecycle
          (cond
            only-body
                 (list 'fn ['& arg-list-sym]
                   docstring
                   ;; children
                   `(let [{:keys [~@prop-names] :as ~(symbol "-props")} (args->props ~arg-list-sym)]
                      ~@body))

            no-state
                 (list 'fn ['& arg-list-sym]
                   docstring
                   ;;  props & children
                   `(let [{:keys [~@prop-names] :as ~(symbol "-props")} (args->props ~arg-list-sym
                                                                                     ~prop-defaults)]
                      ;; FIXME: fix in cljs  TODO make nice errors
                      ;(doseq [[~(symbol "p") ~(symbol "spec-info")] ~prop-specs]
                        ;(spec/assert ~(symbol "spec-info") ~(symbol "p"))

                      ~@body))

            no-props
                 (list 'fn ['& arg-list-sym]
                  docstring
                   ;; children
                   `(let [{:keys [~@prop-names] :as ~(symbol "-props")} (args->props ~arg-list-sym)]
                      ;; FIXME: fix in cljs TODO make nice errors
                      ;(doseq [[~(symbol "p") ~(symbol "spec-info")] ~prop-specs]
                        ;(spec/assert ~(symbol "spec-info") ~(symbol "p"))
                      ;; state
                      (r/with-let [{:keys [~@(keys state)]} (map-vals r/atom ~state-specs)
                                   ~@(mapcat #(vector (symbol (str "!" %)) %) (keys state))]
                        ~@(deref-atom-access body (keys state)))))

            :state+props
                 (list 'fn ['& arg-list-sym]
                   docstring
                    ;; props & children
                   `(let [{:keys [~@prop-names] :as ~(symbol "-props")} (args->props ~arg-list-sym
                                                                                     ~prop-defaults)]
                      ;; FIXME: fix in cljs TODO make nice errors
                      ;(doseq [[~(symbol "p") ~(symbol "spec-info")] ~prop-specs]
                        ;(spec/assert ~(symbol "spec-info") ~(symbol "p"))
                         ;; state
                      (r/with-let [{:keys [~@(keys state)]} (map-vals r/atom ~state-specs)
                                   ~@(mapcat #(vector (symbol (str "!" %)) %) (keys state))]
                        ~@(deref-atom-access body (keys state))))))))))


(defmacro defwidget [name & spec]
  `(def ~(symbol name) (widget ~@spec)))

(defmacro defw [name & spec]
  `(def ~(symbol name) (widget ~@spec)))
