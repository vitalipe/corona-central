(ns alpakit.layout.protocol)


(defprotocol LayoutStrategy
  (generate-layout-styles  [this children]))
