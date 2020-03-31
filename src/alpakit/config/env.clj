(ns alpakit.config.env
  "macros that capture the build env"
  (:require [cljs.env]))


(defmacro spit-cljs-compiler-config []
  (-> @cljs.env/*compiler*
    (get :options)
    ;; https://github.com/clojure/clojurescript-site/blob/master/content/reference/compiler-options.adoc#compiler-options
    ;; symbols are not seralized correctly, so I dropped a few keys.. can add them later if we need..
    (select-keys [:external-config
                  :optimizations
                  :source-map
                  :main
                  :asset-path
                  :output-to
                  :output-dir
                  :foreign-libs
                  :npm-deps
                  :install-deps])))
