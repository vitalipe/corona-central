(ns alpakit.animation.macros
  (:require
   [alpakit.animation.impl.transitionable :refer [->Transitionable]]))



(defmacro tr> [transitionable & forms]
  "create a treansducer from (comp forms) and return a new transitionable,
   with the transducer applied. it sets the hash from `forms` together with
   the source transitionable"
  (let [atom-sym (gensym "ratom-")
        t-sym (gensym "transitionable-")
        xf-sym (gensym "xf-")
        forms-hash (hash forms)]
    `(let [~t-sym ~transitionable
           ~atom-sym (r/atom nil)]
       (if-let [ ~(symbol "t") (get-in (meta ~t-sym) [:alpakit/listeners ~forms-hash])]
          ~(symbol "t")
          ;;otherwise
          (let [~(symbol "new-t")
                (let [~atom-sym (r/atom nil)
                      ~xf-sym ((apply comp [~@forms]) ;; <- init transducer
                               (fn [~(symbol "_") ~(symbol "v")] ;; <- reduce fn
                                 (reset! ~atom-sym ~(symbol "v"))))]
                  ;; set initial value before we listen
                 (~xf-sym
                    nil
                   (deref ~t-sym))

                 (add-watch ~t-sym
                   (cljs.core/random-uuid)
                   (fn [~@(map symbol (repeat 3 "_")) ~(symbol "next")]
                     (~xf-sym
                       (deref ~atom-sym)
                       ~(symbol "next"))))

                 (->Transitionable
                   (reagent.core/cursor #(do
                                           ;; first deref is just to trigger the source
                                           (deref ~t-sym)
                                           (deref ~atom-sym))
                                        [])
                   (reagent.core/cursor #() []) ;; no swaps allowed!!
                   (.-control ~t-sym)
                   {}
                   ;; using forms + transitionable as a hash will always ensure that:
                   ;;      (= (tr> a-spring (map inc) (filter odd?))
                   ;;         (tr> a-spring (map inc) (filter odd?)))
                   (hash [(hash ~t-sym) ~forms-hash])))]

              ;; cache and return
              ;; FIXME: I should read the docs next time... :P with-meta should not mutate!
              (with-meta ~t-sym (assoc-in
                                  (meta ~t-sym)
                                  [:alpakit/listeners ~forms-hash]
                                  ~(symbol "new-t")))
              ~(symbol "new-t"))))))
