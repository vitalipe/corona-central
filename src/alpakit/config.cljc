(ns alpakit.config
  #?(:cljs (:require-macros [alpakit.config.env :refer [spit-cljs-compiler-config]]))
  (:require [alpakit.config.defaults :as defaults]
            [alpakit.util :as util]
            #?(:clj [alpakit.config.env :refer [spit-cljs-compiler-config]])))

(def compiler-options          (spit-cljs-compiler-config))
(def compiler-external-options (get compiler-options :external-config {}))


(def build-options
  (let [profile (if (= :advanced (compiler-options :optimizations))
                  defaults/prod-build
                  defaults/dev-build)]
    (util/deep-merge
      profile
      (get-in compiler-external-options [:alpakit :build])
      (get-in compiler-external-options [:alpakit/build]))))


(def initial-runtime-options
  (let [profile (if (= :advanced (compiler-options :optimizations))
                  defaults/prod-runtime
                  defaults/dev-runtime)]
    (util/deep-merge
      profile
      (get-in compiler-external-options [:alpakit :runtime])
      (get-in compiler-external-options [:alpakit/runtime]))))


(def runtime-options (atom initial-runtime-options))
