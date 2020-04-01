(ns corona.ui.theme)

;; moar reset css
(def baseline-css [[:html {:-ms-text-size-adjust     "100%"
                           :-webkit-text-size-adjust "100%"
                           :-moz-osx-font-smoothing  "grayscale"
                           :-webkit-font-smoothing   "antialiased"}]

                   [:*,
                    :*:before,
                    :*:after {:box-sizing "border-box"
                              :margin 0}]

                   [:a:link,
                    :a:visited,
                    :a:hover,
                    :a:active {:color "currentColor"
                               :text-decoration "none"
                               :-webkit-tap-highlight-color "transparent"}]

                   [:textarea {:resize "none"}]])


(def global-css [[:html {:font-size "16px"}]
                 [:body {:color            "white"
                         :background-color "black"}]])


(def plot-s-color #js[49 97 110])
(def plot-i-color #js[245 102 84])
(def plot-r-color #js[68 67 69])

(def plot-highlight-color #js[251 255 21])


(def stage-s-color #js[116 195 206])
(def stage-i-color #js[245 102 84])
(def stage-r-color #js[68 67 69])
