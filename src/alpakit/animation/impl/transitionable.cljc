(ns alpakit.animation.impl.transitionable
  (:require
   [reagent.core :as r]
   #?(:cljs [reagent.ratom :refer [IReactiveAtom]])))


#?(:clj (deftype Transitionable [a b c d e]))
#?(:cljs
    (deftype Transitionable [anim-cursor    ;; a cursor into control, used for deref
                             control-cursor ;; a cursor into control used for swap!/reset!
                             control        ;; stores all the control info
                             ^:mutable meta
                             hash-token]

      IAtom
      IReactiveAtom

      IEquiv
      (-equiv [this other]
        (and (instance? Transitionable other)
             (= (-hash this) (-hash other))))

      IDeref
      (-deref [this]
        (-deref anim-cursor))

      IReset
      (-reset! [this new-value]
        (-reset! control-cursor new-value))

      ISwap
      (-swap! [_ f]          (-swap! control-cursor f))
      (-swap! [_ f x]        (-swap! control-cursor f x))
      (-swap! [_ f x y]      (-swap! control-cursor f x y))
      (-swap! [_ f x y more] (-swap! control-cursor f x y more))

      IPrintWithWriter
      (-pr-writer [_ w opts] (-pr-writer anim-cursor w opts))

      IWatchable
      (-notify-watches [_ old new] (-notify-watches anim-cursor old new))
      (-add-watch      [_ key f]   (-add-watch anim-cursor key f))
      (-remove-watch   [_ key]     (-remove-watch anim-cursor key))

      IHash
      (-hash [_] hash-token)

      IWithMeta
      (-with-meta [this new-meta]
        (do
          (set! meta new-meta)
          this))

      IMeta
      (-meta [this] (.-meta this))))
