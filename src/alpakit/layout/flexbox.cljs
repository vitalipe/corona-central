(ns alpakit.layout.flexbox
  (:require
    [alpakit.layout.protocol :refer [LayoutStrategy]]))


(defrecord FlexBoxLayout [justify
                          align
                          direction
                          wrap
                          reverse]

  LayoutStrategy
    (generate-layout-styles [{:keys [justify
                                     align
                                     direction
                                     wrap
                                     reverse]} _]

      {:display         "flex"
       :justify-content (name justify)
       :align-items     (name align)
       :flex-wrap       (name wrap)
       :flex-direction  (if reverse
                          (str (name direction) "-reverse")
                          (name direction))}))
